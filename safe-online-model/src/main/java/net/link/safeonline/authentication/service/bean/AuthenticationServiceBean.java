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
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.interceptor.Interceptors;

import net.link.safeonline.audit.AccessAuditLogger;
import net.link.safeonline.audit.AuditContextManager;
import net.link.safeonline.audit.SecurityAuditLogger;
import net.link.safeonline.authentication.exception.ApplicationIdentityNotFoundException;
import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.exception.ArgumentIntegrityException;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.DecodingException;
import net.link.safeonline.authentication.exception.DeviceNotFoundException;
import net.link.safeonline.authentication.exception.DevicePolicyException;
import net.link.safeonline.authentication.exception.EmptyDevicePolicyException;
import net.link.safeonline.authentication.exception.ExistingUserException;
import net.link.safeonline.authentication.exception.IdentityConfirmationRequiredException;
import net.link.safeonline.authentication.exception.MissingAttributeException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.exception.SubscriptionNotFoundException;
import net.link.safeonline.authentication.service.AuthenticationDevice;
import net.link.safeonline.authentication.service.AuthenticationService;
import net.link.safeonline.authentication.service.AuthenticationServiceRemote;
import net.link.safeonline.authentication.service.AuthenticationState;
import net.link.safeonline.authentication.service.CredentialService;
import net.link.safeonline.authentication.service.DevicePolicyService;
import net.link.safeonline.authentication.service.IdentityService;
import net.link.safeonline.authentication.service.PasswordManager;
import net.link.safeonline.dao.ApplicationDAO;
import net.link.safeonline.dao.HistoryDAO;
import net.link.safeonline.dao.StatisticDAO;
import net.link.safeonline.dao.StatisticDataPointDAO;
import net.link.safeonline.dao.SubjectIdentifierDAO;
import net.link.safeonline.dao.SubscriptionDAO;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.HistoryEventType;
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
import net.link.safeonline.service.SubjectService;
import net.link.safeonline.validation.InputValidation;
import net.link.safeonline.validation.annotation.NonEmptyString;
import net.link.safeonline.validation.annotation.NotNull;

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

	private AuthenticationDevice authenticationDevice;

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
	private SubjectService subjectService;

	@EJB
	private ApplicationDAO applicationDAO;

	@EJB
	private SubscriptionDAO subscriptionDAO;

	@EJB
	private HistoryDAO historyDAO;

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

	@EJB
	private PasswordManager passwordManager;

	@EJB
	private DevicePolicyService devicePolicyService;

	@EJB
	private CredentialService credentialService;

	public boolean authenticate(@NonEmptyString
	String login, @NonEmptyString
	String password) throws SubjectNotFoundException, DeviceNotFoundException {
		LOG.debug("authenticate \"" + login + "\"");

		SubjectEntity subject = this.subjectService
				.getSubjectFromUserName(login);

		boolean validationResult = false;

		try {
			validationResult = this.passwordManager.validatePassword(subject,
					password);
		} catch (DeviceNotFoundException e) {
			addHistoryEntry(subject,
					HistoryEventType.LOGIN_PASSWORD_ATTRIBUTE_NOT_FOUND, null,
					null);
			throw e;
		}

		if (!validationResult) {
			addHistoryEntry(subject, HistoryEventType.LOGIN_INCORRECT_PASSWORD,
					null, null);
			this.securityAuditLogger.addSecurityAudit(
					SecurityThreatType.DECEPTION, subject.getUserId(),
					"incorrect password");
			return false;
		}

		/*
		 * Safe the state in this stateful session bean.
		 */
		this.authenticationState = USER_AUTHENTICATED;
		this.authenticatedSubject = subject;
		this.authenticationDevice = AuthenticationDevice.PASSWORD;
		this.expectedApplicationId = null;

		/*
		 * Communicate that the authentication process can continue.
		 */
		return true;
	}

	private void addHistoryEntry(SubjectEntity subject, HistoryEventType event,
			String application, String info) {
		Date now = new Date();
		this.historyDAO.addHistoryEntry(now, subject, event, application, info);
	}

	public boolean authenticate(@NonEmptyString
	String sessionId, @NotNull
	byte[] authenticationStatementData) throws ArgumentIntegrityException,
			TrustDomainNotFoundException, SubjectNotFoundException,
			DecodingException {
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
		this.authenticationDevice = AuthenticationDevice.BEID;
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

	private void checkDevicePolicy(String applicationId,
			Set<AuthenticationDevice> requiredDevicePolicy)
			throws ApplicationNotFoundException, EmptyDevicePolicyException,
			DevicePolicyException {
		LOG.debug("authenticationDevice: " + this.authenticationDevice);
		Set<AuthenticationDevice> devicePolicy = this.devicePolicyService
				.getDevicePolicy(applicationId, requiredDevicePolicy);
		for (AuthenticationDevice device : devicePolicy)
			LOG.debug("devicePolicy: " + device.getDeviceName());
		boolean devicePolicyCheck = devicePolicy
				.contains(this.authenticationDevice);
		if (!devicePolicyCheck)
			throw new DevicePolicyException();
	}

	@Remove
	public void commitAuthentication(@NonEmptyString
	String applicationId, Set<AuthenticationDevice> requiredDevicePolicy)
			throws ApplicationNotFoundException, SubscriptionNotFoundException,
			ApplicationIdentityNotFoundException,
			IdentityConfirmationRequiredException, MissingAttributeException,
			EmptyDevicePolicyException, DevicePolicyException {
		LOG.debug("commitAuthentication for application: " + applicationId);

		checkStateBeforeCommit();

		checkRequiredIdentity(applicationId);

		checkRequiredMissingAttributes(applicationId);

		checkDevicePolicy(applicationId, requiredDevicePolicy);

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
			addHistoryEntry(this.authenticatedSubject,
					HistoryEventType.LOGIN_APPLICATION_NOT_FOUND,
					applicationId, null);
			throw new ApplicationNotFoundException();
		}

		SubscriptionEntity subscription = this.subscriptionDAO
				.findSubscription(this.authenticatedSubject, application);
		if (null == subscription) {
			addHistoryEntry(this.authenticatedSubject,
					HistoryEventType.SUBSCRIPTION_NOT_FOUND, applicationId,
					null);
			throw new SubscriptionNotFoundException();
		}

		addHistoryEntry(this.authenticatedSubject,
				HistoryEventType.LOGIN_SUCCESS, applicationId,
				this.authenticationDevice.getDeviceName());

		this.subscriptionDAO.loggedIn(subscription);
		this.addLoginTick(application);
	}

	public String getUserId() {
		LOG.debug("getUserId");
		if (INIT == this.authenticationState) {
			throw new IllegalStateException("call authenticate first");
		}
		String userId = this.authenticatedSubject.getUserId();
		return userId;
	}

	public String getUsername() {
		String userId = getUserId();
		return this.subjectService.getSubjectLogin(userId);
	}

	public boolean registerDevice(@NotNull
	byte[] identityStatementData) throws TrustDomainNotFoundException,
			PermissionDeniedException, ArgumentIntegrityException,
			AttributeTypeNotFoundException {
		this.credentialService.mergeIdentityStatement(identityStatementData);

		this.authenticationDevice = AuthenticationDevice.BEID;
		return true;
	}

	public boolean registerAndAuthenticate(@NonEmptyString
	String sessionId, @NonEmptyString
	String username, @NotNull
	byte[] registrationStatementData) throws ArgumentIntegrityException,
			TrustDomainNotFoundException, DecodingException,
			ExistingUserException, AttributeTypeNotFoundException {
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
		this.authenticationDevice = AuthenticationDevice.BEID;
		this.expectedApplicationId = registrationStatement.getApplicationId();

		addHistoryEntry(this.authenticatedSubject,
				HistoryEventType.LOGIN_REGISTRATION,
				this.expectedApplicationId, this.authenticationDevice
						.getDeviceName());

		return false;
	}

	public AuthenticationDevice getAuthenticationDevice() {
		return this.authenticationDevice;
	}
}
