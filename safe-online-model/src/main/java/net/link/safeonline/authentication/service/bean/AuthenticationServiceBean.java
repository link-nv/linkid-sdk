/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.authentication.service.bean;

import static net.link.safeonline.authentication.service.AuthenticationState.COMMITTED;
import static net.link.safeonline.authentication.service.AuthenticationState.INIT;
import static net.link.safeonline.authentication.service.AuthenticationState.INITIALIZED;
import static net.link.safeonline.authentication.service.AuthenticationState.REDIRECTED;
import static net.link.safeonline.authentication.service.AuthenticationState.USER_AUTHENTICATED;
import static net.link.safeonline.model.bean.UsageStatisticTaskBean.loginCounter;
import static net.link.safeonline.model.bean.UsageStatisticTaskBean.statisticDomain;
import static net.link.safeonline.model.bean.UsageStatisticTaskBean.statisticName;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.interceptor.Interceptors;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.audit.AccessAuditLogger;
import net.link.safeonline.audit.AuditContextManager;
import net.link.safeonline.authentication.exception.ApplicationIdentityNotFoundException;
import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.AuthenticationInitializationException;
import net.link.safeonline.authentication.exception.DeviceMappingNotFoundException;
import net.link.safeonline.authentication.exception.DeviceNotFoundException;
import net.link.safeonline.authentication.exception.DevicePolicyException;
import net.link.safeonline.authentication.exception.EmptyDevicePolicyException;
import net.link.safeonline.authentication.exception.IdentityConfirmationRequiredException;
import net.link.safeonline.authentication.exception.MissingAttributeException;
import net.link.safeonline.authentication.exception.NodeNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.exception.SubscriptionNotFoundException;
import net.link.safeonline.authentication.exception.UsageAgreementAcceptationRequiredException;
import net.link.safeonline.authentication.service.ApplicationAuthenticationService;
import net.link.safeonline.authentication.service.AuthenticationService;
import net.link.safeonline.authentication.service.AuthenticationServiceRemote;
import net.link.safeonline.authentication.service.AuthenticationState;
import net.link.safeonline.authentication.service.DevicePolicyService;
import net.link.safeonline.authentication.service.IdentityService;
import net.link.safeonline.authentication.service.NodeAuthenticationService;
import net.link.safeonline.authentication.service.SamlAuthorityService;
import net.link.safeonline.authentication.service.UsageAgreementService;
import net.link.safeonline.authentication.service.UserIdMappingService;
import net.link.safeonline.dao.ApplicationDAO;
import net.link.safeonline.dao.DeviceDAO;
import net.link.safeonline.dao.HistoryDAO;
import net.link.safeonline.dao.StatisticDAO;
import net.link.safeonline.dao.StatisticDataPointDAO;
import net.link.safeonline.dao.SubscriptionDAO;
import net.link.safeonline.device.PasswordDeviceService;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.DeviceEntity;
import net.link.safeonline.entity.DeviceMappingEntity;
import net.link.safeonline.entity.HistoryEventType;
import net.link.safeonline.entity.OlasEntity;
import net.link.safeonline.entity.StatisticDataPointEntity;
import net.link.safeonline.entity.StatisticEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.entity.SubscriptionEntity;
import net.link.safeonline.pkix.exception.TrustDomainNotFoundException;
import net.link.safeonline.pkix.model.PkiValidator;
import net.link.safeonline.pkix.model.PkiValidator.PkiResult;
import net.link.safeonline.sdk.auth.saml2.AuthnRequestFactory;
import net.link.safeonline.sdk.auth.saml2.AuthnResponseFactory;
import net.link.safeonline.sdk.auth.saml2.AuthnResponseUtil;
import net.link.safeonline.sdk.auth.saml2.Challenge;
import net.link.safeonline.sdk.auth.saml2.DeviceOperationType;
import net.link.safeonline.sdk.ws.sts.TrustDomainType;
import net.link.safeonline.service.DeviceMappingService;
import net.link.safeonline.service.SubjectService;
import net.link.safeonline.util.ee.AuthIdentityServiceClient;
import net.link.safeonline.util.ee.IdentityServiceClient;
import net.link.safeonline.validation.InputValidation;
import net.link.safeonline.validation.annotation.NonEmptyString;
import net.link.safeonline.validation.annotation.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xml.security.utils.Base64;
import org.joda.time.DateTime;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml2.core.AuthnRequest;
import org.opensaml.saml2.core.AuthnStatement;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.saml2.core.NameID;
import org.opensaml.saml2.core.RequestedAuthnContext;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.core.StatusCode;
import org.opensaml.saml2.core.Subject;
import org.opensaml.xml.security.x509.BasicX509Credential;
import org.opensaml.xml.signature.SignatureValidator;
import org.opensaml.xml.validation.ValidationException;

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

    private static final Log    LOG = LogFactory
                                            .getLog(AuthenticationServiceBean.class);

    private SubjectEntity       authenticatedSubject;

    private DeviceEntity        authenticationDevice;

    private String              expectedApplicationId;

    private String              expectedApplicationFriendlyName;

    private String              expectedChallengeId;

    private String              expectedDeviceChallengeId;

    private String              expectedTarget;

    private Set<DeviceEntity>   requiredDevicePolicy;

    private AuthenticationState authenticationState;


    @PostConstruct
    public void postConstructCallback() {

        /*
         * Set the initial state of this authentication service bean.
         */
        this.authenticationState = INIT;
    }


    @EJB
    private SubjectService                   subjectService;

    @EJB
    private ApplicationDAO                   applicationDAO;

    @EJB
    private SubscriptionDAO                  subscriptionDAO;

    @EJB
    private HistoryDAO                       historyDAO;

    @EJB
    private StatisticDAO                     statisticDAO;

    @EJB
    private DeviceDAO                        deviceDAO;

    @EJB
    private StatisticDataPointDAO            statisticDataPointDAO;

    @EJB
    private IdentityService                  identityService;

    @EJB
    private DevicePolicyService              devicePolicyService;

    @EJB
    private DeviceMappingService             deviceMappingService;

    @EJB
    private UsageAgreementService            usageAgreementService;

    @EJB
    private PasswordDeviceService            passwordDeviceService;

    @EJB
    private NodeAuthenticationService        nodeAuthenticationService;

    @EJB
    private ApplicationAuthenticationService applicationAuthenticationService;

    @EJB
    private PkiValidator                     pkiValidator;

    @EJB
    private SamlAuthorityService             samlAuthorityService;

    @EJB
    private UserIdMappingService             userIdMappingService;


    public void initialize(@NotNull AuthnRequest samlAuthnRequest)
            throws AuthenticationInitializationException,
            ApplicationNotFoundException, TrustDomainNotFoundException {

        Issuer issuer = samlAuthnRequest.getIssuer();
        String issuerName = issuer.getValue();
        LOG.debug("issuer: " + issuerName);

        List<X509Certificate> certificates = this.applicationAuthenticationService
                .getCertificates(issuerName);

        boolean validSignature = false;
        for (X509Certificate certificate : certificates) {
            validSignature = validateSignature(certificate, samlAuthnRequest);
            if (validSignature) {
                break;
            }
        }
        if (!validSignature) {
            throw new AuthenticationInitializationException(
                    "signature validation error");
        }

        String assertionConsumerService = samlAuthnRequest
                .getAssertionConsumerServiceURL();
        if (null == assertionConsumerService) {
            LOG.debug("missing AssertionConsumerServiceURL");
            throw new AuthenticationInitializationException(
                    "missing AssertionConsumerServiceURL");
        }

        String applicationFriendlyName = samlAuthnRequest.getProviderName();
        if (null == applicationFriendlyName) {
            LOG.debug("missing ProviderName");
            throw new AuthenticationInitializationException(
                    "missing ProviderName");
        }

        String samlAuthnRequestId = samlAuthnRequest.getID();
        LOG.debug("SAML authn request ID: " + samlAuthnRequestId);

        RequestedAuthnContext requestedAuthnContext = samlAuthnRequest
                .getRequestedAuthnContext();
        Set<DeviceEntity> devices;
        if (null != requestedAuthnContext) {
            List<AuthnContextClassRef> authnContextClassRefs = requestedAuthnContext
                    .getAuthnContextClassRefs();
            devices = new HashSet<DeviceEntity>();
            for (AuthnContextClassRef authnContextClassRef : authnContextClassRefs) {
                String authnContextClassRefValue = authnContextClassRef
                        .getAuthnContextClassRef();
                LOG.debug("authentication context class reference: "
                        + authnContextClassRefValue);
                List<DeviceEntity> authnDevices = this.devicePolicyService
                        .listDevices(authnContextClassRefValue);
                if (null == authnDevices || authnDevices.size() == 0) {
                    LOG.error("AuthnContextClassRef not supported: "
                            + authnContextClassRefValue);
                    throw new AuthenticationInitializationException(
                            "AuthnContextClassRef not supported: "
                                    + authnContextClassRefValue);
                }
                devices.addAll(authnDevices);
            }
        } else {
            devices = null;
        }
        /*
         * Safe the state in this stateful session bean.
         */
        this.authenticationState = INITIALIZED;
        this.expectedApplicationId = issuerName;
        this.expectedApplicationFriendlyName = applicationFriendlyName;
        this.requiredDevicePolicy = devices;
        this.expectedTarget = assertionConsumerService;
        this.expectedChallengeId = samlAuthnRequestId;

    }

    private boolean validateSignature(X509Certificate certificate,
            AuthnRequest samlAuthnRequest) throws TrustDomainNotFoundException {

        PkiResult certificateValid = this.pkiValidator.validateCertificate(
                SafeOnlineConstants.SAFE_ONLINE_APPLICATIONS_TRUST_DOMAIN,
                certificate);

        if (PkiResult.VALID != certificateValid) {
            return false;
        }

        BasicX509Credential basicX509Credential = new BasicX509Credential();
        basicX509Credential.setPublicKey(certificate.getPublicKey());
        SignatureValidator signatureValidator = new SignatureValidator(
                basicX509Credential);
        try {
            signatureValidator.validate(samlAuthnRequest.getSignature());
        } catch (ValidationException e) {
            return false;
        }
        return true;
    }

    public String redirectAuthentication(
            @NonEmptyString String authenticationServiceUrl,
            @NonEmptyString String targetUrl, @NonEmptyString String device)
            throws NodeNotFoundException {

        /*
         * Also allow redirected state in case the user manually goes back to
         * olas-auth
         */
        if (this.authenticationState != INITIALIZED
                && this.authenticationState != REDIRECTED) {
            throw new IllegalStateException("call initialize first");
        }

        IdentityServiceClient identityServiceClient = new IdentityServiceClient();
        PrivateKey privateKey = identityServiceClient.getPrivateKey();
        PublicKey publicKey = identityServiceClient.getPublicKey();
        KeyPair keyPair = new KeyPair(publicKey, privateKey);

        OlasEntity node = this.nodeAuthenticationService.getLocalNode();

        Set<String> devices = Collections.singleton(device);

        Challenge<String> challenge = new Challenge<String>();

        String samlRequestToken = AuthnRequestFactory.createAuthnRequest(node
                .getName(), this.expectedApplicationId,
                this.expectedApplicationFriendlyName, keyPair,
                authenticationServiceUrl, targetUrl, challenge, devices);

        String encodedSamlRequestToken = Base64.encode(samlRequestToken
                .getBytes());

        /*
         * Safe the state in this stateful session bean.
         */
        this.authenticationState = REDIRECTED;
        this.expectedDeviceChallengeId = challenge.getValue();

        return encodedSamlRequestToken;
    }

    public String redirectRegistration(
            @NonEmptyString String registrationServiceUrl,
            @NonEmptyString String targetUrl, @NonEmptyString String device,
            @NonEmptyString String username) throws NodeNotFoundException,
            SubjectNotFoundException, DeviceNotFoundException {

        /*
         * Also allow redirected state in case the user manually goes back to
         * olas-auth
         */
        if (this.authenticationState != INITIALIZED
                && this.authenticationState != USER_AUTHENTICATED
                && this.authenticationState != REDIRECTED) {
            throw new IllegalStateException(
                    "call initialize or authenticate first");
        }

        IdentityServiceClient identityServiceClient = new IdentityServiceClient();
        PrivateKey privateKey = identityServiceClient.getPrivateKey();
        PublicKey publicKey = identityServiceClient.getPublicKey();
        KeyPair keyPair = new KeyPair(publicKey, privateKey);

        OlasEntity node = this.nodeAuthenticationService.getLocalNode();

        Challenge<String> challenge = new Challenge<String>();

        DeviceMappingEntity deviceMapping = this.deviceMappingService
                .getDeviceMapping(username, device);

        String samlRequestToken = AuthnRequestFactory
                .createDeviceOperationAuthnRequest(node.getName(),
                        deviceMapping.getId(), keyPair, registrationServiceUrl,
                        targetUrl, DeviceOperationType.REGISTER, challenge,
                        device);

        String encodedSamlRequestToken = Base64.encode(samlRequestToken
                .getBytes());

        /*
         * Safe the state in this stateful session bean.
         */
        this.authenticationState = REDIRECTED;
        this.expectedDeviceChallengeId = challenge.getValue();

        return encodedSamlRequestToken;
    }

    public DeviceMappingEntity authenticate(@NotNull HttpServletRequest request)
            throws NodeNotFoundException, ServletException,
            DeviceMappingNotFoundException {

        LOG.debug("authenticate");
        if (this.authenticationState != REDIRECTED) {
            throw new IllegalStateException("call redirect first");
        }

        DateTime now = new DateTime();

        AuthIdentityServiceClient authIdentityServiceClient = new AuthIdentityServiceClient();
        OlasEntity node = this.nodeAuthenticationService.getLocalNode();

        Response samlResponse = AuthnResponseUtil.validateResponse(now,
                request, this.expectedDeviceChallengeId,
                this.expectedApplicationId, node.getLocation(),
                authIdentityServiceClient.getCertificate(),
                authIdentityServiceClient.getPrivateKey(),
                TrustDomainType.DEVICE);
        if (null == samlResponse)
            return null;

        if (samlResponse.getStatus().getStatusCode().getValue().equals(
                StatusCode.UNKNOWN_PRINCIPAL_URI)) {
            /*
             * Authentication failed, user wants to try another device tho. Set
             * the state to redirected to mark this
             */
            this.authenticationState = REDIRECTED;
            return null;
        } else if (samlResponse.getStatus().getStatusCode().getValue().equals(
                StatusCode.AUTHN_FAILED_URI)) {
            /*
             * Authentication failed, reset the state
             */
            if (null == this.authenticatedSubject) {
                this.authenticationState = INITIALIZED;
            } else {
                this.authenticationState = USER_AUTHENTICATED;
            }
            return null;
        }

        Assertion assertion = samlResponse.getAssertions().get(0);
        List<AuthnStatement> authStatements = assertion.getAuthnStatements();
        if (authStatements.isEmpty()) {
            throw new ServletException("missing authentication statement");
        }

        AuthnStatement authStatement = authStatements.get(0);
        if (null == authStatement.getAuthnContext())
            throw new ServletException(
                    "missing authentication context in authentication statement");

        AuthnContextClassRef authnContextClassRef = authStatement
                .getAuthnContext().getAuthnContextClassRef();
        String authenticatedDevice = authnContextClassRef
                .getAuthnContextClassRef();
        LOG.debug("authenticated device: " + authenticatedDevice);

        Subject subject = assertion.getSubject();
        NameID subjectName = subject.getNameID();
        String subjectNameValue = subjectName.getValue();
        LOG.debug("subject name value: " + subjectNameValue);

        DeviceMappingEntity deviceMapping = this.deviceMappingService
                .getDeviceMapping(subjectNameValue);

        /*
         * Safe the state in this stateful session bean.
         */
        this.authenticationState = USER_AUTHENTICATED;
        this.authenticatedSubject = deviceMapping.getSubject();
        this.authenticationDevice = deviceMapping.getDevice();
        this.expectedDeviceChallengeId = null;

        return deviceMapping;
    }

    public boolean authenticate(@NonEmptyString String loginName,
            @NonEmptyString String password) throws SubjectNotFoundException,
            DeviceNotFoundException {

        SubjectEntity subject = this.passwordDeviceService.authenticate(
                loginName, password);
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

        /*
         * Communicate that the authentication process can continue.
         */
        return true;
    }

    public DeviceMappingEntity register(@NotNull HttpServletRequest request)
            throws NodeNotFoundException, ServletException,
            DeviceMappingNotFoundException {

        LOG.debug("register");
        if (this.authenticationState != REDIRECTED) {
            throw new IllegalStateException("call redirect first");
        }

        DateTime now = new DateTime();

        AuthIdentityServiceClient authIdentityServiceClient = new AuthIdentityServiceClient();
        OlasEntity node = this.nodeAuthenticationService.getLocalNode();

        Response samlResponse = AuthnResponseUtil.validateResponse(now,
                request, this.expectedDeviceChallengeId,
                DeviceOperationType.REGISTER.name(), node.getLocation(),
                authIdentityServiceClient.getCertificate(),
                authIdentityServiceClient.getPrivateKey(),
                TrustDomainType.DEVICE);
        if (null == samlResponse)
            return null;

        if (samlResponse.getStatus().getStatusCode().getValue().equals(
                StatusCode.AUTHN_FAILED_URI)) {

            /*
             * Registration failed, reset the state
             */
            this.authenticationState = INITIALIZED;
            this.expectedDeviceChallengeId = null;
            return null;
        } else if (samlResponse.getStatus().getStatusCode().getValue().equals(
                StatusCode.REQUEST_UNSUPPORTED_URI)) {
            // TODO: add security audit
            /*
             * Registration not supported by this device, reset the state
             */
            this.authenticationState = INITIALIZED;
            this.expectedDeviceChallengeId = null;
            return null;
        }

        Assertion assertion = samlResponse.getAssertions().get(0);
        List<AuthnStatement> authStatements = assertion.getAuthnStatements();
        if (authStatements.isEmpty()) {
            throw new ServletException("missing authentication statement");
        }

        AuthnStatement authStatement = authStatements.get(0);
        if (null == authStatement.getAuthnContext())
            throw new ServletException(
                    "missing authentication context in authentication statement");

        AuthnContextClassRef authnContextClassRef = authStatement
                .getAuthnContext().getAuthnContextClassRef();
        String authenticatedDevice = authnContextClassRef
                .getAuthnContextClassRef();
        LOG.debug("authenticated device: " + authenticatedDevice);

        Subject subject = assertion.getSubject();
        NameID subjectName = subject.getNameID();
        String subjectNameValue = subjectName.getValue();
        LOG.debug("subject name value: " + subjectNameValue);

        /**
         * Check if this device mapping truly exists.
         */
        DeviceMappingEntity deviceMapping = this.deviceMappingService
                .getDeviceMapping(subjectNameValue);

        /*
         * Safe the state in this stateful session bean.
         */
        this.authenticationState = USER_AUTHENTICATED;
        this.authenticatedSubject = deviceMapping.getSubject();
        this.authenticationDevice = deviceMapping.getDevice();

        addHistoryEntry(this.authenticatedSubject,
                HistoryEventType.DEVICE_REGISTRATION, null, deviceMapping
                        .getDevice().getName());

        return deviceMapping;
    }

    @Remove
    public String finalizeAuthentication() throws NodeNotFoundException,
            SubscriptionNotFoundException, ApplicationNotFoundException {

        LOG.debug("finalize authentication");
        if (this.authenticationState != COMMITTED) {
            throw new IllegalStateException("call commit first");
        }

        OlasEntity node = this.nodeAuthenticationService.getLocalNode();

        IdentityServiceClient identityServiceClient = new IdentityServiceClient();
        PrivateKey privateKey = identityServiceClient.getPrivateKey();
        PublicKey publicKey = identityServiceClient.getPublicKey();
        KeyPair keyPair = new KeyPair(publicKey, privateKey);

        int validity = this.samlAuthorityService.getAuthnAssertionValidity();

        String userId = this.userIdMappingService.getApplicationUserId(
                this.expectedApplicationId, getUserId());

        String samlResponseToken = AuthnResponseFactory.createAuthResponse(
                this.expectedChallengeId, this.expectedApplicationId, node
                        .getName(), userId, this.authenticationDevice
                        .getAuthenticationContextClass(), keyPair, validity,
                this.expectedTarget);

        String encodedSamlResponseToken = Base64.encode(samlResponseToken
                .getBytes());
        return encodedSamlResponseToken;
    }

    private void addHistoryEntry(SubjectEntity subject, HistoryEventType event,
            String application, String device) {

        Date now = new Date();
        Map<String, String> historyProperties = new HashMap<String, String>();
        historyProperties.put(SafeOnlineConstants.APPLICATION_PROPERTY,
                application);
        historyProperties.put(SafeOnlineConstants.DEVICE_PROPERTY, device);
        this.historyDAO.addHistoryEntry(now, subject, event, historyProperties);
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
        this.expectedApplicationFriendlyName = null;
        this.expectedChallengeId = null;
        this.requiredDevicePolicy = null;
        this.expectedTarget = null;
        this.authenticationState = INIT;
    }

    private void checkStateBeforeCommit() {

        if (this.authenticationState != USER_AUTHENTICATED) {
            throw new IllegalStateException("bean is not in the correct state");
        }
    }

    private void checkRequiredIdentity() throws SubscriptionNotFoundException,
            ApplicationNotFoundException, ApplicationIdentityNotFoundException,
            IdentityConfirmationRequiredException {

        boolean confirmationRequired = this.identityService
                .isConfirmationRequired(this.expectedApplicationId);
        if (true == confirmationRequired) {
            throw new IdentityConfirmationRequiredException();
        }
    }

    private void checkRequiredMissingAttributes()
            throws ApplicationNotFoundException,
            ApplicationIdentityNotFoundException, MissingAttributeException,
            PermissionDeniedException, AttributeTypeNotFoundException {

        boolean hasMissingAttributes = this.identityService
                .hasMissingAttributes(this.expectedApplicationId);
        if (true == hasMissingAttributes) {
            throw new MissingAttributeException();
        }
    }

    private void checkDevicePolicy() throws ApplicationNotFoundException,
            EmptyDevicePolicyException, DevicePolicyException {

        LOG.debug("authenticationDevice: "
                + this.authenticationDevice.getName());
        List<DeviceEntity> devicePolicy = this.devicePolicyService
                .getDevicePolicy(this.expectedApplicationId,
                        this.requiredDevicePolicy);
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

    private void checkRequiredUsageAgreement()
            throws ApplicationNotFoundException,
            UsageAgreementAcceptationRequiredException,
            SubscriptionNotFoundException {

        boolean requiresUsageAgreementAcceptation = this.usageAgreementService
                .requiresUsageAgreementAcceptation(this.expectedApplicationId);
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

    public void commitAuthentication() throws ApplicationNotFoundException,
            SubscriptionNotFoundException,
            ApplicationIdentityNotFoundException,
            IdentityConfirmationRequiredException, MissingAttributeException,
            EmptyDevicePolicyException, DevicePolicyException,
            UsageAgreementAcceptationRequiredException,
            PermissionDeniedException, AttributeTypeNotFoundException {

        LOG.debug("commitAuthentication for application: "
                + this.expectedApplicationId);

        checkStateBeforeCommit();

        checkRequiredIdentity();

        checkRequiredMissingAttributes();

        checkDevicePolicy();

        checkRequiredGlobalUsageAgreement();

        checkRequiredUsageAgreement();

        ApplicationEntity application = this.applicationDAO
                .findApplication(this.expectedApplicationId);
        if (null == application) {
            // TODO: add security audit
            throw new ApplicationNotFoundException();
        }

        SubscriptionEntity subscription = this.subscriptionDAO
                .findSubscription(this.authenticatedSubject, application);
        if (null == subscription) {
            // TODO: add security audit
            throw new SubscriptionNotFoundException();
        }

        addHistoryEntry(this.authenticatedSubject,
                HistoryEventType.LOGIN_SUCCESS, this.expectedApplicationId,
                this.authenticationDevice.getName());

        this.subscriptionDAO.loggedIn(subscription);
        this.addLoginTick(application);

        /*
         * Safe the state in this stateful session bean.
         */
        this.authenticationState = COMMITTED;

    }

    public String getUserId() {

        LOG.debug("getUserId");
        if (this.authenticationState != USER_AUTHENTICATED
                && this.authenticationState != COMMITTED) {
            throw new IllegalStateException("call authenticate first");
        }
        String userId = this.authenticatedSubject.getUserId();
        return userId;
    }

    public String getUsername() {

        String userId = getUserId();
        return this.subjectService.getSubjectLogin(userId);
    }

    public void setPassword(String userId, String password)
            throws SubjectNotFoundException, DeviceNotFoundException {

        LOG.debug("set password");
        this.passwordDeviceService.register(userId, password);
        DeviceEntity device = this.deviceDAO
                .findDevice(SafeOnlineConstants.USERNAME_PASSWORD_DEVICE_ID);

        this.authenticationDevice = device;
    }

    public String getExpectedApplicationId() {

        if (this.authenticationState == INIT) {
            throw new IllegalStateException("call initialize first");
        }
        return this.expectedApplicationId;
    }

    public String getExpectedApplicationFriendlyName() {

        if (this.authenticationState == INIT) {
            throw new IllegalStateException("call initialize first");
        }
        return this.expectedApplicationFriendlyName;
    }

    public String getExpectedTarget() {

        if (this.authenticationState == INIT) {
            throw new IllegalStateException("call initialize first");
        }
        return this.expectedTarget;
    }

    public Set<DeviceEntity> getRequiredDevicePolicy() {

        if (this.authenticationState == INIT) {
            throw new IllegalStateException("call initialize first");
        }
        return this.requiredDevicePolicy;
    }

    public AuthenticationState getAuthenticationState() {

        return this.authenticationState;
    }

}
