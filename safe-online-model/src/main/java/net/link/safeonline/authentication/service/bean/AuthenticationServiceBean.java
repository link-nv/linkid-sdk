/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.authentication.service.bean;

import static net.link.safeonline.authentication.service.AuthenticationState.INIT;
import static net.link.safeonline.authentication.service.AuthenticationState.USER_AUTHENTICATED;
import static net.link.safeonline.model.bean.UsageStatisticTaskBean.loginCounter;
import static net.link.safeonline.model.bean.UsageStatisticTaskBean.statisticDomain;
import static net.link.safeonline.model.bean.UsageStatisticTaskBean.statisticName;

import java.security.cert.X509Certificate;
import java.util.Date;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.interceptor.Interceptors;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.audit.AccessAuditLogger;
import net.link.safeonline.audit.AuditContextManager;
import net.link.safeonline.audit.SecurityAuditLogger;
import net.link.safeonline.authentication.exception.ApplicationIdentityNotFoundException;
import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.exception.ArgumentIntegrityException;
import net.link.safeonline.authentication.exception.DecodingException;
import net.link.safeonline.authentication.exception.ExistingUserException;
import net.link.safeonline.authentication.exception.IdentityConfirmationRequiredException;
import net.link.safeonline.authentication.exception.MissingAttributeException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.exception.SubscriptionNotFoundException;
import net.link.safeonline.authentication.service.AuthenticationService;
import net.link.safeonline.authentication.service.AuthenticationServiceRemote;
import net.link.safeonline.authentication.service.AuthenticationState;
import net.link.safeonline.authentication.service.IdentityService;
import net.link.safeonline.dao.ApplicationDAO;
import net.link.safeonline.dao.AttributeDAO;
import net.link.safeonline.dao.HistoryDAO;
import net.link.safeonline.dao.StatisticDAO;
import net.link.safeonline.dao.StatisticDataPointDAO;
import net.link.safeonline.dao.SubjectDAO;
import net.link.safeonline.dao.SubjectIdentifierDAO;
import net.link.safeonline.dao.SubscriptionDAO;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.AttributeEntity;
import net.link.safeonline.entity.StatisticDataPointEntity;
import net.link.safeonline.entity.StatisticEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.entity.SubscriptionEntity;
import net.link.safeonline.entity.audit.SecurityThreatType;
import net.link.safeonline.entity.pkix.TrustDomainEntity;
import net.link.safeonline.model.UserRegistrationManager;
import net.link.safeonline.pkix.exception.TrustDomainNotFoundException;
import net.link.safeonline.pkix.model.PkiProvider;
import net.link.safeonline.pkix.model.PkiProviderManager;
import net.link.safeonline.pkix.model.PkiValidator;
import net.link.safeonline.validation.InputValidation;
import net.link.safeonline.validation.annotation.NonEmptyString;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Implementation of authentication service interface. This component does not
 * live within the SafeOnline core security domain (chicken-egg problem).
 * 
 * @author fcorneli
 * 
 */
@Stateful
@Interceptors( { AuditContextManager.class, AccessAuditLogger.class,
		InputValidation.class })
public class AuthenticationServiceBean implements AuthenticationService,
		AuthenticationServiceRemote {

	private static final Log LOG = LogFactory
			.getLog(AuthenticationServiceBean.class);

	private SubjectEntity authenticatedSubject;

	private String authenticationDevice;

	private String expectedApplicationId;

	private AuthenticationState authenticationState;

	@PostConstruct
	public void postConstructCallback() {
		/*
		 * Set the initial state of this authentication service bean.
		 */
		this.authenticationState = INIT;
	}

	@EJB
	private SubjectDAO entityDAO;

	@EJB
	private ApplicationDAO applicationDAO;

	@EJB
	private SubscriptionDAO subscriptionDAO;

	@EJB
	private HistoryDAO historyDAO;

	@EJB
	private AttributeDAO attributeDAO;

	@EJB
	private PkiProviderManager pkiProviderManager;

	@EJB
	private PkiValidator pkiValidator;

	@EJB
	private SubjectIdentifierDAO subjectIdentifierDAO;

	@EJB
	private StatisticDAO statisticDAO;

	@EJB
	private StatisticDataPointDAO statisticDataPointDAO;

	@EJB
	private IdentityService identityService;

	@EJB
	private SecurityAuditLogger securityAuditLogger;

	@EJB
	private UserRegistrationManager userRegistrationManager;

	public boolean authenticate(@NonEmptyString
	String login, @NonEmptyString
	String password) throws SubjectNotFoundException {
		LOG.debug("authenticate \"" + login + "\"");

		SubjectEntity subject = this.entityDAO.getSubject(login);

		AttributeEntity passwordAttribute = this.attributeDAO.findAttribute(
				SafeOnlineConstants.PASSWORD_ATTRIBUTE, login);
		if (null == passwordAttribute) {
			String event = "password attribute not present for subject "
					+ login;
			addHistoryEntry(subject, event);
			throw new EJBException(event);
		}

		String expectedPassword = passwordAttribute.getStringValue();
		if (null == expectedPassword) {
			String event = "actual password is null for subject " + login;
			addHistoryEntry(subject, event);
			return false;
		}

		if (!expectedPassword.equals(password)) {
			String event = "incorrect password for subject: " + login;
			addHistoryEntry(subject, event);
			this.securityAuditLogger.addSecurityAudit(
					SecurityThreatType.DECEPTION, login, "incorrect password");
			return false;
		}

		/*
		 * Safe the state in this stateful session bean.
		 */
		this.authenticationState = USER_AUTHENTICATED;
		this.authenticatedSubject = subject;
		this.authenticationDevice = SafeOnlineConstants.USERNAME_PASSWORD_AUTH_DEVICE;
		this.expectedApplicationId = null;

		/*
		 * Communicate that the authentication process can continue.
		 */
		return true;
	}

	private void addHistoryEntry(SubjectEntity subject, String event) {
		Date now = new Date();
		this.historyDAO.addHistoryEntry(now, subject, event);
	}

	public boolean authenticate(@NonEmptyString
	String sessionId, byte[] authenticationStatementData)
			throws ArgumentIntegrityException, TrustDomainNotFoundException,
			SubjectNotFoundException, DecodingException {
		LOG.debug("authenticate session: " + sessionId);
		AuthenticationStatement authenticationStatement = new AuthenticationStatement(
				authenticationStatementData);

		X509Certificate certificate = authenticationStatement.verifyIntegrity();
		if (null == certificate) {
			throw new ArgumentIntegrityException();
		}

		String statementSessionId = authenticationStatement.getSessionId();

		PkiProvider pkiProvider = this.pkiProviderManager
				.findPkiProvider(certificate);
		if (null == pkiProvider) {
			throw new ArgumentIntegrityException();
		}
		TrustDomainEntity trustDomain = pkiProvider.getTrustDomain();
		boolean validationResult = this.pkiValidator.validateCertificate(
				trustDomain, certificate);
		if (false == validationResult) {
			throw new ArgumentIntegrityException();
		}

		if (false == sessionId.equals(statementSessionId)) {
			this.securityAuditLogger.addSecurityAudit(
					SecurityThreatType.DECEPTION, "session Id mismatch");
			throw new ArgumentIntegrityException();
		}

		String identifierDomainName = pkiProvider.getIdentifierDomainName();
		String identifier = pkiProvider.getSubjectIdentifier(certificate);
		SubjectEntity subject = this.subjectIdentifierDAO.findSubject(
				identifierDomainName, identifier);
		if (null == subject) {
			String event = "no subject was found for the given user certificate";
			LOG.warn(event);
			throw new SubjectNotFoundException();
		}
		LOG.debug("subject: " + subject);

		/*
		 * Safe the state.
		 */
		this.authenticationState = USER_AUTHENTICATED;
		this.authenticatedSubject = subject;
		this.authenticationDevice = SafeOnlineConstants.BEID_AUTH_DEVICE;
		this.expectedApplicationId = authenticationStatement.getApplicationId();

		return true;
	}

	private void addLoginTick(ApplicationEntity application) {
		StatisticEntity statistic = this.statisticDAO
				.findOrAddStatisticByNameDomainAndApplication(statisticName,
						statisticDomain, application);

		StatisticDataPointEntity dp = this.statisticDataPointDAO
				.findOrAddStatisticDataPoint(loginCounter, statistic);

		long count = dp.getX();
		dp.setX(count + 1);
	}

	@Remove
	public void abort() {
		LOG.debug("abort");
		this.authenticatedSubject = null;
		this.authenticationDevice = null;
		this.expectedApplicationId = null;
	}

	private void checkStateBeforeCommit() {
		if (INIT == this.authenticationState) {
			throw new IllegalStateException("bean is still in INIT state");
		}
	}

	private void checkRequiredIdentity(String applicationId)
			throws SubscriptionNotFoundException, ApplicationNotFoundException,
			ApplicationIdentityNotFoundException,
			IdentityConfirmationRequiredException {
		boolean confirmationRequired = this.identityService
				.isConfirmationRequired(applicationId);
		if (true == confirmationRequired) {
			throw new IdentityConfirmationRequiredException();
		}
	}

	private void checkRequiredMissingAttributes(String applicationId)
			throws ApplicationNotFoundException,
			ApplicationIdentityNotFoundException, MissingAttributeException {
		boolean hasMissingAttributes = this.identityService
				.hasMissingAttributes(applicationId);
		if (true == hasMissingAttributes) {
			throw new MissingAttributeException();
		}
	}

	@Remove
	public void commitAuthentication(@NonEmptyString
	String applicationId) throws ApplicationNotFoundException,
			SubscriptionNotFoundException,
			ApplicationIdentityNotFoundException,
			IdentityConfirmationRequiredException, MissingAttributeException {
		LOG.debug("commitAuthentication for application: " + applicationId);

		checkStateBeforeCommit();

		checkRequiredIdentity(applicationId);

		checkRequiredMissingAttributes(applicationId);

		if (null != this.expectedApplicationId) {
			/*
			 * In that case the applicationId must match. The expected
			 * application Id can be provided by authentication statements.
			 */
			if (false == this.expectedApplicationId.equals(applicationId)) {
				throw new IllegalStateException("ApplicationId does not match");
			}
		}

		ApplicationEntity application = this.applicationDAO
				.findApplication(applicationId);
		if (null == application) {
			String event = "application not found: " + applicationId;
			addHistoryEntry(this.authenticatedSubject, event);
			throw new ApplicationNotFoundException();
		}

		SubscriptionEntity subscription = this.subscriptionDAO
				.findSubscription(this.authenticatedSubject, application);
		if (null == subscription) {
			String event = "subscription not found for application: "
					+ applicationId;
			addHistoryEntry(this.authenticatedSubject, event);
			throw new SubscriptionNotFoundException();
		}

		addHistoryEntry(this.authenticatedSubject, "authenticated subject "
				+ this.authenticatedSubject + " for application "
				+ applicationId + " via " + this.authenticationDevice);

		this.subscriptionDAO.loggedIn(subscription);
		this.addLoginTick(application);
	}

	public String getUserId() {
		LOG.debug("getUserId");
		if (INIT == this.authenticationState) {
			throw new IllegalStateException("call authenticate first");
		}
		String userId = this.authenticatedSubject.getLogin();
		return userId;
	}

	public boolean registerAndAuthenticate(String sessionId, String username,
			byte[] registrationStatementData)
			throws ArgumentIntegrityException, TrustDomainNotFoundException,
			DecodingException, ExistingUserException {
		LOG.debug("registerAndAuthentication: " + username);
		RegistrationStatement registrationStatement = new RegistrationStatement(
				registrationStatementData);

		X509Certificate certificate = registrationStatement.verifyIntegrity();
		if (null == certificate) {
			throw new ArgumentIntegrityException();
		}

		PkiProvider pkiProvider = this.pkiProviderManager
				.findPkiProvider(certificate);
		if (null == pkiProvider) {
			throw new ArgumentIntegrityException();
		}
		TrustDomainEntity trustDomain = pkiProvider.getTrustDomain();
		boolean validationResult = this.pkiValidator.validateCertificate(
				trustDomain, certificate);
		if (false == validationResult) {
			throw new ArgumentIntegrityException();
		}

		String statementSessionId = registrationStatement.getSessionId();
		if (false == sessionId.equals(statementSessionId)) {
			this.securityAuditLogger.addSecurityAudit(
					SecurityThreatType.DECEPTION, "session Id mismatch");
			throw new ArgumentIntegrityException();
		}

		String statementUsername = registrationStatement.getUsername();
		if (false == username.equals(statementUsername)) {
			this.securityAuditLogger.addSecurityAudit(
					SecurityThreatType.DECEPTION, "username mismatch");
			throw new ArgumentIntegrityException();
		}

		String domain = pkiProvider.getIdentifierDomainName();
		String identifier = pkiProvider.getSubjectIdentifier(certificate);
		SubjectEntity existingMappedSubject = this.subjectIdentifierDAO
				.findSubject(domain, identifier);
		if (null != existingMappedSubject) {
			throw new ArgumentIntegrityException();
		}

		SubjectEntity subject = this.userRegistrationManager
				.registerUser(username);
		this.subjectIdentifierDAO.addSubjectIdentifier(domain, identifier,
				subject);

		pkiProvider.storeAdditionalAttributes(subject, certificate);

		/*
		 * Safe the state.
		 */
		this.authenticationState = USER_AUTHENTICATED;
		this.authenticatedSubject = subject;
		this.authenticationDevice = SafeOnlineConstants.BEID_AUTH_DEVICE;
		this.expectedApplicationId = registrationStatement.getApplicationId();
		return false;
	}
}
