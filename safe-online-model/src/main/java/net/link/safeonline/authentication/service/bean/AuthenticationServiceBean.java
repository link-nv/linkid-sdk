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

import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.EJB;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.interceptor.Interceptors;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.audit.AccessAuditLogger;
import net.link.safeonline.audit.AuditContextManager;
import net.link.safeonline.audit.SecurityAuditLogger;
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
import net.link.safeonline.authentication.exception.MissingAttributeException;
import net.link.safeonline.authentication.exception.NodeMappingNotFoundException;
import net.link.safeonline.authentication.exception.NodeNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.SignatureValidationException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.exception.SubscriptionNotFoundException;
import net.link.safeonline.authentication.exception.UsageAgreementAcceptationRequiredException;
import net.link.safeonline.authentication.service.ApplicationAuthenticationService;
import net.link.safeonline.authentication.service.AuthenticationAssertion;
import net.link.safeonline.authentication.service.AuthenticationService;
import net.link.safeonline.authentication.service.AuthenticationServiceRemote;
import net.link.safeonline.authentication.service.AuthenticationState;
import net.link.safeonline.authentication.service.DevicePolicyService;
import net.link.safeonline.authentication.service.IdentityService;
import net.link.safeonline.authentication.service.NodeAuthenticationService;
import net.link.safeonline.authentication.service.SamlAuthorityService;
import net.link.safeonline.authentication.service.SingleSignOnService;
import net.link.safeonline.authentication.service.UsageAgreementService;
import net.link.safeonline.authentication.service.UserIdMappingService;
import net.link.safeonline.dao.ApplicationDAO;
import net.link.safeonline.dao.DeviceDAO;
import net.link.safeonline.dao.HistoryDAO;
import net.link.safeonline.dao.StatisticDAO;
import net.link.safeonline.dao.StatisticDataPointDAO;
import net.link.safeonline.dao.SubscriptionDAO;
import net.link.safeonline.device.sdk.auth.saml2.request.DeviceAuthnRequestFactory;
import net.link.safeonline.device.sdk.auth.saml2.response.AuthnResponseFactory;
import net.link.safeonline.device.sdk.operation.saml2.DeviceOperationType;
import net.link.safeonline.device.sdk.operation.saml2.request.DeviceOperationRequestFactory;
import net.link.safeonline.device.sdk.operation.saml2.response.DeviceOperationResponse;
import net.link.safeonline.entity.ApplicationEntity;
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
import net.link.safeonline.sdk.auth.saml2.ResponseUtil;
import net.link.safeonline.service.NodeMappingService;
import net.link.safeonline.service.SubjectService;
import net.link.safeonline.util.ee.EjbUtils;
import net.link.safeonline.validation.InputValidation;
import net.link.safeonline.validation.annotation.NonEmptyString;
import net.link.safeonline.validation.annotation.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xml.security.utils.Base64;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.ejb.RemoteBinding;
import org.joda.time.DateTime;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.Audience;
import org.opensaml.saml2.core.AudienceRestriction;
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
import org.opensaml.xml.signature.Signature;
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

    private static final Log                    LOG                   = LogFactory.getLog(AuthenticationServiceBean.class);

    private static final SafeOnlineNodeKeyStore nodeKeyStore          = new SafeOnlineNodeKeyStore();

    private AuthenticationAssertion             authenticationAssertion;

    private DeviceEntity                        registeredDevice;

    private long                                expectedApplicationId = -1;

    private String                              expectedApplicationName;

    private String                              expectedApplicationFriendlyName;

    private String                              expectedChallengeId;

    private String                              expectedDeviceChallengeId;

    private String                              expectedTarget;

    private Set<DeviceEntity>                   requiredDevicePolicy;

    private AuthenticationState                 authenticationState;

    private SingleSignOnService                 ssoService;


    @PostConstruct
    public void postConstructCallback() {

        /*
         * Set the initial state of this authentication service bean.
         */
        authenticationState = INIT;
    }

    @PreDestroy
    public void preDestroyCallback() {

        if (null != ssoService) {
            ssoService.abort();
        }
    }


    @EJB(mappedName = SubjectService.JNDI_BINDING)
    private SubjectService                   subjectService;

    @EJB(mappedName = ApplicationDAO.JNDI_BINDING)
    private ApplicationDAO                   applicationDAO;

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


    /**
     * {@inheritDoc}
     */
    public ProtocolContext initialize(Locale language, Integer color, Boolean minimal, @NotNull AuthnRequest samlAuthnRequest)
            throws AuthenticationInitializationException, ApplicationNotFoundException, TrustDomainNotFoundException,
            SignatureValidationException {

        Issuer issuer = samlAuthnRequest.getIssuer();
        String issuerName = issuer.getValue();
        LOG.debug("issuer: " + issuerName);

        ApplicationEntity application = applicationDAO.getApplication(issuerName);

        List<X509Certificate> certificates = applicationAuthenticationService.getCertificates(application.getId());

        boolean validSignature = false;
        for (X509Certificate certificate : certificates) {
            validSignature = validateSignature(certificate, samlAuthnRequest.getSignature(),
                    SafeOnlineConstants.SAFE_ONLINE_APPLICATIONS_TRUST_DOMAIN);
            if (validSignature) {
                break;
            }
        }
        if (!validSignature)
            throw new SignatureValidationException("signature validation error");

        String assertionConsumerService = samlAuthnRequest.getAssertionConsumerServiceURL();
        if (null == assertionConsumerService) {
            LOG.debug("missing AssertionConsumerServiceURL");
            throw new AuthenticationInitializationException("missing AssertionConsumerServiceURL");
        }

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
         * Get SSO info, if enabled, initialize stateful sso service
         */
        boolean forceAuthn = samlAuthnRequest.isForceAuthn();
        LOG.debug("ForceAuthn: " + forceAuthn);

        List<String> audienceList = new LinkedList<String>();
        if (null != samlAuthnRequest.getConditions()) {
            if (null != samlAuthnRequest.getConditions().getAudienceRestrictions()) {
                List<AudienceRestriction> audienceRestrictions = samlAuthnRequest.getConditions().getAudienceRestrictions();
                for (AudienceRestriction audienceRestriction : audienceRestrictions) {
                    List<Audience> audiences = audienceRestriction.getAudiences();
                    for (Audience audience : audiences) {
                        audienceList.add(audience.getAudienceURI());
                    }
                }
            }
        }
        ssoService = EjbUtils.getEJB(SingleSignOnService.JNDI_BINDING, SingleSignOnService.class);
        ssoService.initialize(forceAuthn, audienceList, application, devices);

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

        return new ProtocolContext(application.getId(), application.getName(), expectedApplicationFriendlyName, expectedTarget, language,
                color, minimal, requiredDevicePolicy);
    }

    private boolean validateSignature(X509Certificate certificate, Signature signature, String trustDomain)
            throws TrustDomainNotFoundException {

        PkiResult certificateValid = pkiValidator.validateCertificate(trustDomain, certificate);

        if (PkiResult.VALID != certificateValid)
            return false;

        BasicX509Credential basicX509Credential = new BasicX509Credential();
        basicX509Credential.setPublicKey(certificate.getPublicKey());
        SignatureValidator signatureValidator = new SignatureValidator(basicX509Credential);
        try {
            signatureValidator.validate(signature);
        } catch (ValidationException e) {
            return false;
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
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

        String samlRequestToken = DeviceAuthnRequestFactory.createDeviceAuthnRequest(node.getName(), expectedApplicationName,
                expectedApplicationFriendlyName, nodeKeyStore.getKeyPair(), authenticationServiceUrl, targetUrl, challenge, devices);

        String encodedSamlRequestToken = Base64.encode(samlRequestToken.getBytes());

        /*
         * Safe the state in this stateful session bean.
         */
        authenticationState = REDIRECTED;
        expectedDeviceChallengeId = challenge.getValue();

        return encodedSamlRequestToken;
    }

    /**
     * {@inheritDoc}
     */
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

        String samlRequestToken = DeviceOperationRequestFactory.createDeviceOperationRequest(node.getName(), nodeUserId,
                nodeKeyStore.getKeyPair(), registrationServiceUrl, targetUrl, DeviceOperationType.NEW_ACCOUNT_REGISTER, challenge,
                deviceName, authenticationAssertion.getDevicesList(), null, null);

        String encodedSamlRequestToken = Base64.encode(samlRequestToken.getBytes());

        /*
         * Safe the state in this stateful session bean.
         */
        authenticationState = REDIRECTED;
        expectedDeviceChallengeId = challenge.getValue();

        return encodedSamlRequestToken;
    }

    /**
     * {@inheritDoc}
     */
    public AuthenticationAssertion authenticate(@NotNull Response response)
            throws ApplicationNotFoundException, TrustDomainNotFoundException, SignatureValidationException, ServletException,
            NodeMappingNotFoundException, DeviceNotFoundException, SubjectNotFoundException, NodeNotFoundException {

        LOG.debug("authenticate");
        if (authenticationState != REDIRECTED)
            throw new IllegalStateException("call redirect first");

        Issuer issuer = response.getIssuer();
        String issuerName = issuer.getValue();
        LOG.debug("issuer: " + issuerName);

        if (!response.getInResponseTo().equals(expectedDeviceChallengeId))
            throw new ServletException("response is not matching the request");

        List<X509Certificate> certificates = nodeAuthenticationService.getCertificates(issuerName);
        boolean validSignature = false;
        for (X509Certificate certificate : certificates) {
            validSignature = validateSignature(certificate, response.getSignature(), SafeOnlineConstants.SAFE_ONLINE_NODE_TRUST_DOMAIN);
            if (validSignature) {
                break;
            }
        }
        if (!validSignature)
            throw new SignatureValidationException("signature validation error");

        DateTime now = new DateTime();
        for (Assertion assertion : response.getAssertions()) {
            ResponseUtil.validateAssertion(assertion, now, expectedApplicationName);
        }

        if (response.getStatus().getStatusCode().getValue().equals(StatusCode.UNKNOWN_PRINCIPAL_URI)) {
            /*
             * Authentication failed, user wants to try another device tho. Set the state to redirected to mark this
             */
            authenticationState = REDIRECTED;
            return null;
        } else if (response.getStatus().getStatusCode().getValue().equals(StatusCode.AUTHN_FAILED_URI)) {
            /*
             * Authentication failed, reset the state
             */
            if (null == authenticationAssertion) {
                authenticationState = INITIALIZED;
            } else {
                authenticationState = USER_AUTHENTICATED;
            }
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

        NodeEntity localNode = nodeAuthenticationService.getLocalNode();
        SubjectEntity subjectEntity;
        if (device.getLocation().equals(localNode)) {
            subjectEntity = subjectService.getSubject(subjectNameValue);
        } else {
            NodeMappingEntity nodeMapping = nodeMappingService.getNodeMapping(subjectNameValue);
            subjectEntity = nodeMapping.getSubject();
        }
        DateTime authenticationTime = new DateTime();

        /*
         * Safe the state in this stateful session bean.
         */
        authenticationState = USER_AUTHENTICATED;
        authenticationAssertion = new AuthenticationAssertion(subjectEntity);
        authenticationAssertion.addAuthentication(authenticationTime, device);
        expectedDeviceChallengeId = null;

        /*
         * Set SSO Cookies
         */
        ssoService.setCookies(subjectEntity, device, authenticationTime);

        return authenticationAssertion;
    }

    /**
     * {@inheritDoc}
     */
    public List<AuthenticationAssertion> login(List<Cookie> ssoCookies) {

        List<AuthenticationAssertion> authenticationAssertions = ssoService.signOn(ssoCookies);
        if (null != authenticationAssertions && !authenticationAssertions.isEmpty() && authenticationAssertions.size() == 1) {
            /*
             * Safe the state in this stateful session bean.
             */
            authenticationState = USER_AUTHENTICATED;
            authenticationAssertion = authenticationAssertions.get(0);

            LOG.debug("single sign-on allowed for user " + authenticationAssertion.getSubject().getUserId() + " using devices: "
                    + authenticationAssertion.getDevicesString());

        }

        return authenticationAssertions;
    }

    /**
     * {@inheritDoc}
     */
    public List<Cookie> getInvalidCookies() {

        return ssoService.getInvalidCookies();
    }

    /**
     * {@inheritDoc}
     */
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
         * Communicate that the authentication process can continue.
         */
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public AuthenticationAssertion register(@NotNull DeviceOperationResponse response)
            throws NodeNotFoundException, ServletException, NodeMappingNotFoundException, DeviceNotFoundException,
            SubjectNotFoundException, ApplicationNotFoundException, TrustDomainNotFoundException, SignatureValidationException {

        LOG.debug("register");
        if (authenticationState != REDIRECTED)
            throw new IllegalStateException("call redirect first");

        Issuer issuer = response.getIssuer();
        String issuerName = issuer.getValue();
        LOG.debug("issuer: " + issuerName);

        List<X509Certificate> certificates = nodeAuthenticationService.getCertificates(issuerName);
        boolean validSignature = false;
        for (X509Certificate certificate : certificates) {
            validSignature = validateSignature(certificate, response.getSignature(), SafeOnlineConstants.SAFE_ONLINE_NODE_TRUST_DOMAIN);
            if (validSignature) {
                break;
            }
        }
        if (!validSignature)
            throw new SignatureValidationException("signature validation error");

        /*
         * Check whether the response is indeed a response to a previous request by comparing the InResponseTo fields
         */
        if (!response.getInResponseTo().equals(expectedDeviceChallengeId)) {
            securityAuditLogger.addSecurityAudit(SecurityThreatType.DECEPTION,
                    "device operation response is not belonging to the original request, mismatch in challenge");
            throw new ServletException("device operation response is not a response belonging to the original request.");
        }

        if (!response.getDeviceOperation().equals(DeviceOperationType.NEW_ACCOUNT_REGISTER.name())) {
            securityAuditLogger.addSecurityAudit(SecurityThreatType.DECEPTION,
                    "device operation response is not belonging to the original request, mismatch in device operation");
            throw new ServletException(
                    "device operation response is not a response belonging to the original request, mismatch in device operation");
        }

        if (!response.getStatus().getStatusCode().getValue().equals(StatusCode.REQUEST_UNSUPPORTED_URI)
                && !response.getStatus().getStatusCode().getValue().equals(DeviceOperationResponse.FAILED_URI)) {
            DateTime now = new DateTime();

            List<Assertion> assertions = response.getAssertions();
            if (assertions.isEmpty())
                throw new ServletException("missing Assertion");

            for (Assertion assertion : assertions) {
                ResponseUtil.validateAssertion(assertion, now, null);
            }
        }

        if (response.getStatus().getStatusCode().getValue().equals(DeviceOperationResponse.FAILED_URI)) {
            /*
             * Registration failed, reset the state
             */
            if (null == authenticationAssertion) {
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
            if (null == authenticationAssertion) {
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
        registeredDevice = deviceDAO.getDevice(authenticatedDevice);

        Subject subject = assertion.getSubject();
        NameID subjectName = subject.getNameID();
        String subjectNameValue = subjectName.getValue();
        LOG.debug("subject name value: " + subjectNameValue);

        String userId;
        SubjectEntity subjectEntity;
        NodeEntity localNode = nodeAuthenticationService.getLocalNode();
        if (registeredDevice.getLocation().equals(localNode)) {
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
        if (null == authenticationAssertion) {
            authenticationAssertion = new AuthenticationAssertion(subjectEntity);
        } else {
            if (!subjectEntity.equals(authenticationAssertion.getSubject())) {
                securityAuditLogger.addSecurityAudit(SecurityThreatType.DECEPTION,
                        "Subject in device response does not match already authenticated subject.");
                throw new ServletException("Subject in device response does not match already authenticated subject.");
            }
        }
        DateTime authenticationTime = new DateTime();
        authenticationAssertion.addAuthentication(authenticationTime, registeredDevice);

        /*
         * Set SSO Cookies
         */
        ssoService.setCookies(subjectEntity, registeredDevice, authenticationTime);

        addHistoryEntry(authenticationAssertion.getSubject(), HistoryEventType.DEVICE_REGISTRATION, null, registeredDevice.getName());

        return authenticationAssertion;
    }

    /**
     * {@inheritDoc}
     */
    @Remove
    public String finalizeAuthentication()
            throws NodeNotFoundException, SubscriptionNotFoundException, ApplicationNotFoundException {

        LOG.debug("finalize authentication");
        if (authenticationState != COMMITTED)
            throw new IllegalStateException("call commit first");

        NodeEntity node = nodeAuthenticationService.getLocalNode();

        int validity = samlAuthorityService.getAuthnAssertionValidity();

        String userId = userIdMappingService.getApplicationUserId(expectedApplicationId, authenticationAssertion.getSubject().getUserId());

        Map<DateTime, String> authentications = new HashMap<DateTime, String>();
        for (Entry<DateTime, DeviceEntity> authentication : authenticationAssertion.getAuthentications().entrySet()) {
            authentications.put(authentication.getKey(), authentication.getValue().getName());
        }

        String samlResponseToken = AuthnResponseFactory.createAuthResponse(expectedChallengeId, expectedApplicationName, node.getName(),
                userId, nodeKeyStore.getKeyPair(), validity, expectedTarget, authentications);
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

    /**
     * {@inheritDoc}
     */
    @Remove
    public void abort() {

        LOG.debug("abort");
        authenticationAssertion = null;
        registeredDevice = null;
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

    private void checkDevicePolicy()
            throws ApplicationNotFoundException, EmptyDevicePolicyException, DevicePolicyException {

        LOG.debug("check device policy");
        boolean found = false;

        List<DeviceEntity> devicePolicy = devicePolicyService.getDevicePolicy(expectedApplicationId, requiredDevicePolicy);
        for (DeviceEntity device : authenticationAssertion.getDevices()) {
            if (devicePolicy.contains(device)) {
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

    /**
     * {@inheritDoc}
     */
    public void commitAuthentication(String language)
            throws ApplicationNotFoundException, SubscriptionNotFoundException, ApplicationIdentityNotFoundException,
            IdentityConfirmationRequiredException, MissingAttributeException, EmptyDevicePolicyException, DevicePolicyException,
            UsageAgreementAcceptationRequiredException, PermissionDeniedException, AttributeTypeNotFoundException {

        LOG.debug("commitAuthentication for application: " + expectedApplicationId);
        ApplicationEntity application = applicationDAO.getApplication(expectedApplicationId);

        checkStateBeforeCommit();

        checkRequiredIdentity();

        checkRequiredMissingAttributes();

        checkDevicePolicy();

        checkRequiredGlobalUsageAgreement(language);

        checkRequiredUsageAgreement(language);

        if (null == application) {
            securityAuditLogger.addSecurityAudit(SecurityThreatType.DECEPTION, authenticationAssertion.getSubject().getUserId(),
                    "unknown application " + expectedApplicationId);
            throw new ApplicationNotFoundException();
        }

        SubscriptionEntity subscription = subscriptionDAO.findSubscription(authenticationAssertion.getSubject(), application);
        if (null == subscription) {
            securityAuditLogger.addSecurityAudit(SecurityThreatType.DECEPTION, authenticationAssertion.getSubject().getUserId(),
                    "susbcription not found for " + application.getName());
            throw new SubscriptionNotFoundException();
        }

        addHistoryEntry(authenticationAssertion.getSubject(), HistoryEventType.LOGIN_SUCCESS, expectedApplicationName,
                authenticationAssertion.getDevicesString());

        subscriptionDAO.loggedIn(subscription);
        addLoginTick(application);

        /*
         * Safe the state in this stateful session bean.
         */
        authenticationState = COMMITTED;
    }

    /**
     * {@inheritDoc}
     */
    public String getUsername() {

        if (authenticationState != USER_AUTHENTICATED && authenticationState != COMMITTED)
            throw new IllegalStateException("call authenticate first");

        return subjectService.getSubjectLogin(authenticationAssertion.getSubject().getUserId());
    }

    /**
     * {@inheritDoc}
     */
    public void setPassword(String userId, String password)
            throws SubjectNotFoundException, DeviceNotFoundException {

        LOG.debug("set password");
        // this.passwordDeviceService.register(userId, password);
        // DeviceEntity device = this.deviceDAO.findDevice(SafeOnlineConstants.USERNAME_PASSWORD_DEVICE_ID);

        // this.authenticationDevice = device;
    }

    /**
     * {@inheritDoc}
     */
    public AuthenticationState getAuthenticationState() {

        return authenticationState;
    }

    /**
     * {@inheritDoc}
     */
    public DeviceEntity getRegisteredDevice() {

        return registeredDevice;
    }

    /**
     * {@inheritDoc}
     */
    public List<Cookie> getSsoCookies() {

        return ssoService.getCookies();
    }
}
