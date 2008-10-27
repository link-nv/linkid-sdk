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
import static net.link.safeonline.authentication.service.AuthenticationState.LOGGING_OUT;
import static net.link.safeonline.authentication.service.AuthenticationState.REDIRECTED;
import static net.link.safeonline.authentication.service.AuthenticationState.USER_AUTHENTICATED;
import static net.link.safeonline.model.bean.UsageStatisticTaskBean.loginCounter;
import static net.link.safeonline.model.bean.UsageStatisticTaskBean.statisticDomain;
import static net.link.safeonline.model.bean.UsageStatisticTaskBean.statisticName;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.ejb.EJB;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.interceptor.Interceptors;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.audit.AccessAuditLogger;
import net.link.safeonline.audit.AuditContextManager;
import net.link.safeonline.audit.SecurityAuditLogger;
import net.link.safeonline.authentication.LogoutProtocolContext;
import net.link.safeonline.authentication.ProtocolContext;
import net.link.safeonline.authentication.exception.ApplicationIdentityNotFoundException;
import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.AuthenticationInitializationException;
import net.link.safeonline.authentication.exception.DeviceDisabledException;
import net.link.safeonline.authentication.exception.DeviceNotFoundException;
import net.link.safeonline.authentication.exception.DevicePolicyException;
import net.link.safeonline.authentication.exception.EmptyDevicePolicyException;
import net.link.safeonline.authentication.exception.IdentityConfirmationRequiredException;
import net.link.safeonline.authentication.exception.InvalidCookieException;
import net.link.safeonline.authentication.exception.MissingAttributeException;
import net.link.safeonline.authentication.exception.NodeMappingNotFoundException;
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
import net.link.safeonline.common.SafeOnlineCookies;
import net.link.safeonline.dao.ApplicationDAO;
import net.link.safeonline.dao.ApplicationPoolDAO;
import net.link.safeonline.dao.DeviceDAO;
import net.link.safeonline.dao.HistoryDAO;
import net.link.safeonline.dao.StatisticDAO;
import net.link.safeonline.dao.StatisticDataPointDAO;
import net.link.safeonline.dao.SubscriptionDAO;
import net.link.safeonline.device.PasswordDeviceService;
import net.link.safeonline.device.sdk.saml2.DeviceOperationType;
import net.link.safeonline.device.sdk.saml2.request.DeviceOperationRequestFactory;
import net.link.safeonline.device.sdk.saml2.response.DeviceOperationResponse;
import net.link.safeonline.device.sdk.saml2.response.DeviceOperationResponseUtil;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.ApplicationPoolEntity;
import net.link.safeonline.entity.DeviceEntity;
import net.link.safeonline.entity.HistoryEventType;
import net.link.safeonline.entity.NodeEntity;
import net.link.safeonline.entity.NodeMappingEntity;
import net.link.safeonline.entity.StatisticDataPointEntity;
import net.link.safeonline.entity.StatisticEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.entity.SubscriptionEntity;
import net.link.safeonline.entity.audit.SecurityThreatType;
import net.link.safeonline.pkix.exception.TrustDomainNotFoundException;
import net.link.safeonline.pkix.model.PkiValidator;
import net.link.safeonline.pkix.model.PkiValidator.PkiResult;
import net.link.safeonline.sdk.auth.saml2.AuthnRequestFactory;
import net.link.safeonline.sdk.auth.saml2.AuthnResponseFactory;
import net.link.safeonline.sdk.auth.saml2.Challenge;
import net.link.safeonline.sdk.auth.saml2.LogoutRequestFactory;
import net.link.safeonline.sdk.auth.saml2.LogoutResponseFactory;
import net.link.safeonline.sdk.auth.saml2.ResponseUtil;
import net.link.safeonline.sdk.ws.sts.TrustDomainType;
import net.link.safeonline.service.NodeMappingService;
import net.link.safeonline.service.SubjectService;
import net.link.safeonline.util.ee.AuthIdentityServiceClient;
import net.link.safeonline.util.ee.IdentityServiceClient;
import net.link.safeonline.validation.InputValidation;
import net.link.safeonline.validation.annotation.NonEmptyString;
import net.link.safeonline.validation.annotation.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xml.security.exceptions.Base64DecodingException;
import org.apache.xml.security.utils.Base64;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml2.core.AuthnRequest;
import org.opensaml.saml2.core.AuthnStatement;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.saml2.core.LogoutRequest;
import org.opensaml.saml2.core.LogoutResponse;
import org.opensaml.saml2.core.NameID;
import org.opensaml.saml2.core.RequestedAuthnContext;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.core.StatusCode;
import org.opensaml.saml2.core.Subject;
import org.opensaml.xml.security.x509.BasicX509Credential;
import org.opensaml.xml.signature.SignatureValidator;
import org.opensaml.xml.validation.ValidationException;


/**
 * Implementation of authentication service interface. This component does not live within the SafeOnline core security
 * domain (chicken-egg problem).
 * 
 * @author fcorneli
 * 
 */
@Stateful
@Interceptors( { AuditContextManager.class, AccessAuditLogger.class, InputValidation.class })
public class AuthenticationServiceBean implements AuthenticationService, AuthenticationServiceRemote {

    private static final Log        LOG                                  = LogFactory
                                                                                 .getLog(AuthenticationServiceBean.class);

    public static final String      SECURITY_MESSAGE_INVALID_COOKIE      = "Attempt to use an invalid SSO Cookie";

    public static final String      SECURITY_MESSAGE_INVALID_APPLICATION = SECURITY_MESSAGE_INVALID_COOKIE
                                                                                 + ": Invalid application: ";

    public static final String      SECURITY_MESSAGE_INVALID_DEVICE      = SECURITY_MESSAGE_INVALID_COOKIE
                                                                                 + ": Invalid device: ";

    public static final String      SECURITY_MESSAGE_INVALID_USER        = SECURITY_MESSAGE_INVALID_COOKIE
                                                                                 + ": Invalid user: ";

    private SubjectEntity           authenticatedSubject;

    private DeviceEntity            authenticationDevice;

    private DateTime                authenticationDate;

    private String                  expectedApplicationId;

    private String                  expectedApplicationFriendlyName;

    private String                  expectedChallengeId;

    private String                  expectedDeviceChallengeId;

    private String                  expectedTarget;

    private Set<DeviceEntity>       requiredDevicePolicy;

    private AuthenticationState     authenticationState;

    private boolean                 ssoEnabled;

    private Cookie                  ssoCookie;

    private String                  cookiePath;

    private List<ApplicationEntity> ssoApplicationsToLogOut;

    private Challenge<String>       expectedLogoutChallenge;


    @PostConstruct
    public void postConstructCallback() {

        ResourceBundle properties = ResourceBundle.getBundle("config");
        this.cookiePath = "/" + properties.getString("olas.auth.webapp.name");

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
    private ApplicationPoolDAO               applicationPoolDAO;

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
    private NodeMappingService               nodeMappingService;

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

    @EJB
    private SecurityAuditLogger              securityAuditLogger;


    public ProtocolContext initialize(String language, @NotNull AuthnRequest samlAuthnRequest)
            throws AuthenticationInitializationException, ApplicationNotFoundException, TrustDomainNotFoundException {

        Issuer issuer = samlAuthnRequest.getIssuer();
        String issuerName = issuer.getValue();
        LOG.debug("issuer: " + issuerName);

        List<X509Certificate> certificates = this.applicationAuthenticationService.getCertificates(issuerName);

        boolean validSignature = false;
        for (X509Certificate certificate : certificates) {
            validSignature = validateSignature(certificate, samlAuthnRequest);
            if (validSignature) {
                break;
            }
        }
        if (!validSignature)
            throw new AuthenticationInitializationException("signature validation error");

        String assertionConsumerService = samlAuthnRequest.getAssertionConsumerServiceURL();
        if (null == assertionConsumerService) {
            LOG.debug("missing AssertionConsumerServiceURL");
            throw new AuthenticationInitializationException("missing AssertionConsumerServiceURL");
        }

        String applicationFriendlyName = samlAuthnRequest.getProviderName();
        if (null == applicationFriendlyName) {
            LOG.debug("missing ProviderName");
            throw new AuthenticationInitializationException("missing ProviderName");
        }

        String samlAuthnRequestId = samlAuthnRequest.getID();
        LOG.debug("SAML authn request ID: " + samlAuthnRequestId);

        boolean forceAuthn = samlAuthnRequest.isForceAuthn();
        LOG.debug("ForceAuthn: " + forceAuthn);

        RequestedAuthnContext requestedAuthnContext = samlAuthnRequest.getRequestedAuthnContext();
        Set<DeviceEntity> devices;
        if (null != requestedAuthnContext) {
            List<AuthnContextClassRef> authnContextClassRefs = requestedAuthnContext.getAuthnContextClassRefs();
            devices = new HashSet<DeviceEntity>();
            for (AuthnContextClassRef authnContextClassRef : authnContextClassRefs) {
                String authnContextClassRefValue = authnContextClassRef.getAuthnContextClassRef();
                LOG.debug("authentication context class reference: " + authnContextClassRefValue);
                List<DeviceEntity> authnDevices = this.devicePolicyService.listDevices(authnContextClassRefValue);
                if (null == authnDevices || authnDevices.size() == 0) {
                    LOG.error("AuthnContextClassRef not supported: " + authnContextClassRefValue);
                    throw new AuthenticationInitializationException("AuthnContextClassRef not supported: "
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
        this.ssoEnabled = !forceAuthn;

        return new ProtocolContext(this.expectedApplicationId, this.expectedApplicationFriendlyName,
                this.expectedTarget, language, this.requiredDevicePolicy);
    }

    private boolean validateSignature(X509Certificate certificate, AuthnRequest samlAuthnRequest)
            throws TrustDomainNotFoundException {

        PkiResult certificateValid = this.pkiValidator.validateCertificate(
                SafeOnlineConstants.SAFE_ONLINE_APPLICATIONS_TRUST_DOMAIN, certificate);

        if (PkiResult.VALID != certificateValid)
            return false;

        BasicX509Credential basicX509Credential = new BasicX509Credential();
        basicX509Credential.setPublicKey(certificate.getPublicKey());
        SignatureValidator signatureValidator = new SignatureValidator(basicX509Credential);
        try {
            signatureValidator.validate(samlAuthnRequest.getSignature());
        } catch (ValidationException e) {
            return false;
        }
        return true;
    }

    public String redirectAuthentication(@NonEmptyString String authenticationServiceUrl,
            @NonEmptyString String targetUrl, @NonEmptyString String device) throws NodeNotFoundException {

        /*
         * Also allow redirected state in case the user manually goes back to olas-auth
         */
        if (this.authenticationState != INITIALIZED && this.authenticationState != REDIRECTED)
            throw new IllegalStateException("call initialize first");

        IdentityServiceClient identityServiceClient = new IdentityServiceClient();
        PrivateKey privateKey = identityServiceClient.getPrivateKey();
        PublicKey publicKey = identityServiceClient.getPublicKey();
        KeyPair keyPair = new KeyPair(publicKey, privateKey);

        NodeEntity node = this.nodeAuthenticationService.getLocalNode();

        Set<String> devices = Collections.singleton(device);

        Challenge<String> challenge = new Challenge<String>();

        String samlRequestToken = AuthnRequestFactory.createAuthnRequest(node.getName(), this.expectedApplicationId,
                this.expectedApplicationFriendlyName, keyPair, authenticationServiceUrl, targetUrl, challenge, devices,
                false);

        String encodedSamlRequestToken = Base64.encode(samlRequestToken.getBytes());

        /*
         * Safe the state in this stateful session bean.
         */
        this.authenticationState = REDIRECTED;
        this.expectedDeviceChallengeId = challenge.getValue();

        return encodedSamlRequestToken;
    }

    public String redirectRegistration(@NonEmptyString String registrationServiceUrl, @NonEmptyString String targetUrl,
            @NonEmptyString String deviceName, @NonEmptyString String userId) throws NodeNotFoundException,
            SubjectNotFoundException, DeviceNotFoundException {

        /*
         * Also allow redirected state in case the user manually goes back to olas-auth
         */
        if (this.authenticationState != INITIALIZED && this.authenticationState != USER_AUTHENTICATED
                && this.authenticationState != REDIRECTED)
            throw new IllegalStateException("call initialize or authenticate first");

        IdentityServiceClient identityServiceClient = new IdentityServiceClient();
        PrivateKey privateKey = identityServiceClient.getPrivateKey();
        PublicKey publicKey = identityServiceClient.getPublicKey();
        KeyPair keyPair = new KeyPair(publicKey, privateKey);

        NodeEntity node = this.nodeAuthenticationService.getLocalNode();

        /*
         * If local node just pass on the userId, else go to node mapping
         */
        DeviceEntity device = this.deviceDAO.getDevice(deviceName);
        String nodeUserId;
        if (node.equals(device.getLocation())) {
            nodeUserId = userId;
        } else {
            NodeMappingEntity nodeMapping = this.nodeMappingService.getNodeMapping(userId, device.getLocation()
                    .getName());
            nodeUserId = nodeMapping.getId();
        }

        Challenge<String> challenge = new Challenge<String>();

        String authenticatedDevice = null;
        if (null != this.authenticationDevice) {
            authenticatedDevice = this.authenticationDevice.getName();
        }

        String samlRequestToken = DeviceOperationRequestFactory.createDeviceOperationRequest(node.getName(),
                nodeUserId, keyPair, registrationServiceUrl, targetUrl, DeviceOperationType.NEW_ACCOUNT_REGISTER,
                challenge, deviceName, authenticatedDevice, null);

        String encodedSamlRequestToken = Base64.encode(samlRequestToken.getBytes());

        /*
         * Safe the state in this stateful session bean.
         */
        this.authenticationState = REDIRECTED;
        this.expectedDeviceChallengeId = challenge.getValue();

        return encodedSamlRequestToken;
    }

    public String authenticate(@NotNull HttpServletRequest request) throws NodeNotFoundException, ServletException,
            NodeMappingNotFoundException, DeviceNotFoundException, SubjectNotFoundException {

        LOG.debug("authenticate");
        if (this.authenticationState != REDIRECTED)
            throw new IllegalStateException("call redirect first");

        DateTime now = new DateTime();

        AuthIdentityServiceClient authIdentityServiceClient = new AuthIdentityServiceClient();
        NodeEntity node = this.nodeAuthenticationService.getLocalNode();

        Response samlResponse = ResponseUtil.validateResponse(now, request, this.expectedDeviceChallengeId,
                this.expectedApplicationId, node.getLocation(), authIdentityServiceClient.getCertificate(),
                authIdentityServiceClient.getPrivateKey(), TrustDomainType.DEVICE);
        if (null == samlResponse)
            return null;

        if (samlResponse.getStatus().getStatusCode().getValue().equals(StatusCode.UNKNOWN_PRINCIPAL_URI)) {
            /*
             * Authentication failed, user wants to try another device tho. Set the state to redirected to mark this
             */
            this.authenticationState = REDIRECTED;
            return null;
        } else if (samlResponse.getStatus().getStatusCode().getValue().equals(StatusCode.AUTHN_FAILED_URI)) {
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
        if (authStatements.isEmpty())
            throw new ServletException("missing authentication statement");

        AuthnStatement authStatement = authStatements.get(0);
        if (null == authStatement.getAuthnContext())
            throw new ServletException("missing authentication context in authentication statement");

        AuthnContextClassRef authnContextClassRef = authStatement.getAuthnContext().getAuthnContextClassRef();
        String authenticatedDevice = authnContextClassRef.getAuthnContextClassRef();
        LOG.debug("authenticated device: " + authenticatedDevice);
        DeviceEntity device = this.deviceDAO.getDevice(authenticatedDevice);

        Subject subject = assertion.getSubject();
        NameID subjectName = subject.getNameID();
        String subjectNameValue = subjectName.getValue();
        LOG.debug("subject name value: " + subjectNameValue);

        NodeEntity localNode = this.nodeAuthenticationService.getLocalNode();
        SubjectEntity subjectEntity;
        if (device.getLocation().equals(localNode)) {
            subjectEntity = this.subjectService.getSubject(subjectNameValue);
        } else {
            NodeMappingEntity nodeMapping = this.nodeMappingService.getNodeMapping(subjectNameValue);
            subjectEntity = nodeMapping.getSubject();
        }

        /*
         * Safe the state in this stateful session bean.
         */
        this.authenticationState = USER_AUTHENTICATED;
        this.authenticatedSubject = subjectEntity;
        this.authenticationDevice = device;
        this.authenticationDate = new DateTime();
        this.expectedDeviceChallengeId = null;

        /*
         * Create SSO Cookie for authentication webapp
         */
        IdentityServiceClient identityServiceClient = new IdentityServiceClient();
        createSsoCookie(identityServiceClient.getSsoKey());

        return subjectEntity.getUserId();
    }

    private void createSsoCookie(SecretKey ssoKey) {

        if (null == this.authenticatedSubject || null == this.authenticationDevice
                || null == this.expectedApplicationId) {
            this.securityAuditLogger.addSecurityAudit(SecurityThreatType.DECEPTION,
                    "Attempt to create SSO Cookie without authenticating");
            throw new IllegalStateException("don't try to create a single sign-on cookie without authenticating");
        }

        DateTime now = new DateTime();
        ApplicationEntity application = this.applicationDAO.findApplication(this.expectedApplicationId);
        if (application.isSsoEnabled()) {
            SingleSignOn sso = new SingleSignOn(this.authenticatedSubject, application, this.authenticationDevice, now);
            createSsoCookie(ssoKey, sso);
        }
    }

    private void createSsoCookie(SecretKey ssoKey, SingleSignOn sso) {

        String value = sso.getValue();

        LOG.debug("cookie value: " + value);

        BouncyCastleProvider bcp = (BouncyCastleProvider) Security.getProvider(BouncyCastleProvider.PROVIDER_NAME);
        String encryptedValue;
        try {
            Cipher encryptCipher = Cipher.getInstance("AES", bcp);
            encryptCipher.init(Cipher.ENCRYPT_MODE, ssoKey);
            byte[] encryptedBytes = encryptCipher.doFinal(value.getBytes());
            encryptedValue = new String(Base64.encode(encryptedBytes));

        } catch (InvalidKeyException e) {
            LOG.debug("invalid key: " + e.getMessage());
            return;
        } catch (IllegalBlockSizeException e) {
            LOG.debug("illegal block size: " + e.getMessage());
            return;
        } catch (BadPaddingException e) {
            LOG.debug("bad padding: " + e.getMessage());
            return;
        } catch (NoSuchAlgorithmException e) {
            LOG.debug("no such algorithm: " + e.getMessage());
            return;
        } catch (NoSuchPaddingException e) {
            LOG.debug("no such padding: " + e.getMessage());
            return;
        }

        this.ssoCookie = new Cookie(SafeOnlineCookies.SINGLE_SIGN_ON_COOKIE_PREFIX + "." + this.expectedApplicationId,
                encryptedValue);
        this.ssoCookie.setMaxAge(-1);
        this.ssoCookie.setPath(this.cookiePath);
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public boolean checkSsoCookie(@NotNull Cookie cookie) throws ApplicationNotFoundException, InvalidCookieException,
            EmptyDevicePolicyException {

        LOG.debug("check single sign on cookie: " + cookie.getName());

        ApplicationEntity application = this.applicationDAO.getApplication(this.expectedApplicationId);
        if (!application.isSsoEnabled())
            return false;

        if (false == this.ssoEnabled)
            return false;

        SingleSignOn sso = parseCookie(cookie);

        List<ApplicationPoolEntity> commonApplicationPools = this.applicationPoolDAO.listCommonApplicationPools(
                application, sso.application);
        LOG.debug("common application pools: " + commonApplicationPools.size());
        if (0 == commonApplicationPools.size()) {
            LOG.debug("no common application pool");
            return false;
        }
        long timeout = Long.MAX_VALUE;
        for (ApplicationPoolEntity applicationPool : commonApplicationPools) {
            if (applicationPool.getSsoTimeout() < timeout) {
                timeout = applicationPool.getSsoTimeout();
            }
        }

        try {
            checkDevicePolicy(sso.device.getName());
        } catch (DevicePolicyException e) {
            LOG.debug("device " + sso.device.getName() + " not enough for application " + this.expectedApplicationId
                    + " device policy");
            return false;
        }

        DateTime notAfter = sso.time.plus(timeout);
        DateTime now = new DateTime();
        if (now.isAfter(notAfter)) {
            LOG.debug("SSO Cookie has expired");
            throw new InvalidCookieException("Expired SSO Cookie");
        }

        /*
         * Add application to cookie
         */
        IdentityServiceClient identityServiceClient = new IdentityServiceClient();
        SecretKey ssoKey = identityServiceClient.getSsoKey();

        sso.addSsoApplication(application);
        createSsoCookie(ssoKey, sso);

        /*
         * Safe the state in this stateful session bean.
         */
        this.authenticationState = USER_AUTHENTICATED;
        this.authenticatedSubject = sso.subject;
        this.authenticationDevice = sso.device;
        this.authenticationDate = sso.time;

        LOG.debug("single sign-on allowed for user " + this.authenticatedSubject.getUserId() + " using device: "
                + this.authenticationDevice.getName());

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public boolean checkSsoCookieForLogout(@NotNull Cookie cookie) throws ApplicationNotFoundException,
            InvalidCookieException {

        LOG.debug("check single sign on cookie for logout: " + cookie.getName());

        ApplicationEntity application = this.applicationDAO.getApplication(this.expectedApplicationId);
        if (!application.isSsoEnabled())
            return false;

        SingleSignOn sso = parseCookie(cookie);

        if (null == this.ssoApplicationsToLogOut) {
            this.ssoApplicationsToLogOut = new LinkedList<ApplicationEntity>();
        }
        if (sso.application.equals(application) || sso.ssoApplications.contains(application)) {
            if (!this.ssoApplicationsToLogOut.contains(sso.application) && !sso.application.equals(application)) {
                LOG.debug("add application " + sso.application.getName() + " for logout");
                this.ssoApplicationsToLogOut.add(sso.application);
            }
            for (ApplicationEntity ssoApplication : sso.ssoApplications) {
                if (!this.ssoApplicationsToLogOut.contains(sso.application) && !ssoApplication.equals(application)) {
                    LOG.debug("add application " + ssoApplication.getName() + " for logout");
                    this.ssoApplicationsToLogOut.add(ssoApplication);
                }
            }
            return true;
        }

        return false;
    }

    public ApplicationEntity findSsoApplicationToLogout() {

        if (null == this.ssoApplicationsToLogOut || this.ssoApplicationsToLogOut.isEmpty())
            return null;

        ApplicationEntity application = this.ssoApplicationsToLogOut.get(0);
        this.ssoApplicationsToLogOut.remove(0);
        return application;
    }


    public static class SingleSignOn {

        public static final String     SUBJECT_FIELD          = "subject";
        public static final String     APPLICATION_FIELD      = "application";
        public static final String     DEVICE_FIELD           = "device";
        public static final String     TIME_FIELD             = "time";
        public static final String     SSO_APPLICATIONS_FIELD = "ssoApplications";

        public SubjectEntity           subject;
        public ApplicationEntity       application;
        public DeviceEntity            device;
        public DateTime                time;
        public List<ApplicationEntity> ssoApplications;


        public SingleSignOn(SubjectEntity subject, ApplicationEntity application, DeviceEntity device, DateTime time) {

            this.subject = subject;
            this.application = application;
            this.device = device;
            this.time = time;
            this.ssoApplications = new LinkedList<ApplicationEntity>();
        }

        public String getValue() {

            String ssoApplicationsValue = "";
            for (ApplicationEntity ssoApplication : this.ssoApplications) {
                ssoApplicationsValue += ssoApplication.getName() + ",";
            }
            if (ssoApplicationsValue.endsWith(",")) {
                ssoApplicationsValue = ssoApplicationsValue.substring(0, ssoApplicationsValue.length() - 1);
            }

            return SUBJECT_FIELD + "=" + this.subject.getUserId() + ";" + APPLICATION_FIELD + "="
                    + this.application.getName() + ";" + DEVICE_FIELD + "=" + this.device.getName() + ";" + TIME_FIELD
                    + "=" + this.time.toString() + ";" + SSO_APPLICATIONS_FIELD + "=" + ssoApplicationsValue;
        }

        public void addSsoApplication(ApplicationEntity ssoApplication) {

            if (!(this.ssoApplications.contains(ssoApplication) || ssoApplication.equals(this.application))) {
                this.ssoApplications.add(ssoApplication);
            }
        }
    }


    private SingleSignOn parseCookie(Cookie cookie) throws InvalidCookieException {

        IdentityServiceClient identityServiceClient = new IdentityServiceClient();
        SecretKey ssoKey = identityServiceClient.getSsoKey();

        /*
         * Decrypt SSO Cookie value
         */
        BouncyCastleProvider bcp = (BouncyCastleProvider) Security.getProvider(BouncyCastleProvider.PROVIDER_NAME);
        String decryptedValue;
        try {
            Cipher decryptCipher = Cipher.getInstance("AES", bcp);
            decryptCipher.init(Cipher.DECRYPT_MODE, ssoKey);
            byte[] decryptedBytes = decryptCipher.doFinal(Base64.decode(cookie.getValue().getBytes("UTF-8")));
            decryptedValue = new String(decryptedBytes);
        } catch (InvalidKeyException e) {
            LOG.debug("invalid key: " + e.getMessage());
            throw new InvalidCookieException(e.getMessage());
        } catch (IllegalBlockSizeException e) {
            LOG.debug("illegal block size: " + e.getMessage());
            throw new InvalidCookieException(e.getMessage());
        } catch (BadPaddingException e) {
            LOG.debug("bad padding: " + e.getMessage());
            throw new InvalidCookieException(e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            LOG.debug("no such algorithm: " + e.getMessage());
            throw new InvalidCookieException(e.getMessage());
        } catch (NoSuchPaddingException e) {
            LOG.debug("no such padding: " + e.getMessage());
            throw new InvalidCookieException(e.getMessage());
        } catch (Base64DecodingException e) {
            LOG.debug("base 64 encoding error: " + e.getMessage());
            throw new InvalidCookieException(e.getMessage());
        } catch (UnsupportedEncodingException e) {
            LOG.debug("unsupported encoding error: " + e.getMessage());
            throw new InvalidCookieException(e.getMessage());
        }

        LOG.debug("Decrypted cookie: " + decryptedValue);

        /*
         * Check SSO Cookie properties
         */
        String[] properties = decryptedValue.split(";");
        if (null == properties || properties.length != 5) {
            LOG.debug(SECURITY_MESSAGE_INVALID_COOKIE);
            this.securityAuditLogger.addSecurityAudit(SecurityThreatType.DECEPTION, SECURITY_MESSAGE_INVALID_COOKIE);
            throw new InvalidCookieException("Invalid SSO Cookie");
        }

        // check subject property
        String[] subjectProperty = properties[0].split("=");
        if (null == subjectProperty || subjectProperty.length != 2
                || !subjectProperty[0].equals(SingleSignOn.SUBJECT_FIELD)) {
            LOG.debug(SECURITY_MESSAGE_INVALID_COOKIE);
            this.securityAuditLogger.addSecurityAudit(SecurityThreatType.DECEPTION, SECURITY_MESSAGE_INVALID_COOKIE);
            throw new InvalidCookieException("Invalid SSO Cookie");
        }
        SubjectEntity subject = this.subjectService.findSubject(subjectProperty[1]);
        if (null == subject) {
            LOG.debug(SECURITY_MESSAGE_INVALID_USER + subjectProperty[1]);
            this.securityAuditLogger.addSecurityAudit(SecurityThreatType.DECEPTION, SECURITY_MESSAGE_INVALID_USER
                    + subjectProperty[1]);
            throw new InvalidCookieException("Invalid SSO Cookie");
        }

        // check application property
        String[] applicationProperty = properties[1].split("=");
        if (null == applicationProperty || applicationProperty.length != 2
                || !applicationProperty[0].equals(SingleSignOn.APPLICATION_FIELD)) {
            LOG.debug(SECURITY_MESSAGE_INVALID_COOKIE);
            this.securityAuditLogger.addSecurityAudit(SecurityThreatType.DECEPTION, SECURITY_MESSAGE_INVALID_COOKIE);
            throw new InvalidCookieException("Invalid SSO Cookie");
        }
        ApplicationEntity application = this.applicationDAO.findApplication(applicationProperty[1]);
        if (null == application) {
            LOG.debug(SECURITY_MESSAGE_INVALID_APPLICATION + applicationProperty[1]);
            this.securityAuditLogger.addSecurityAudit(SecurityThreatType.DECEPTION,
                    SECURITY_MESSAGE_INVALID_APPLICATION + applicationProperty[1]);
            throw new InvalidCookieException("Invalid SSO Cookie");
        }

        // check device property
        String[] deviceProperty = properties[2].split("=");
        if (null == deviceProperty || deviceProperty.length != 2
                || !deviceProperty[0].equals(SingleSignOn.DEVICE_FIELD)) {
            LOG.debug(SECURITY_MESSAGE_INVALID_COOKIE);
            this.securityAuditLogger.addSecurityAudit(SecurityThreatType.DECEPTION, SECURITY_MESSAGE_INVALID_COOKIE);
            throw new InvalidCookieException("Invalid SSO Cookie");
        }
        DeviceEntity device = this.deviceDAO.findDevice(deviceProperty[1]);
        if (null == device) {
            LOG.debug(SECURITY_MESSAGE_INVALID_DEVICE + deviceProperty[1]);
            this.securityAuditLogger.addSecurityAudit(SecurityThreatType.DECEPTION, SECURITY_MESSAGE_INVALID_DEVICE
                    + deviceProperty[1]);
            throw new InvalidCookieException("Invalid SSO Cookie");
        }

        // check time property
        String[] timeProperty = properties[3].split("=");
        if (null == timeProperty || timeProperty.length != 2 || !timeProperty[0].equals(SingleSignOn.TIME_FIELD)) {
            LOG.debug(SECURITY_MESSAGE_INVALID_COOKIE);
            this.securityAuditLogger.addSecurityAudit(SecurityThreatType.DECEPTION, SECURITY_MESSAGE_INVALID_COOKIE);
            throw new InvalidCookieException("Invalid SSO Cookie");
        }
        DateTimeFormatter dateFormatter = ISODateTimeFormat.dateTime();
        DateTime time = dateFormatter.parseDateTime(timeProperty[1]);

        SingleSignOn sso = new SingleSignOn(subject, application, device, time);

        // check sso applications property
        String[] ssoAppProperty = properties[4].split("=");
        if (null == ssoAppProperty || !ssoAppProperty[0].equals(SingleSignOn.SSO_APPLICATIONS_FIELD)) {
            LOG.debug(SECURITY_MESSAGE_INVALID_COOKIE);
            this.securityAuditLogger.addSecurityAudit(SecurityThreatType.DECEPTION, SECURITY_MESSAGE_INVALID_COOKIE);
            throw new InvalidCookieException("Invalid SSO Cookie");
        }
        if (2 == ssoAppProperty.length) {
            String[] ssoApplications = ssoAppProperty[1].split(",");
            for (String ssoApplicationName : ssoApplications) {
                ApplicationEntity ssoApplication = this.applicationDAO.findApplication(ssoApplicationName);
                if (null == ssoApplication) {
                    LOG.debug(SECURITY_MESSAGE_INVALID_APPLICATION + ssoApplicationName);
                    this.securityAuditLogger.addSecurityAudit(SecurityThreatType.DECEPTION,
                            SECURITY_MESSAGE_INVALID_APPLICATION + ssoApplicationName);
                    throw new InvalidCookieException("Invalid SSO Cookie");
                }
                sso.addSsoApplication(ssoApplication);
            }
        }

        return sso;
    }

    public boolean authenticate(@NonEmptyString String loginName, @NonEmptyString String password)
            throws SubjectNotFoundException, DeviceNotFoundException, DeviceDisabledException {

        SubjectEntity subject = this.passwordDeviceService.authenticate(loginName, password);
        if (null == subject)
            return false;
        DeviceEntity device = this.deviceDAO.getDevice(SafeOnlineConstants.USERNAME_PASSWORD_DEVICE_ID);

        /*
         * Safe the state in this stateful session bean.
         */
        this.authenticationState = USER_AUTHENTICATED;
        this.authenticatedSubject = subject;
        this.authenticationDevice = device;
        this.authenticationDate = new DateTime();

        /*
         * Create SSO Cookie for authentication webapp
         */
        IdentityServiceClient identityServiceClient = new IdentityServiceClient();
        createSsoCookie(identityServiceClient.getSsoKey());

        /*
         * Communicate that the authentication process can continue.
         */
        return true;
    }

    public String register(@NotNull HttpServletRequest request) throws NodeNotFoundException, ServletException,
            NodeMappingNotFoundException, DeviceNotFoundException, SubjectNotFoundException {

        LOG.debug("register");
        if (this.authenticationState != REDIRECTED)
            throw new IllegalStateException("call redirect first");

        DateTime now = new DateTime();

        AuthIdentityServiceClient authIdentityServiceClient = new AuthIdentityServiceClient();
        NodeEntity node = this.nodeAuthenticationService.getLocalNode();

        DeviceOperationResponse response = DeviceOperationResponseUtil.validateResponse(now, request,
                this.expectedDeviceChallengeId, DeviceOperationType.NEW_ACCOUNT_REGISTER, node.getLocation(),
                authIdentityServiceClient.getCertificate(), authIdentityServiceClient.getPrivateKey(),
                TrustDomainType.DEVICE);
        if (null == response)
            return null;

        if (response.getStatus().getStatusCode().getValue().equals(DeviceOperationResponse.FAILED_URI)) {
            /*
             * Registration failed, reset the state
             */
            if (null == this.authenticatedSubject) {
                this.authenticationState = INITIALIZED;
            } else {
                this.authenticationState = USER_AUTHENTICATED;
            }
            this.expectedDeviceChallengeId = null;
            return null;
        } else if (response.getStatus().getStatusCode().getValue().equals(StatusCode.REQUEST_UNSUPPORTED_URI)) {
            /*
             * Registration not supported by this device, reset the state
             */
            if (null == this.authenticatedSubject) {
                this.authenticationState = INITIALIZED;
            } else {
                this.authenticationState = USER_AUTHENTICATED;
            }

            this.expectedDeviceChallengeId = null;
            return null;
        }

        Assertion assertion = response.getAssertions().get(0);
        List<AuthnStatement> authStatements = assertion.getAuthnStatements();
        if (authStatements.isEmpty())
            throw new ServletException("missing authentication statement");

        AuthnStatement authStatement = authStatements.get(0);
        if (null == authStatement.getAuthnContext())
            throw new ServletException("missing authentication context in authentication statement");

        AuthnContextClassRef authnContextClassRef = authStatement.getAuthnContext().getAuthnContextClassRef();
        String authenticatedDevice = authnContextClassRef.getAuthnContextClassRef();
        LOG.debug("authenticated device: " + authenticatedDevice);
        DeviceEntity device = this.deviceDAO.getDevice(authenticatedDevice);

        Subject subject = assertion.getSubject();
        NameID subjectName = subject.getNameID();
        String subjectNameValue = subjectName.getValue();
        LOG.debug("subject name value: " + subjectNameValue);

        String userId;
        SubjectEntity subjectEntity;
        NodeEntity localNode = this.nodeAuthenticationService.getLocalNode();
        if (device.getLocation().equals(localNode)) {
            userId = subjectNameValue;
            subjectEntity = this.subjectService.getSubject(userId);
        } else {
            NodeMappingEntity nodeMapping = this.nodeMappingService.getNodeMapping(subjectNameValue);
            userId = nodeMapping.getId();
            subjectEntity = nodeMapping.getSubject();
        }

        /*
         * Safe the state in this stateful session bean.
         */
        this.authenticationState = USER_AUTHENTICATED;
        this.authenticatedSubject = subjectEntity;
        this.authenticationDevice = device;
        this.authenticationDate = new DateTime();

        /*
         * Create SSO Cookie for authentication webapp
         */
        IdentityServiceClient identityServiceClient = new IdentityServiceClient();
        createSsoCookie(identityServiceClient.getSsoKey());

        addHistoryEntry(this.authenticatedSubject, HistoryEventType.DEVICE_REGISTRATION, null, device.getName());

        return userId;
    }

    @Remove
    public String finalizeAuthentication() throws NodeNotFoundException, SubscriptionNotFoundException,
            ApplicationNotFoundException {

        LOG.debug("finalize authentication");
        if (this.authenticationState != COMMITTED)
            throw new IllegalStateException("call commit first");

        NodeEntity node = this.nodeAuthenticationService.getLocalNode();

        IdentityServiceClient identityServiceClient = new IdentityServiceClient();
        PrivateKey privateKey = identityServiceClient.getPrivateKey();
        PublicKey publicKey = identityServiceClient.getPublicKey();
        KeyPair keyPair = new KeyPair(publicKey, privateKey);

        int validity = this.samlAuthorityService.getAuthnAssertionValidity();

        String userId = this.userIdMappingService.getApplicationUserId(this.expectedApplicationId, getUserId());

        String samlResponseToken = AuthnResponseFactory.createAuthResponse(this.expectedChallengeId,
                this.expectedApplicationId, node.getName(), userId, this.authenticationDevice
                        .getAuthenticationContextClass(), keyPair, validity, this.expectedTarget,
                this.authenticationDate);
        LOG.debug("saml response token: " + samlResponseToken);

        String encodedSamlResponseToken = Base64.encode(samlResponseToken.getBytes());
        return encodedSamlResponseToken;
    }

    private void addHistoryEntry(SubjectEntity subject, HistoryEventType event, String application, String device) {

        Date now = new Date();
        Map<String, String> historyProperties = new HashMap<String, String>();
        historyProperties.put(SafeOnlineConstants.APPLICATION_PROPERTY, application);
        historyProperties.put(SafeOnlineConstants.DEVICE_PROPERTY, device);
        this.historyDAO.addHistoryEntry(now, subject, event, historyProperties);
    }

    private void addLoginTick(ApplicationEntity application) {

        StatisticEntity statistic = this.statisticDAO.findOrAddStatisticByNameDomainAndApplication(statisticName,
                statisticDomain, application);

        StatisticDataPointEntity dp = this.statisticDataPointDAO.findOrAddStatisticDataPoint(loginCounter, statistic);

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

        if (this.authenticationState != USER_AUTHENTICATED)
            throw new IllegalStateException("bean is not in the correct state");
    }

    private void checkRequiredIdentity() throws SubscriptionNotFoundException, ApplicationNotFoundException,
            ApplicationIdentityNotFoundException, IdentityConfirmationRequiredException {

        boolean confirmationRequired = this.identityService.isConfirmationRequired(this.expectedApplicationId);
        if (true == confirmationRequired)
            throw new IdentityConfirmationRequiredException();
    }

    private void checkRequiredMissingAttributes() throws ApplicationNotFoundException,
            ApplicationIdentityNotFoundException, MissingAttributeException, PermissionDeniedException,
            AttributeTypeNotFoundException {

        boolean hasMissingAttributes = this.identityService.hasMissingAttributes(this.expectedApplicationId);
        if (true == hasMissingAttributes)
            throw new MissingAttributeException();
    }

    private void checkDevicePolicy(String deviceName) throws ApplicationNotFoundException, EmptyDevicePolicyException,
            DevicePolicyException {

        LOG.debug("check device policy for device: " + deviceName);
        List<DeviceEntity> devicePolicy = this.devicePolicyService.getDevicePolicy(this.expectedApplicationId,
                this.requiredDevicePolicy);
        boolean found = false;
        for (DeviceEntity device : devicePolicy) {
            LOG.debug("devicePolicy: " + device.getName());
            if (device.getName().equals(deviceName)) {
                found = true;
                break;
            }
        }
        if (!found)
            throw new DevicePolicyException();
    }

    private void checkRequiredUsageAgreement() throws ApplicationNotFoundException,
            UsageAgreementAcceptationRequiredException, SubscriptionNotFoundException {

        boolean requiresUsageAgreementAcceptation = this.usageAgreementService
                .requiresUsageAgreementAcceptation(this.expectedApplicationId);
        if (true == requiresUsageAgreementAcceptation)
            throw new UsageAgreementAcceptationRequiredException();
    }

    private void checkRequiredGlobalUsageAgreement() throws UsageAgreementAcceptationRequiredException {

        boolean requiresGlobalUsageAgreementAcceptation = this.usageAgreementService
                .requiresGlobalUsageAgreementAcceptation();
        if (true == requiresGlobalUsageAgreementAcceptation)
            throw new UsageAgreementAcceptationRequiredException();
    }

    public void commitAuthentication() throws ApplicationNotFoundException, SubscriptionNotFoundException,
            ApplicationIdentityNotFoundException, IdentityConfirmationRequiredException, MissingAttributeException,
            EmptyDevicePolicyException, DevicePolicyException, UsageAgreementAcceptationRequiredException,
            PermissionDeniedException, AttributeTypeNotFoundException {

        LOG.debug("commitAuthentication for application: " + this.expectedApplicationId);

        checkStateBeforeCommit();

        checkRequiredIdentity();

        checkRequiredMissingAttributes();

        checkDevicePolicy(this.authenticationDevice.getName());

        checkRequiredGlobalUsageAgreement();

        checkRequiredUsageAgreement();

        ApplicationEntity application = this.applicationDAO.findApplication(this.expectedApplicationId);
        if (null == application) {
            this.securityAuditLogger.addSecurityAudit(SecurityThreatType.DECEPTION, this.authenticatedSubject
                    .getUserId(), "unknown application " + this.expectedApplicationId);
            throw new ApplicationNotFoundException();
        }

        SubscriptionEntity subscription = this.subscriptionDAO.findSubscription(this.authenticatedSubject, application);
        if (null == subscription) {
            this.securityAuditLogger.addSecurityAudit(SecurityThreatType.DECEPTION, this.authenticatedSubject
                    .getUserId(), "susbcription not found for " + this.expectedApplicationId);
            throw new SubscriptionNotFoundException();
        }

        addHistoryEntry(this.authenticatedSubject, HistoryEventType.LOGIN_SUCCESS, this.expectedApplicationId,
                this.authenticationDevice.getName());

        this.subscriptionDAO.loggedIn(subscription);
        addLoginTick(application);

        /*
         * Safe the state in this stateful session bean.
         */
        this.authenticationState = COMMITTED;
    }

    public String getUserId() {

        LOG.debug("getUserId");
        if (this.authenticationState != USER_AUTHENTICATED && this.authenticationState != COMMITTED)
            throw new IllegalStateException("call authenticate first");
        String userId = this.authenticatedSubject.getUserId();
        return userId;
    }

    public String getUsername() {

        String userId = getUserId();
        return this.subjectService.getSubjectLogin(userId);
    }

    public void setPassword(String userId, String password) throws SubjectNotFoundException, DeviceNotFoundException {

        LOG.debug("set password");
        this.passwordDeviceService.register(userId, password);
        DeviceEntity device = this.deviceDAO.findDevice(SafeOnlineConstants.USERNAME_PASSWORD_DEVICE_ID);

        this.authenticationDevice = device;
    }

    public AuthenticationState getAuthenticationState() {

        return this.authenticationState;
    }

    public DeviceEntity getAuthenticationDevice() {

        return this.authenticationDevice;
    }

    public Cookie getSsoCookie() {

        return this.ssoCookie;
    }

    /**
     * {@inheritDoc}
     * 
     */
    public LogoutProtocolContext initialize(@NotNull LogoutRequest samlLogoutRequest)
            throws AuthenticationInitializationException, ApplicationNotFoundException, TrustDomainNotFoundException,
            SubjectNotFoundException {

        Issuer issuer = samlLogoutRequest.getIssuer();
        String issuerName = issuer.getValue();
        LOG.debug("issuer: " + issuerName);
        ApplicationEntity application = this.applicationDAO.getApplication(issuerName);

        List<X509Certificate> certificates = this.applicationAuthenticationService.getCertificates(issuerName);

        boolean validSignature = false;
        for (X509Certificate certificate : certificates) {
            validSignature = validateSignature(certificate, samlLogoutRequest);
            if (validSignature) {
                break;
            }
        }
        if (!validSignature)
            throw new AuthenticationInitializationException("signature validation error");

        String samlAuthnRequestId = samlLogoutRequest.getID();
        LOG.debug("SAML authn request ID: " + samlAuthnRequestId);

        String subjectName = samlLogoutRequest.getNameID().getValue();
        LOG.debug("subject name: " + subjectName);
        String userId = this.userIdMappingService.findUserId(issuerName, subjectName);
        if (null == userId)
            throw new SubjectNotFoundException();
        SubjectEntity subject = this.subjectService.getSubject(userId);

        /*
         * Safe the state in this stateful session bean.
         */
        this.authenticationState = INITIALIZED;
        this.expectedApplicationId = application.getName();
        this.expectedChallengeId = samlAuthnRequestId;
        this.expectedTarget = application.getSsoLogoutUrl().toString();
        this.authenticatedSubject = subject;

        return new LogoutProtocolContext(this.expectedApplicationId, this.expectedTarget);

    }

    private boolean validateSignature(X509Certificate certificate, LogoutRequest samlLogoutRequest)
            throws TrustDomainNotFoundException {

        PkiResult certificateValid = this.pkiValidator.validateCertificate(
                SafeOnlineConstants.SAFE_ONLINE_APPLICATIONS_TRUST_DOMAIN, certificate);

        if (PkiResult.VALID != certificateValid)
            return false;

        BasicX509Credential basicX509Credential = new BasicX509Credential();
        basicX509Credential.setPublicKey(certificate.getPublicKey());
        SignatureValidator signatureValidator = new SignatureValidator(basicX509Credential);
        try {
            signatureValidator.validate(samlLogoutRequest.getSignature());
        } catch (ValidationException e) {
            return false;
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public String getLogoutRequest(ApplicationEntity application) throws SubscriptionNotFoundException,
            ApplicationNotFoundException, NodeNotFoundException {

        LOG.debug("get logout request for " + application.getName());
        if (this.authenticationState != INITIALIZED)
            throw new IllegalStateException("call initialize first");

        NodeEntity node = this.nodeAuthenticationService.getLocalNode();

        IdentityServiceClient identityServiceClient = new IdentityServiceClient();
        PrivateKey privateKey = identityServiceClient.getPrivateKey();
        PublicKey publicKey = identityServiceClient.getPublicKey();
        KeyPair keyPair = new KeyPair(publicKey, privateKey);

        String userId = this.userIdMappingService.getApplicationUserId(application.getName(), this.authenticatedSubject
                .getUserId());

        this.expectedLogoutChallenge = new Challenge<String>();

        String samlLogoutRequestToken = LogoutRequestFactory.createLogoutRequest(userId, node.getName(), keyPair,
                application.getSsoLogoutUrl().toString(), this.expectedLogoutChallenge);

        String encodedSamlLogoutRequestToken = Base64.encode(samlLogoutRequestToken.getBytes());

        /*
         * Safe the state in this stateful session bean.
         */
        this.authenticationState = LOGGING_OUT;

        return encodedSamlLogoutRequestToken;
    }

    /**
     * {@inheritDoc}
     */
    public String handleLogoutResponse(@NotNull HttpServletRequest httpRequest) throws ServletException,
            NodeNotFoundException {

        LOG.debug("handle logout response");
        if (this.authenticationState != LOGGING_OUT)
            throw new IllegalStateException("call getLogoutRequest first");

        AuthIdentityServiceClient authIdentityServiceClient = new AuthIdentityServiceClient();
        NodeEntity node = this.nodeAuthenticationService.getLocalNode();

        LogoutResponse logoutResponse = ResponseUtil.validateLogoutResponse(httpRequest, this.expectedLogoutChallenge
                .getValue(), node.getLocation(), authIdentityServiceClient.getCertificate(), authIdentityServiceClient
                .getPrivateKey(), TrustDomainType.APPLICATION);
        if (null == logoutResponse)
            return null;

        if (!logoutResponse.getStatus().getStatusCode().getValue().equals(StatusCode.SUCCESS_URI)) {
            /*
             * Logout failed, reset state, return null
             */
            this.authenticationState = INITIALIZED;
            this.expectedLogoutChallenge = null;
            return null;
        }

        String applicationName = logoutResponse.getIssuer().getValue();
        LOG.debug("application: " + applicationName);

        /*
         * Safe the state in this stateful session bean.
         */
        this.authenticationState = INITIALIZED;

        return applicationName;
    }

    @Remove
    public String finalizeLogout(boolean partialLogout) throws NodeNotFoundException {

        LOG.debug("finalize logout");
        if (this.authenticationState != INITIALIZED)
            throw new IllegalStateException("call initialize first");

        NodeEntity node = this.nodeAuthenticationService.getLocalNode();

        IdentityServiceClient identityServiceClient = new IdentityServiceClient();
        PrivateKey privateKey = identityServiceClient.getPrivateKey();
        PublicKey publicKey = identityServiceClient.getPublicKey();
        KeyPair keyPair = new KeyPair(publicKey, privateKey);

        String samlLogoutResponseToken = LogoutResponseFactory.createLogoutResponse(partialLogout,
                this.expectedChallengeId, node.getName(), keyPair, this.expectedTarget);
        String encodedSamlLogoutResponseToken = Base64.encode(samlLogoutResponseToken.getBytes());
        return encodedSamlLogoutResponseToken;
    }
}
