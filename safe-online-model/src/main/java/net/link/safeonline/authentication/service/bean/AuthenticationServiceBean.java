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
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
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
import net.link.safeonline.keystore.SafeOnlineNodeKeyStore;
import net.link.safeonline.pkix.exception.TrustDomainNotFoundException;
import net.link.safeonline.pkix.model.PkiValidator;
import net.link.safeonline.pkix.model.PkiValidator.PkiResult;
import net.link.safeonline.saml.common.Challenge;
import net.link.safeonline.sdk.auth.saml2.AuthnRequestFactory;
import net.link.safeonline.sdk.auth.saml2.AuthnResponseFactory;
import net.link.safeonline.sdk.auth.saml2.LogoutRequestFactory;
import net.link.safeonline.sdk.auth.saml2.LogoutResponseFactory;
import net.link.safeonline.sdk.auth.saml2.ResponseUtil;
import net.link.safeonline.sdk.ws.sts.TrustDomainType;
import net.link.safeonline.service.NodeMappingService;
import net.link.safeonline.service.SubjectService;
import net.link.safeonline.validation.InputValidation;
import net.link.safeonline.validation.annotation.NonEmptyString;
import net.link.safeonline.validation.annotation.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xml.security.exceptions.Base64DecodingException;
import org.apache.xml.security.utils.Base64;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.ejb.RemoteBinding;
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
 * Implementation of authentication service interface. This component does not live within the SafeOnline core security domain (chicken-egg
 * problem).
 * 
 * @author fcorneli
 * 
 */
@Stateful
@LocalBinding(jndiBinding = AuthenticationService.JNDI_BINDING)
@RemoteBinding(jndiBinding = AuthenticationServiceRemote.JNDI_BINDING)
@Interceptors( { AuditContextManager.class, AccessAuditLogger.class, InputValidation.class })
public class AuthenticationServiceBean implements AuthenticationService, AuthenticationServiceRemote {

    private static final Log                    LOG                                  = LogFactory.getLog(AuthenticationServiceBean.class);

    private static final SafeOnlineNodeKeyStore nodeKeyStore                         = new SafeOnlineNodeKeyStore();

    public static final String                  SECURITY_MESSAGE_INVALID_COOKIE      = "Attempt to use an invalid SSO Cookie";

    public static final String                  SECURITY_MESSAGE_INVALID_APPLICATION = SECURITY_MESSAGE_INVALID_COOKIE
                                                                                             + ": Invalid application: ";

    public static final String                  SECURITY_MESSAGE_INVALID_DEVICE      = SECURITY_MESSAGE_INVALID_COOKIE
                                                                                             + ": Invalid device: ";

    public static final String                  SECURITY_MESSAGE_INVALID_USER        = SECURITY_MESSAGE_INVALID_COOKIE + ": Invalid user: ";

    private SubjectEntity                       authenticatedSubject;

    private DeviceEntity                        authenticationDevice;

    private DateTime                            authenticationDate;

    private long                                expectedApplicationId                = -1;

    private String                              expectedApplicationName;

    private String                              expectedApplicationFriendlyName;

    private String                              expectedChallengeId;

    private String                              expectedDeviceChallengeId;

    private String                              expectedTarget;

    private Set<DeviceEntity>                   requiredDevicePolicy;

    private AuthenticationState                 authenticationState;

    private boolean                             ssoEnabled;

    private Cookie                              ssoCookie;

    private String                              cookiePath;

    private List<ApplicationEntity>             ssoApplicationsToLogOut;

    private Challenge<String>                   expectedLogoutChallenge;


    @PostConstruct
    public void postConstructCallback() {

        ResourceBundle properties = ResourceBundle.getBundle("config");
        cookiePath = "/" + properties.getString("olas.auth.webapp.name");

        /*
         * Set the initial state of this authentication service bean.
         */
        authenticationState = INIT;
    }


    @EJB(mappedName = SubjectService.JNDI_BINDING)
    private SubjectService                   subjectService;

    @EJB(mappedName = ApplicationDAO.JNDI_BINDING)
    private ApplicationDAO                   applicationDAO;

    @EJB(mappedName = ApplicationPoolDAO.JNDI_BINDING)
    private ApplicationPoolDAO               applicationPoolDAO;

    @EJB(mappedName = SubscriptionDAO.JNDI_BINDING)
    private SubscriptionDAO                  subscriptionDAO;

    @EJB(mappedName = HistoryDAO.JNDI_BINDING)
    private HistoryDAO                       historyDAO;

    @EJB(mappedName = StatisticDAO.JNDI_BINDING)
    private StatisticDAO                     statisticDAO;

    @EJB(mappedName = DeviceDAO.JNDI_BINDING)
    private DeviceDAO                        deviceDAO;

    @EJB(mappedName = StatisticDataPointDAO.JNDI_BINDING)
    private StatisticDataPointDAO            statisticDataPointDAO;

    @EJB(mappedName = IdentityService.JNDI_BINDING)
    private IdentityService                  identityService;

    @EJB(mappedName = DevicePolicyService.JNDI_BINDING)
    private DevicePolicyService              devicePolicyService;

    @EJB(mappedName = NodeMappingService.JNDI_BINDING)
    private NodeMappingService               nodeMappingService;

    @EJB(mappedName = UsageAgreementService.JNDI_BINDING)
    private UsageAgreementService            usageAgreementService;

    @EJB(mappedName = NodeAuthenticationService.JNDI_BINDING)
    private NodeAuthenticationService        nodeAuthenticationService;

    @EJB(mappedName = ApplicationAuthenticationService.JNDI_BINDING)
    private ApplicationAuthenticationService applicationAuthenticationService;

    @EJB(mappedName = PkiValidator.JNDI_BINDING)
    private PkiValidator                     pkiValidator;

    @EJB(mappedName = SamlAuthorityService.JNDI_BINDING)
    private SamlAuthorityService             samlAuthorityService;

    @EJB(mappedName = UserIdMappingService.JNDI_BINDING)
    private UserIdMappingService             userIdMappingService;

    @EJB(mappedName = SecurityAuditLogger.JNDI_BINDING)
    private SecurityAuditLogger              securityAuditLogger;


    public ProtocolContext initialize(Locale language, Integer color, Boolean minimal, @NotNull AuthnRequest samlAuthnRequest)
            throws AuthenticationInitializationException, ApplicationNotFoundException, TrustDomainNotFoundException {

        Issuer issuer = samlAuthnRequest.getIssuer();
        String issuerName = issuer.getValue();
        LOG.debug("issuer: " + issuerName);

        ApplicationEntity application = applicationDAO.getApplication(issuerName);

        List<X509Certificate> certificates = applicationAuthenticationService.getCertificates(application.getId());

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

        // if null fetch from dbase
        String applicationFriendlyName = samlAuthnRequest.getProviderName();
        if (null == applicationFriendlyName) {
            if (null == application.getFriendlyName()) {
                applicationFriendlyName = application.getName();
            } else {
                applicationFriendlyName = application.getFriendlyName();
            }
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
                List<DeviceEntity> authnDevices = devicePolicyService.listDevices(authnContextClassRefValue);
                if (null == authnDevices || authnDevices.size() == 0) {
                    LOG.error("AuthnContextClassRef not supported: " + authnContextClassRefValue);
                    throw new AuthenticationInitializationException("AuthnContextClassRef not supported: " + authnContextClassRefValue);
                }
                devices.addAll(authnDevices);
            }
        } else {
            devices = null;
        }
        /*
         * Safe the state in this stateful session bean.
         */
        authenticationState = INITIALIZED;
        expectedApplicationId = application.getId();
        expectedApplicationName = application.getName();
        expectedApplicationFriendlyName = applicationFriendlyName;
        requiredDevicePolicy = devices;
        expectedTarget = assertionConsumerService;
        expectedChallengeId = samlAuthnRequestId;
        ssoEnabled = !forceAuthn;

        return new ProtocolContext(application.getId(), application.getName(), expectedApplicationFriendlyName, expectedTarget, language,
                color, minimal, requiredDevicePolicy);
    }

    private boolean validateSignature(X509Certificate certificate, AuthnRequest samlAuthnRequest)
            throws TrustDomainNotFoundException {

        PkiResult certificateValid = pkiValidator.validateCertificate(SafeOnlineConstants.SAFE_ONLINE_APPLICATIONS_TRUST_DOMAIN,
                certificate);

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

    public String redirectAuthentication(@NonEmptyString String authenticationServiceUrl, @NonEmptyString String targetUrl,
                                         @NonEmptyString String device)
            throws NodeNotFoundException {

        /*
         * Also allow redirected state in case the user manually goes back to olas-auth
         */
        if (authenticationState != INITIALIZED && authenticationState != REDIRECTED && authenticationState != USER_AUTHENTICATED)
            throw new IllegalStateException("call initialize first");

        NodeEntity node = nodeAuthenticationService.getLocalNode();

        Set<String> devices = Collections.singleton(device);

        Challenge<String> challenge = new Challenge<String>();

        String samlRequestToken = AuthnRequestFactory.createAuthnRequest(node.getName(), expectedApplicationName,
                expectedApplicationFriendlyName, nodeKeyStore.getKeyPair(), authenticationServiceUrl, targetUrl, challenge, devices, false);

        String encodedSamlRequestToken = Base64.encode(samlRequestToken.getBytes());

        /*
         * Safe the state in this stateful session bean.
         */
        authenticationState = REDIRECTED;
        expectedDeviceChallengeId = challenge.getValue();

        return encodedSamlRequestToken;
    }

    public String redirectRegistration(@NonEmptyString String registrationServiceUrl, @NonEmptyString String targetUrl,
                                       @NonEmptyString String deviceName, @NonEmptyString String userId)
            throws NodeNotFoundException, SubjectNotFoundException, DeviceNotFoundException {

        /*
         * Also allow redirected state in case the user manually goes back to olas-auth
         */
        if (authenticationState != INITIALIZED && authenticationState != USER_AUTHENTICATED && authenticationState != REDIRECTED)
            throw new IllegalStateException("call initialize or authenticate first");

        NodeEntity node = nodeAuthenticationService.getLocalNode();

        /*
         * If local node just pass on the userId, else go to node mapping
         */
        DeviceEntity device = deviceDAO.getDevice(deviceName);
        String nodeUserId;
        if (node.equals(device.getLocation())) {
            nodeUserId = userId;
        } else {
            NodeMappingEntity nodeMapping = nodeMappingService.getNodeMapping(userId, device.getLocation().getName());
            nodeUserId = nodeMapping.getId();
        }

        Challenge<String> challenge = new Challenge<String>();

        String authenticatedDevice = null;
        if (null != authenticationDevice) {
            authenticatedDevice = authenticationDevice.getName();
        }

        String samlRequestToken = DeviceOperationRequestFactory.createDeviceOperationRequest(node.getName(), nodeUserId,
                nodeKeyStore.getKeyPair(), registrationServiceUrl, targetUrl, DeviceOperationType.NEW_ACCOUNT_REGISTER, challenge,
                deviceName, authenticatedDevice, null);

        String encodedSamlRequestToken = Base64.encode(samlRequestToken.getBytes());

        /*
         * Safe the state in this stateful session bean.
         */
        authenticationState = REDIRECTED;
        expectedDeviceChallengeId = challenge.getValue();

        return encodedSamlRequestToken;
    }

    public String authenticate(@NotNull HttpServletRequest request)
            throws NodeNotFoundException, ServletException, NodeMappingNotFoundException, DeviceNotFoundException, SubjectNotFoundException {

        LOG.debug("authenticate");
        if (authenticationState != REDIRECTED)
            throw new IllegalStateException("call redirect first");

        DateTime now = new DateTime();

        NodeEntity node = nodeAuthenticationService.getLocalNode();

        Response samlResponse = ResponseUtil.validateResponse(now, request, expectedDeviceChallengeId, expectedApplicationName,
                node.getLocation(), nodeKeyStore.getCertificate(), nodeKeyStore.getPrivateKey(), TrustDomainType.NODE);
        if (null == samlResponse)
            return null;

        if (samlResponse.getStatus().getStatusCode().getValue().equals(StatusCode.UNKNOWN_PRINCIPAL_URI)) {
            /*
             * Authentication failed, user wants to try another device tho. Set the state to redirected to mark this
             */
            authenticationState = REDIRECTED;
            return null;
        } else if (samlResponse.getStatus().getStatusCode().getValue().equals(StatusCode.AUTHN_FAILED_URI)) {
            /*
             * Authentication failed, reset the state
             */
            if (null == authenticatedSubject) {
                authenticationState = INITIALIZED;
            } else {
                authenticationState = USER_AUTHENTICATED;
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
        DeviceEntity device = deviceDAO.getDevice(authenticatedDevice);

        Subject subject = assertion.getSubject();
        NameID subjectName = subject.getNameID();
        String subjectNameValue = subjectName.getValue();
        LOG.debug("subject name value: " + subjectNameValue);

        NodeEntity localNode = nodeAuthenticationService.getLocalNode();
        SubjectEntity subjectEntity;
        if (device.getLocation().equals(localNode)) {
            subjectEntity = subjectService.getSubject(subjectNameValue);
        } else {
            NodeMappingEntity nodeMapping = nodeMappingService.getNodeMapping(subjectNameValue);
            subjectEntity = nodeMapping.getSubject();
        }

        /*
         * Safe the state in this stateful session bean.
         */
        authenticationState = USER_AUTHENTICATED;
        authenticatedSubject = subjectEntity;
        authenticationDevice = device;
        authenticationDate = new DateTime();
        expectedDeviceChallengeId = null;

        /*
         * Create SSO Cookie for authentication webapp
         */
        createSsoCookie(SafeOnlineNodeKeyStore.getSSOKey());

        return subjectEntity.getUserId();
    }

    private void createSsoCookie(SecretKey ssoKey) {

        if (null == authenticatedSubject || null == authenticationDevice || -1 == expectedApplicationId) {
            securityAuditLogger.addSecurityAudit(SecurityThreatType.DECEPTION, "Attempt to create SSO Cookie without authenticating");
            throw new IllegalStateException("don't try to create a single sign-on cookie without authenticating");
        }

        DateTime now = new DateTime();
        ApplicationEntity application = applicationDAO.findApplication(expectedApplicationId);
        if (null == application) {
            securityAuditLogger.addSecurityAudit(SecurityThreatType.DECEPTION, "Attempt to create SSO Cookie for unknown application: "
                    + expectedApplicationId);
            throw new IllegalStateException("Attempt to create SSO Cookie for unknown application: " + expectedApplicationId);
        }
        if (application.isSsoEnabled()) {
            SingleSignOn sso = new SingleSignOn(authenticatedSubject, application, authenticationDevice, now);
            createSsoCookie(application, ssoKey, sso);
        }
    }

    private void createSsoCookie(ApplicationEntity application, SecretKey ssoKey, SingleSignOn sso) {

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

        ssoCookie = new Cookie(SafeOnlineCookies.SINGLE_SIGN_ON_COOKIE_PREFIX + "." + application.getName(), encryptedValue);
        ssoCookie.setMaxAge(-1);
        ssoCookie.setPath(cookiePath);
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public boolean checkSsoCookie(@NotNull Cookie cookie)
            throws ApplicationNotFoundException, InvalidCookieException, EmptyDevicePolicyException {

        LOG.debug("check single sign on cookie: " + cookie.getName());

        ApplicationEntity application = applicationDAO.findApplication(expectedApplicationId);
        if (null == application) {
            securityAuditLogger.addSecurityAudit(SecurityThreatType.DECEPTION, "Attempt to check SSO Cookie for unknown application: "
                    + expectedApplicationId);
            throw new IllegalStateException("Attempt to check SSO Cookie for unknown application: " + expectedApplicationId);
        }
        if (!application.isSsoEnabled())
            return false;

        if (false == ssoEnabled)
            return false;

        SingleSignOn sso = parseCookie(cookie);

        List<ApplicationPoolEntity> commonApplicationPools = applicationPoolDAO.listCommonApplicationPools(application, sso.application);
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
            LOG.debug("device " + sso.device.getName() + " not enough for application " + application.getName() + " device policy");
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
        sso.addSsoApplication(application);
        createSsoCookie(application, SafeOnlineNodeKeyStore.getSSOKey(), sso);

        /*
         * Safe the state in this stateful session bean.
         */
        authenticationState = USER_AUTHENTICATED;
        authenticatedSubject = sso.subject;
        authenticationDevice = sso.device;
        authenticationDate = sso.time;

        LOG.debug("single sign-on allowed for user " + authenticatedSubject.getUserId() + " using device: "
                + authenticationDevice.getName());

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public boolean checkSsoCookieForLogout(@NotNull Cookie cookie)
            throws ApplicationNotFoundException, InvalidCookieException {

        LOG.debug("check single sign on cookie for logout: " + cookie.getName());

        ApplicationEntity application = applicationDAO.findApplication(expectedApplicationId);
        if (null == application) {
            securityAuditLogger.addSecurityAudit(SecurityThreatType.DECEPTION, "Attempt to check SSO Cookie for unknown application: "
                    + expectedApplicationId);
            throw new IllegalStateException("Attempt to check SSO Cookie for unknown application: " + expectedApplicationId);
        }
        if (!application.isSsoEnabled())
            return false;

        SingleSignOn sso = parseCookie(cookie);

        if (null == ssoApplicationsToLogOut) {
            ssoApplicationsToLogOut = new LinkedList<ApplicationEntity>();
        }
        if (sso.application.equals(application) || sso.ssoApplications.contains(application)) {
            if (!ssoApplicationsToLogOut.contains(sso.application) && !sso.application.equals(application)) {
                if (null != sso.application.getSsoLogoutUrl()) {
                    LOG.debug("add application " + sso.application.getName() + " for logout");
                    ssoApplicationsToLogOut.add(sso.application);
                }
            }
            for (ApplicationEntity ssoApplication : sso.ssoApplications) {
                if (!ssoApplicationsToLogOut.contains(sso.application) && !ssoApplication.equals(application)) {
                    if (null != ssoApplication.getSsoLogoutUrl()) {
                        LOG.debug("add application " + ssoApplication.getName() + " for logout");
                        ssoApplicationsToLogOut.add(ssoApplication);
                    }
                }
            }
            return true;
        }

        return false;
    }

    public ApplicationEntity findSsoApplicationToLogout() {

        if (null == ssoApplicationsToLogOut || ssoApplicationsToLogOut.isEmpty())
            return null;

        ApplicationEntity application = ssoApplicationsToLogOut.get(0);
        ssoApplicationsToLogOut.remove(0);
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
            ssoApplications = new LinkedList<ApplicationEntity>();
        }

        public String getValue() {

            String ssoApplicationsValue = "";
            for (ApplicationEntity ssoApplication : ssoApplications) {
                ssoApplicationsValue += ssoApplication.getName() + ",";
            }
            if (ssoApplicationsValue.endsWith(",")) {
                ssoApplicationsValue = ssoApplicationsValue.substring(0, ssoApplicationsValue.length() - 1);
            }

            return SUBJECT_FIELD + "=" + subject.getUserId() + ";" + APPLICATION_FIELD + "=" + application.getName() + ";" + DEVICE_FIELD
                    + "=" + device.getName() + ";" + TIME_FIELD + "=" + time.toString() + ";" + SSO_APPLICATIONS_FIELD + "="
                    + ssoApplicationsValue;
        }

        public void addSsoApplication(ApplicationEntity ssoApplication) {

            if (!(ssoApplications.contains(ssoApplication) || ssoApplication.equals(application))) {
                ssoApplications.add(ssoApplication);
            }
        }
    }


    private SingleSignOn parseCookie(Cookie cookie)
            throws InvalidCookieException {

        /*
         * Decrypt SSO Cookie value
         */
        BouncyCastleProvider bcp = (BouncyCastleProvider) Security.getProvider(BouncyCastleProvider.PROVIDER_NAME);
        String decryptedValue;
        try {
            Cipher decryptCipher = Cipher.getInstance("AES", bcp);
            decryptCipher.init(Cipher.DECRYPT_MODE, SafeOnlineNodeKeyStore.getSSOKey());
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
            securityAuditLogger.addSecurityAudit(SecurityThreatType.DECEPTION, SECURITY_MESSAGE_INVALID_COOKIE);
            throw new InvalidCookieException("Invalid SSO Cookie");
        }

        // check subject property
        String[] subjectProperty = properties[0].split("=");
        if (null == subjectProperty || subjectProperty.length != 2 || !subjectProperty[0].equals(SingleSignOn.SUBJECT_FIELD)) {
            LOG.debug(SECURITY_MESSAGE_INVALID_COOKIE);
            securityAuditLogger.addSecurityAudit(SecurityThreatType.DECEPTION, SECURITY_MESSAGE_INVALID_COOKIE);
            throw new InvalidCookieException("Invalid SSO Cookie");
        }
        SubjectEntity subject = subjectService.findSubject(subjectProperty[1]);
        if (null == subject) {
            LOG.debug(SECURITY_MESSAGE_INVALID_USER + subjectProperty[1]);
            securityAuditLogger.addSecurityAudit(SecurityThreatType.DECEPTION, SECURITY_MESSAGE_INVALID_USER + subjectProperty[1]);
            throw new InvalidCookieException("Invalid SSO Cookie");
        }

        // check application property
        String[] applicationProperty = properties[1].split("=");
        if (null == applicationProperty || applicationProperty.length != 2
                || !applicationProperty[0].equals(SingleSignOn.APPLICATION_FIELD)) {
            LOG.debug(SECURITY_MESSAGE_INVALID_COOKIE);
            securityAuditLogger.addSecurityAudit(SecurityThreatType.DECEPTION, SECURITY_MESSAGE_INVALID_COOKIE);
            throw new InvalidCookieException("Invalid SSO Cookie");
        }
        ApplicationEntity application = applicationDAO.findApplication(applicationProperty[1]);
        if (null == application) {
            LOG.debug(SECURITY_MESSAGE_INVALID_APPLICATION + applicationProperty[1]);
            securityAuditLogger.addSecurityAudit(SecurityThreatType.DECEPTION, SECURITY_MESSAGE_INVALID_APPLICATION
                    + applicationProperty[1]);
            throw new InvalidCookieException("Invalid SSO Cookie");
        }

        // check device property
        String[] deviceProperty = properties[2].split("=");
        if (null == deviceProperty || deviceProperty.length != 2 || !deviceProperty[0].equals(SingleSignOn.DEVICE_FIELD)) {
            LOG.debug(SECURITY_MESSAGE_INVALID_COOKIE);
            securityAuditLogger.addSecurityAudit(SecurityThreatType.DECEPTION, SECURITY_MESSAGE_INVALID_COOKIE);
            throw new InvalidCookieException("Invalid SSO Cookie");
        }
        DeviceEntity device = deviceDAO.findDevice(deviceProperty[1]);
        if (null == device) {
            LOG.debug(SECURITY_MESSAGE_INVALID_DEVICE + deviceProperty[1]);
            securityAuditLogger.addSecurityAudit(SecurityThreatType.DECEPTION, SECURITY_MESSAGE_INVALID_DEVICE + deviceProperty[1]);
            throw new InvalidCookieException("Invalid SSO Cookie");
        }

        // check time property
        String[] timeProperty = properties[3].split("=");
        if (null == timeProperty || timeProperty.length != 2 || !timeProperty[0].equals(SingleSignOn.TIME_FIELD)) {
            LOG.debug(SECURITY_MESSAGE_INVALID_COOKIE);
            securityAuditLogger.addSecurityAudit(SecurityThreatType.DECEPTION, SECURITY_MESSAGE_INVALID_COOKIE);
            throw new InvalidCookieException("Invalid SSO Cookie");
        }
        DateTimeFormatter dateFormatter = ISODateTimeFormat.dateTime();
        DateTime time = dateFormatter.parseDateTime(timeProperty[1]);

        SingleSignOn sso = new SingleSignOn(subject, application, device, time);

        // check sso applications property
        String[] ssoAppProperty = properties[4].split("=");
        if (null == ssoAppProperty || !ssoAppProperty[0].equals(SingleSignOn.SSO_APPLICATIONS_FIELD)) {
            LOG.debug(SECURITY_MESSAGE_INVALID_COOKIE);
            securityAuditLogger.addSecurityAudit(SecurityThreatType.DECEPTION, SECURITY_MESSAGE_INVALID_COOKIE);
            throw new InvalidCookieException("Invalid SSO Cookie");
        }
        if (2 == ssoAppProperty.length) {
            String[] ssoApplications = ssoAppProperty[1].split(",");
            for (String ssoApplicationName : ssoApplications) {
                ApplicationEntity ssoApplication = applicationDAO.findApplication(ssoApplicationName);
                if (null == ssoApplication) {
                    LOG.debug(SECURITY_MESSAGE_INVALID_APPLICATION + ssoApplicationName);
                    securityAuditLogger.addSecurityAudit(SecurityThreatType.DECEPTION, SECURITY_MESSAGE_INVALID_APPLICATION
                            + ssoApplicationName);
                    throw new InvalidCookieException("Invalid SSO Cookie");
                }
                sso.addSsoApplication(ssoApplication);
            }
        }

        return sso;
    }

    public boolean authenticate(@NonEmptyString String loginName, @NonEmptyString String password)
            throws SubjectNotFoundException, DeviceNotFoundException, DeviceDisabledException {

        /*
         * SubjectEntity subject = this.passwordDeviceService.authenticate(loginName, password); if (null == subject) return false;
         * DeviceEntity device = this.deviceDAO.getDevice(SafeOnlineConstants.USERNAME_PASSWORD_DEVICE_ID);
         */

        /*
         * Safe the state in this stateful session bean.
         */
        /*
         * this.authenticationState = USER_AUTHENTICATED; this.authenticatedSubject = subject; this.authenticationDevice = device;
         * this.authenticationDate = new DateTime();
         */
        /*
         * Create SSO Cookie for authentication webapp
         */
        createSsoCookie(SafeOnlineNodeKeyStore.getSSOKey());

        /*
         * Communicate that the authentication process can continue.
         */
        return true;
    }

    public String register(@NotNull HttpServletRequest request)
            throws NodeNotFoundException, ServletException, NodeMappingNotFoundException, DeviceNotFoundException, SubjectNotFoundException {

        LOG.debug("register");
        if (authenticationState != REDIRECTED)
            throw new IllegalStateException("call redirect first");

        DateTime now = new DateTime();

        NodeEntity node = nodeAuthenticationService.getLocalNode();

        DeviceOperationResponse response = DeviceOperationResponseUtil.validateResponse(now, request, expectedDeviceChallengeId,
                DeviceOperationType.NEW_ACCOUNT_REGISTER, node.getLocation(), nodeKeyStore.getCertificate(), nodeKeyStore.getPrivateKey(),
                TrustDomainType.NODE);
        if (null == response)
            return null;

        if (response.getStatus().getStatusCode().getValue().equals(DeviceOperationResponse.FAILED_URI)) {
            /*
             * Registration failed, reset the state
             */
            if (null == authenticatedSubject) {
                authenticationState = INITIALIZED;
            } else {
                authenticationState = USER_AUTHENTICATED;
            }
            expectedDeviceChallengeId = null;
            return null;
        } else if (response.getStatus().getStatusCode().getValue().equals(StatusCode.REQUEST_UNSUPPORTED_URI)) {
            /*
             * Registration not supported by this device, reset the state
             */
            if (null == authenticatedSubject) {
                authenticationState = INITIALIZED;
            } else {
                authenticationState = USER_AUTHENTICATED;
            }

            expectedDeviceChallengeId = null;
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
        DeviceEntity device = deviceDAO.getDevice(authenticatedDevice);

        Subject subject = assertion.getSubject();
        NameID subjectName = subject.getNameID();
        String subjectNameValue = subjectName.getValue();
        LOG.debug("subject name value: " + subjectNameValue);

        String userId;
        SubjectEntity subjectEntity;
        NodeEntity localNode = nodeAuthenticationService.getLocalNode();
        if (device.getLocation().equals(localNode)) {
            userId = subjectNameValue;
            subjectEntity = subjectService.getSubject(userId);
        } else {
            NodeMappingEntity nodeMapping = nodeMappingService.getNodeMapping(subjectNameValue);
            userId = nodeMapping.getId();
            subjectEntity = nodeMapping.getSubject();
        }

        /*
         * Safe the state in this stateful session bean.
         */
        authenticationState = USER_AUTHENTICATED;
        authenticatedSubject = subjectEntity;
        authenticationDevice = device;
        authenticationDate = new DateTime();

        /*
         * Create SSO Cookie for authentication webapp
         */
        createSsoCookie(SafeOnlineNodeKeyStore.getSSOKey());

        addHistoryEntry(authenticatedSubject, HistoryEventType.DEVICE_REGISTRATION, null, device.getName());

        return userId;
    }

    @Remove
    public String finalizeAuthentication()
            throws NodeNotFoundException, SubscriptionNotFoundException, ApplicationNotFoundException {

        LOG.debug("finalize authentication");
        if (authenticationState != COMMITTED)
            throw new IllegalStateException("call commit first");

        NodeEntity node = nodeAuthenticationService.getLocalNode();

        int validity = samlAuthorityService.getAuthnAssertionValidity();

        String userId = userIdMappingService.getApplicationUserId(expectedApplicationId, getUserId());

        String samlResponseToken = AuthnResponseFactory.createAuthResponse(expectedChallengeId, expectedApplicationName, node.getName(),
                userId, authenticationDevice.getAuthenticationContextClass(), nodeKeyStore.getKeyPair(), validity, expectedTarget,
                authenticationDate);
        LOG.debug("saml response token: " + samlResponseToken);

        String encodedSamlResponseToken = Base64.encode(samlResponseToken.getBytes());
        return encodedSamlResponseToken;
    }

    private void addHistoryEntry(SubjectEntity subject, HistoryEventType event, String application, String device) {

        Date now = new Date();
        Map<String, String> historyProperties = new HashMap<String, String>();
        historyProperties.put(SafeOnlineConstants.APPLICATION_PROPERTY, application);
        historyProperties.put(SafeOnlineConstants.DEVICE_PROPERTY, device);
        historyDAO.addHistoryEntry(now, subject, event, historyProperties);
    }

    private void addLoginTick(ApplicationEntity application) {

        StatisticEntity statistic = statisticDAO.findOrAddStatisticByNameDomainAndApplication(statisticName, statisticDomain, application);

        StatisticDataPointEntity dp = statisticDataPointDAO.findOrAddStatisticDataPoint(loginCounter, statistic);

        long count = dp.getX();
        dp.setX(count + 1);
    }

    @Remove
    public void abort() {

        LOG.debug("abort");
        authenticatedSubject = null;
        authenticationDevice = null;
        expectedApplicationId = -1;
        expectedApplicationFriendlyName = null;
        expectedChallengeId = null;
        requiredDevicePolicy = null;
        expectedTarget = null;
        authenticationState = INIT;
    }

    private void checkStateBeforeCommit() {

        if (authenticationState != USER_AUTHENTICATED)
            throw new IllegalStateException("bean is not in the correct state");
    }

    private void checkRequiredIdentity()
            throws SubscriptionNotFoundException, ApplicationNotFoundException, ApplicationIdentityNotFoundException,
            IdentityConfirmationRequiredException {

        boolean confirmationRequired = identityService.isConfirmationRequired(expectedApplicationId);
        if (true == confirmationRequired)
            throw new IdentityConfirmationRequiredException();
    }

    private void checkRequiredMissingAttributes()
            throws ApplicationNotFoundException, ApplicationIdentityNotFoundException, MissingAttributeException,
            PermissionDeniedException, AttributeTypeNotFoundException {

        boolean hasMissingAttributes = identityService.hasMissingAttributes(expectedApplicationId);
        if (true == hasMissingAttributes)
            throw new MissingAttributeException();
    }

    private void checkDevicePolicy(String deviceName)
            throws ApplicationNotFoundException, EmptyDevicePolicyException, DevicePolicyException {

        LOG.debug("check device policy for device: " + deviceName);
        List<DeviceEntity> devicePolicy = devicePolicyService.getDevicePolicy(expectedApplicationId, requiredDevicePolicy);
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

    private void checkRequiredUsageAgreement(String language)
            throws ApplicationNotFoundException, UsageAgreementAcceptationRequiredException, SubscriptionNotFoundException {

        boolean requiresUsageAgreementAcceptation = usageAgreementService
                                                                         .requiresUsageAgreementAcceptation(expectedApplicationId, language);
        if (true == requiresUsageAgreementAcceptation)
            throw new UsageAgreementAcceptationRequiredException();
    }

    private void checkRequiredGlobalUsageAgreement(String language)
            throws UsageAgreementAcceptationRequiredException {

        boolean requiresGlobalUsageAgreementAcceptation = usageAgreementService.requiresGlobalUsageAgreementAcceptation(language);
        if (true == requiresGlobalUsageAgreementAcceptation)
            throw new UsageAgreementAcceptationRequiredException();
    }

    public void commitAuthentication(String language)
            throws ApplicationNotFoundException, SubscriptionNotFoundException, ApplicationIdentityNotFoundException,
            IdentityConfirmationRequiredException, MissingAttributeException, EmptyDevicePolicyException, DevicePolicyException,
            UsageAgreementAcceptationRequiredException, PermissionDeniedException, AttributeTypeNotFoundException {

        LOG.debug("commitAuthentication for application: " + expectedApplicationId);
        ApplicationEntity application = applicationDAO.getApplication(expectedApplicationId);

        checkStateBeforeCommit();

        checkRequiredIdentity();

        checkRequiredMissingAttributes();

        checkDevicePolicy(authenticationDevice.getName());

        checkRequiredGlobalUsageAgreement(language);

        checkRequiredUsageAgreement(language);

        if (null == application) {
            securityAuditLogger.addSecurityAudit(SecurityThreatType.DECEPTION, authenticatedSubject.getUserId(), "unknown application "
                    + expectedApplicationId);
            throw new ApplicationNotFoundException();
        }

        SubscriptionEntity subscription = subscriptionDAO.findSubscription(authenticatedSubject, application);
        if (null == subscription) {
            securityAuditLogger.addSecurityAudit(SecurityThreatType.DECEPTION, authenticatedSubject.getUserId(),
                    "susbcription not found for " + application.getName());
            throw new SubscriptionNotFoundException();
        }

        addHistoryEntry(authenticatedSubject, HistoryEventType.LOGIN_SUCCESS, expectedApplicationName, authenticationDevice.getName());

        subscriptionDAO.loggedIn(subscription);
        addLoginTick(application);

        /*
         * Safe the state in this stateful session bean.
         */
        authenticationState = COMMITTED;
    }

    public String getUserId() {

        LOG.debug("getUserId");
        if (authenticationState != USER_AUTHENTICATED && authenticationState != COMMITTED)
            throw new IllegalStateException("call authenticate first");
        String userId = authenticatedSubject.getUserId();
        return userId;
    }

    public String getUsername() {

        String userId = getUserId();
        return subjectService.getSubjectLogin(userId);
    }

    public void setPassword(String userId, String password)
            throws SubjectNotFoundException, DeviceNotFoundException {

        LOG.debug("set password");
        // this.passwordDeviceService.register(userId, password);
        // DeviceEntity device = this.deviceDAO.findDevice(SafeOnlineConstants.USERNAME_PASSWORD_DEVICE_ID);

        // this.authenticationDevice = device;
    }

    public AuthenticationState getAuthenticationState() {

        return authenticationState;
    }

    public DeviceEntity getAuthenticationDevice() {

        return authenticationDevice;
    }

    public Cookie getSsoCookie() {

        return ssoCookie;
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
        ApplicationEntity application = applicationDAO.getApplication(issuerName);

        List<X509Certificate> certificates = applicationAuthenticationService.getCertificates(issuerName);

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
        String userId = userIdMappingService.findUserId(application.getId(), subjectName);
        if (null == userId)
            throw new SubjectNotFoundException();
        SubjectEntity subject = subjectService.getSubject(userId);

        /*
         * Safe the state in this stateful session bean.
         */
        authenticationState = INITIALIZED;
        expectedApplicationId = application.getId();
        expectedChallengeId = samlAuthnRequestId;
        expectedTarget = application.getSsoLogoutUrl().toString();
        authenticatedSubject = subject;

        return new LogoutProtocolContext(application.getName(), expectedTarget);

    }

    private boolean validateSignature(X509Certificate certificate, LogoutRequest samlLogoutRequest)
            throws TrustDomainNotFoundException {

        PkiResult certificateValid = pkiValidator.validateCertificate(SafeOnlineConstants.SAFE_ONLINE_APPLICATIONS_TRUST_DOMAIN,
                certificate);

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
    public String getLogoutRequest(ApplicationEntity application)
            throws SubscriptionNotFoundException, ApplicationNotFoundException, NodeNotFoundException {

        LOG.debug("get logout request for " + application.getName());
        if (authenticationState != INITIALIZED)
            throw new IllegalStateException("call initialize first");

        NodeEntity node = nodeAuthenticationService.getLocalNode();

        String userId = userIdMappingService.getApplicationUserId(application.getId(), authenticatedSubject.getUserId());

        expectedLogoutChallenge = new Challenge<String>();

        String samlLogoutRequestToken = LogoutRequestFactory.createLogoutRequest(userId, node.getName(), nodeKeyStore.getKeyPair(),
                application.getSsoLogoutUrl().toString(), expectedLogoutChallenge);

        String encodedSamlLogoutRequestToken = Base64.encode(samlLogoutRequestToken.getBytes());

        /*
         * Safe the state in this stateful session bean.
         */
        authenticationState = LOGGING_OUT;

        return encodedSamlLogoutRequestToken;
    }

    /**
     * {@inheritDoc}
     */
    public String handleLogoutResponse(@NotNull HttpServletRequest httpRequest)
            throws ServletException, NodeNotFoundException {

        LOG.debug("handle logout response");
        if (authenticationState != LOGGING_OUT)
            throw new IllegalStateException("call getLogoutRequest first");

        NodeEntity node = nodeAuthenticationService.getLocalNode();

        LogoutResponse logoutResponse = ResponseUtil.validateLogoutResponse(httpRequest, expectedLogoutChallenge.getValue(),
                node.getLocation(), nodeKeyStore.getCertificate(), nodeKeyStore.getPrivateKey(), TrustDomainType.APPLICATION);
        if (null == logoutResponse)
            return null;

        if (!logoutResponse.getStatus().getStatusCode().getValue().equals(StatusCode.SUCCESS_URI)) {
            /*
             * Logout failed, reset state, return null
             */
            authenticationState = INITIALIZED;
            expectedLogoutChallenge = null;
            return null;
        }

        String applicationName = logoutResponse.getIssuer().getValue();
        LOG.debug("application: " + applicationName);

        /*
         * Safe the state in this stateful session bean.
         */
        authenticationState = INITIALIZED;

        return applicationName;
    }

    @Remove
    public String finalizeLogout(boolean partialLogout)
            throws NodeNotFoundException {

        LOG.debug("finalize logout");
        if (authenticationState != INITIALIZED)
            throw new IllegalStateException("call initialize first");

        NodeEntity node = nodeAuthenticationService.getLocalNode();

        String samlLogoutResponseToken = LogoutResponseFactory.createLogoutResponse(partialLogout, expectedChallengeId, node.getName(),
                nodeKeyStore.getKeyPair(), expectedTarget);
        String encodedSamlLogoutResponseToken = Base64.encode(samlLogoutResponseToken.getBytes());
        return encodedSamlLogoutResponseToken;
    }
}
