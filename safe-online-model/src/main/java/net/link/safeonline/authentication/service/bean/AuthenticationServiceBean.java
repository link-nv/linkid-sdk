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

import java.net.MalformedURLException;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.interceptor.Interceptors;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.audit.AccessAuditLogger;
import net.link.safeonline.audit.AuditContextManager;
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
import net.link.safeonline.authentication.exception.MobileAuthenticationException;
import net.link.safeonline.authentication.exception.MobileException;
import net.link.safeonline.authentication.exception.MobileRegistrationException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.exception.SubscriptionNotFoundException;
import net.link.safeonline.authentication.exception.UsageAgreementAcceptationRequiredException;
import net.link.safeonline.authentication.service.AuthenticationService;
import net.link.safeonline.authentication.service.AuthenticationServiceRemote;
import net.link.safeonline.authentication.service.AuthenticationState;
import net.link.safeonline.authentication.service.DevicePolicyService;
import net.link.safeonline.authentication.service.IdentityService;
import net.link.safeonline.authentication.service.UsageAgreementService;
import net.link.safeonline.dao.ApplicationDAO;
import net.link.safeonline.dao.DeviceDAO;
import net.link.safeonline.dao.HistoryDAO;
import net.link.safeonline.dao.StatisticDAO;
import net.link.safeonline.dao.StatisticDataPointDAO;
import net.link.safeonline.dao.SubscriptionDAO;
import net.link.safeonline.device.BeIdDeviceService;
import net.link.safeonline.device.PasswordDeviceService;
import net.link.safeonline.device.WeakMobileDeviceService;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.DeviceEntity;
import net.link.safeonline.entity.HistoryEventType;
import net.link.safeonline.entity.StatisticDataPointEntity;
import net.link.safeonline.entity.StatisticEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.entity.SubscriptionEntity;
import net.link.safeonline.model.SubjectManager;
import net.link.safeonline.pkix.exception.TrustDomainNotFoundException;
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

	private DeviceEntity authenticationDevice;

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
	private SubjectManager subjectManager;

	@EJB
	private ApplicationDAO applicationDAO;

	@EJB
	private SubscriptionDAO subscriptionDAO;

	@EJB
	private HistoryDAO historyDAO;

	@EJB
	private StatisticDAO statisticDAO;

	@EJB
	private DeviceDAO deviceDAO;

	@EJB
	private StatisticDataPointDAO statisticDataPointDAO;

	@EJB
	private IdentityService identityService;

	@EJB
	private DevicePolicyService devicePolicyService;

	@EJB
	private UsageAgreementService usageAgreementService;

	@EJB
	private PasswordDeviceService passwordDeviceService;

	@EJB
	private BeIdDeviceService beIdDeviceService;

	@EJB
	private WeakMobileDeviceService weakMobileDeviceService;

	public boolean authenticate(@NonEmptyString
	String login, @NonEmptyString
	String password) throws SubjectNotFoundException, DeviceNotFoundException {
		SubjectEntity subject = this.passwordDeviceService.authenticate(login,
				password);
		if (null == subject)
			return false;
		DeviceEntity device = this.deviceDAO
				.getDevice(SafeOnlineConstants.USERNAME_PASSWORD_DEVICE_ID);

		/*
		 * Safe the state in this stateful session bean.
		 */
		this.authenticationState = USER_AUTHENTICATED;
		this.authenticatedSubject = subject;
		this.authenticationDevice = device;
		this.expectedApplicationId = null;

		/*
		 * Communicate that the authentication process can continue.
		 */
		return true;
	}

	public String authenticate(@NotNull
	DeviceEntity device, @NonEmptyString
	String mobile, @NonEmptyString
	String challengeId, @NonEmptyString
	String mobileOTP) throws SubjectNotFoundException, MalformedURLException,
			MobileException, MobileAuthenticationException {
		SubjectEntity subject = this.weakMobileDeviceService.authenticate(
				mobile, challengeId, mobileOTP);
		/*
		 * Safe the state in this stateful session bean.
		 */
		this.authenticationState = USER_AUTHENTICATED;
		this.authenticatedSubject = subject;
		this.authenticationDevice = device;
		this.expectedApplicationId = null;

		/*
		 * Communicate that the authentication process can continue.
		 */
		return this.subjectService.getSubjectLogin(subject.getUserId());
	}

	public String requestMobileOTP(@NonEmptyString
	String mobile) throws MalformedURLException, MobileException {
		return this.weakMobileDeviceService.requestOTP(mobile);
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
		AuthenticationStatement authenticationStatement = new AuthenticationStatement(
				authenticationStatementData);
		SubjectEntity subject = this.beIdDeviceService.authenticate(sessionId,
				authenticationStatement);
		if (null == subject)
			return false;
		DeviceEntity device = this.deviceDAO
				.findDevice(SafeOnlineConstants.BEID_DEVICE_ID);

		/*
		 * Safe the state.
		 */
		this.authenticationState = USER_AUTHENTICATED;
		this.authenticatedSubject = subject;
		this.authenticationDevice = device;
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
			Set<DeviceEntity> requiredDevicePolicy)
			throws ApplicationNotFoundException, EmptyDevicePolicyException,
			DevicePolicyException {
		LOG.debug("authenticationDevice: " + this.authenticationDevice);
		List<DeviceEntity> devicePolicy = this.devicePolicyService
				.getDevicePolicy(applicationId, requiredDevicePolicy);
		boolean found = false;
		for (DeviceEntity device : devicePolicy) {
			LOG.debug("devicePolicy: " + device.getName());
			if (device.getName().equals(this.authenticationDevice.getName())) {
				found = true;
				break;
			}
		}
		if (!found)
			throw new DevicePolicyException();
	}

	private void checkRequiredUsageAgreement(String applicationId)
			throws ApplicationNotFoundException,
			UsageAgreementAcceptationRequiredException,
			SubscriptionNotFoundException {
		boolean requiresUsageAgreementAcceptation = this.usageAgreementService
				.requiresUsageAgreementAcceptation(applicationId);
		if (true == requiresUsageAgreementAcceptation)
			throw new UsageAgreementAcceptationRequiredException();
	}

	private void checkRequiredGlobalUsageAgreement()
			throws UsageAgreementAcceptationRequiredException {
		boolean requiresGlobalUsageAgreementAcceptation = this.usageAgreementService
				.requiresGlobalUsageAgreementAcceptation();
		if (true == requiresGlobalUsageAgreementAcceptation)
			throw new UsageAgreementAcceptationRequiredException();
	}

	@Remove
	public void commitAuthentication(@NonEmptyString
	String applicationId, Set<DeviceEntity> requiredDevicePolicy)
			throws ApplicationNotFoundException, SubscriptionNotFoundException,
			ApplicationIdentityNotFoundException,
			IdentityConfirmationRequiredException, MissingAttributeException,
			EmptyDevicePolicyException, DevicePolicyException,
			UsageAgreementAcceptationRequiredException {
		LOG.debug("commitAuthentication for application: " + applicationId);

		checkStateBeforeCommit();

		checkRequiredIdentity(applicationId);

		checkRequiredMissingAttributes(applicationId);

		checkDevicePolicy(applicationId, requiredDevicePolicy);

		checkRequiredGlobalUsageAgreement();

		checkRequiredUsageAgreement(applicationId);

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
				this.authenticationDevice.getName());

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
		this.beIdDeviceService.register(identityStatementData);
		DeviceEntity device = this.deviceDAO
				.findDevice(SafeOnlineConstants.BEID_DEVICE_ID);
		this.authenticationDevice = device;
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

		SubjectEntity subject = this.beIdDeviceService.registerAndAuthenticate(
				sessionId, username, registrationStatement);
		DeviceEntity device = this.deviceDAO
				.findDevice(SafeOnlineConstants.BEID_DEVICE_ID);

		/*
		 * Safe the state.
		 */
		this.authenticationState = USER_AUTHENTICATED;
		this.authenticatedSubject = subject;
		this.authenticationDevice = device;
		this.expectedApplicationId = registrationStatement.getApplicationId();

		addHistoryEntry(this.authenticatedSubject,
				HistoryEventType.LOGIN_REGISTRATION,
				this.expectedApplicationId, this.authenticationDevice.getName());

		return false;
	}

	public String registerMobile(String mobile) throws MobileException,
			MalformedURLException, MobileRegistrationException,
			ArgumentIntegrityException {
		LOG.debug("register mobile: " + mobile);
		SubjectEntity subject = this.subjectManager.getCallerSubject();
		String activationCode = this.weakMobileDeviceService.register(subject,
				mobile);
		DeviceEntity device = this.deviceDAO
				.findDevice(SafeOnlineConstants.ENCAP_DEVICE_ID);

		this.authenticationDevice = device;
		return activationCode;
	}

	public void removeMobile(String mobile) throws MobileException,
			MalformedURLException {
		SubjectEntity subject = this.subjectManager.getCallerSubject();
		this.weakMobileDeviceService.remove(subject, mobile);
		this.authenticationDevice = null;
	}

	public void setPassword(String password) throws PermissionDeniedException {
		LOG.debug("set password");
		SubjectEntity subject = this.subjectManager.getCallerSubject();
		this.passwordDeviceService.register(subject, password);
		DeviceEntity device = this.deviceDAO
				.findDevice(SafeOnlineConstants.USERNAME_PASSWORD_DEVICE_ID);

		this.authenticationDevice = device;
	}

}
