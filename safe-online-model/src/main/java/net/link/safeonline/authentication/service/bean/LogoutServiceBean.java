/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.authentication.service.bean;

import static net.link.safeonline.authentication.service.LogoutState.INIT;
import static net.link.safeonline.authentication.service.LogoutState.INITIALIZED;
import static net.link.safeonline.authentication.service.LogoutState.LOGGING_OUT;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.ejb.EJB;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.interceptor.Interceptors;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.audit.AccessAuditLogger;
import net.link.safeonline.audit.AuditContextManager;
import net.link.safeonline.audit.SecurityAuditLogger;
import net.link.safeonline.authentication.LogoutProtocolContext;
import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.exception.InvalidCookieException;
import net.link.safeonline.authentication.exception.NodeNotFoundException;
import net.link.safeonline.authentication.exception.SignatureValidationException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.exception.SubscriptionNotFoundException;
import net.link.safeonline.authentication.service.ApplicationAuthenticationService;
import net.link.safeonline.authentication.service.LogoutService;
import net.link.safeonline.authentication.service.LogoutServiceRemote;
import net.link.safeonline.authentication.service.LogoutState;
import net.link.safeonline.authentication.service.NodeAuthenticationService;
import net.link.safeonline.authentication.service.UserIdMappingService;
import net.link.safeonline.dao.ApplicationDAO;
import net.link.safeonline.dao.DeviceDAO;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.DeviceEntity;
import net.link.safeonline.entity.NodeEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.entity.audit.SecurityThreatType;
import net.link.safeonline.keystore.SafeOnlineNodeKeyStore;
import net.link.safeonline.pkix.exception.TrustDomainNotFoundException;
import net.link.safeonline.pkix.model.PkiValidator;
import net.link.safeonline.pkix.model.PkiValidator.PkiResult;
import net.link.safeonline.saml.common.Challenge;
import net.link.safeonline.sdk.auth.saml2.LogoutRequestFactory;
import net.link.safeonline.sdk.auth.saml2.LogoutResponseFactory;
import net.link.safeonline.service.SubjectService;
import net.link.safeonline.validation.InputValidation;
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
import org.opensaml.saml2.core.Issuer;
import org.opensaml.saml2.core.LogoutRequest;
import org.opensaml.saml2.core.LogoutResponse;
import org.opensaml.saml2.core.StatusCode;
import org.opensaml.xml.security.x509.BasicX509Credential;
import org.opensaml.xml.signature.Signature;
import org.opensaml.xml.signature.SignatureValidator;
import org.opensaml.xml.validation.ValidationException;


/**
 * Implementation of logout service interface. This component does not live within the SafeOnline core security domain (chicken-egg
 * problem).
 * 
 * @author wvdhaute
 * 
 */
@Stateful
@LocalBinding(jndiBinding = LogoutService.JNDI_BINDING)
@RemoteBinding(jndiBinding = LogoutServiceRemote.JNDI_BINDING)
@Interceptors( { AuditContextManager.class, AccessAuditLogger.class, InputValidation.class })
public class LogoutServiceBean implements LogoutService, LogoutServiceRemote {

    private static final Log                    LOG                                  = LogFactory.getLog(LogoutServiceBean.class);

    private static final SafeOnlineNodeKeyStore nodeKeyStore                         = new SafeOnlineNodeKeyStore();

    public static final String                  SECURITY_MESSAGE_INVALID_COOKIE      = "Attempt to use an invalid SSO Cookie";

    public static final String                  SECURITY_MESSAGE_INVALID_APPLICATION = SECURITY_MESSAGE_INVALID_COOKIE
                                                                                             + ": Invalid application: ";

    public static final String                  SECURITY_MESSAGE_INVALID_DEVICE      = SECURITY_MESSAGE_INVALID_COOKIE
                                                                                             + ": Invalid device: ";

    public static final String                  SECURITY_MESSAGE_INVALID_USER        = SECURITY_MESSAGE_INVALID_COOKIE + ": Invalid user: ";

    private SubjectEntity                       authenticatedSubject;

    private long                                expectedApplicationId                = -1;

    private String                              expectedChallengeId;

    private String                              expectedTarget;

    private LogoutState                         logoutState;

    private List<ApplicationEntity>             ssoApplicationsToLogOut;

    private Challenge<String>                   expectedLogoutChallenge;


    @PostConstruct
    public void postConstructCallback() {

        /*
         * Set the initial state of this logout service bean.
         */
        logoutState = INIT;
    }


    @EJB(mappedName = SubjectService.JNDI_BINDING)
    private SubjectService                   subjectService;

    @EJB(mappedName = ApplicationDAO.JNDI_BINDING)
    private ApplicationDAO                   applicationDAO;

    @EJB(mappedName = DeviceDAO.JNDI_BINDING)
    private DeviceDAO                        deviceDAO;

    @EJB(mappedName = NodeAuthenticationService.JNDI_BINDING)
    private NodeAuthenticationService        nodeAuthenticationService;

    @EJB(mappedName = ApplicationAuthenticationService.JNDI_BINDING)
    private ApplicationAuthenticationService applicationAuthenticationService;

    @EJB(mappedName = PkiValidator.JNDI_BINDING)
    private PkiValidator                     pkiValidator;

    @EJB(mappedName = UserIdMappingService.JNDI_BINDING)
    private UserIdMappingService             userIdMappingService;

    @EJB(mappedName = SecurityAuditLogger.JNDI_BINDING)
    private SecurityAuditLogger              securityAuditLogger;


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

    @Remove
    public void abort() {

        LOG.debug("abort");
        authenticatedSubject = null;
        expectedApplicationId = -1;
        expectedChallengeId = null;
        expectedTarget = null;
        logoutState = INIT;
    }

    public LogoutState getLogoutState() {

        return logoutState;
    }

    /**
     * {@inheritDoc}
     */
    public LogoutProtocolContext initialize(@NotNull LogoutRequest logoutRequest)
            throws ApplicationNotFoundException, TrustDomainNotFoundException, SubjectNotFoundException, SignatureValidationException {

        Issuer issuer = logoutRequest.getIssuer();
        String issuerName = issuer.getValue();
        LOG.debug("issuer: " + issuerName);
        ApplicationEntity application = applicationDAO.getApplication(issuerName);

        List<X509Certificate> certificates = applicationAuthenticationService.getCertificates(issuerName);

        boolean validSignature = false;
        for (X509Certificate certificate : certificates) {
            validSignature = validateSignature(certificate, logoutRequest.getSignature());
            if (validSignature) {
                break;
            }
        }
        if (!validSignature)
            throw new SignatureValidationException("signature validation error");

        String samlAuthnRequestId = logoutRequest.getID();
        LOG.debug("SAML authn request ID: " + samlAuthnRequestId);

        String subjectName = logoutRequest.getNameID().getValue();
        LOG.debug("subject name: " + subjectName);
        String userId = userIdMappingService.findUserId(application.getId(), subjectName);
        if (null == userId)
            throw new SubjectNotFoundException();
        SubjectEntity subject = subjectService.getSubject(userId);

        /*
         * Save the state in this stateful session bean.
         */
        logoutState = INITIALIZED;
        expectedApplicationId = application.getId();
        expectedChallengeId = samlAuthnRequestId;
        expectedTarget = application.getSsoLogoutUrl().toString();
        authenticatedSubject = subject;

        return new LogoutProtocolContext(application.getName(), expectedTarget);

    }

    private boolean validateSignature(X509Certificate certificate, Signature signature)
            throws TrustDomainNotFoundException {

        PkiResult certificateValid = pkiValidator.validateCertificate(SafeOnlineConstants.SAFE_ONLINE_APPLICATIONS_TRUST_DOMAIN,
                certificate);

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
    public String getLogoutRequest(ApplicationEntity application)
            throws SubscriptionNotFoundException, ApplicationNotFoundException, NodeNotFoundException {

        LOG.debug("get logout request for " + application.getName());
        if (logoutState != INITIALIZED)
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
        logoutState = LOGGING_OUT;

        return encodedSamlLogoutRequestToken;
    }

    /**
     * {@inheritDoc}
     */
    public String handleLogoutResponse(@NotNull LogoutResponse logoutResponse)
            throws ServletException, NodeNotFoundException, ApplicationNotFoundException, TrustDomainNotFoundException,
            SignatureValidationException {

        LOG.debug("handle logout response");
        if (logoutState != LOGGING_OUT)
            throw new IllegalStateException("call getLogoutRequest first");

        Issuer issuer = logoutResponse.getIssuer();
        String issuerName = issuer.getValue();
        LOG.debug("issuer: " + issuerName);

        List<X509Certificate> certificates = applicationAuthenticationService.getCertificates(issuerName);

        boolean validSignature = false;
        for (X509Certificate certificate : certificates) {
            validSignature = validateSignature(certificate, logoutResponse.getSignature());
            if (validSignature) {
                break;
            }
        }
        if (!validSignature)
            throw new SignatureValidationException("signature validation error");

        if (!logoutResponse.getInResponseTo().equals(expectedLogoutChallenge.getValue()))
            throw new ServletException("SAML logout response is not a response belonging to the original request.");

        if (!logoutResponse.getStatus().getStatusCode().getValue().equals(StatusCode.SUCCESS_URI)) {
            /*
             * Logout failed, reset state, return null
             */
            logoutState = INITIALIZED;
            expectedLogoutChallenge = null;
            return null;
        }

        String applicationName = logoutResponse.getIssuer().getValue();
        LOG.debug("application: " + applicationName);

        /*
         * Safe the state in this stateful session bean.
         */
        logoutState = INITIALIZED;

        return applicationName;
    }

    @Remove
    public String finalizeLogout(boolean partialLogout)
            throws NodeNotFoundException {

        LOG.debug("finalize logout");
        if (logoutState != INITIALIZED)
            throw new IllegalStateException("call initialize first");

        NodeEntity node = nodeAuthenticationService.getLocalNode();

        String samlLogoutResponseToken = LogoutResponseFactory.createLogoutResponse(partialLogout, expectedChallengeId, node.getName(),
                nodeKeyStore.getKeyPair(), expectedTarget);
        String encodedSamlLogoutResponseToken = Base64.encode(samlLogoutResponseToken.getBytes());
        return encodedSamlLogoutResponseToken;
    }
}
