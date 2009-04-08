/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.authentication.service.bean;

import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import net.link.safeonline.authentication.LogoutProtocolContext;
import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.exception.LogoutInitializationException;
import net.link.safeonline.authentication.exception.NodeNotFoundException;
import net.link.safeonline.authentication.exception.SignatureValidationException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.service.ApplicationAuthenticationService;
import net.link.safeonline.authentication.service.LogoutService;
import net.link.safeonline.authentication.service.LogoutServiceRemote;
import net.link.safeonline.authentication.service.LogoutState;
import net.link.safeonline.authentication.service.NodeAuthenticationService;
import net.link.safeonline.authentication.service.SingleSignOnService;
import net.link.safeonline.authentication.service.UserIdMappingService;
import net.link.safeonline.dao.ApplicationDAO;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.NodeEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.keystore.SafeOnlineNodeKeyStore;
import net.link.safeonline.pkix.exception.TrustDomainNotFoundException;
import net.link.safeonline.pkix.model.PkiValidator;
import net.link.safeonline.pkix.model.PkiValidator.PkiResult;
import net.link.safeonline.saml.common.Challenge;
import net.link.safeonline.sdk.auth.saml2.LogoutRequestFactory;
import net.link.safeonline.sdk.auth.saml2.LogoutResponseFactory;
import net.link.safeonline.sdk.auth.saml2.sessiontracking.SessionInfo;
import net.link.safeonline.service.SubjectService;
import net.link.safeonline.util.ee.EjbUtils;
import net.link.safeonline.validation.InputValidation;
import net.link.safeonline.validation.annotation.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xml.security.utils.Base64;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.ejb.RemoteBinding;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.saml2.core.LogoutRequest;
import org.opensaml.saml2.core.LogoutResponse;
import org.opensaml.saml2.core.StatusCode;
import org.opensaml.xml.XMLObject;
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

    private static final Log                          LOG          = LogFactory.getLog(LogoutServiceBean.class);

    private static final SafeOnlineNodeKeyStore       nodeKeyStore = new SafeOnlineNodeKeyStore();

    @EJB(mappedName = SubjectService.JNDI_BINDING)
    private SubjectService                            subjectService;

    @EJB(mappedName = ApplicationDAO.JNDI_BINDING)
    private ApplicationDAO                            applicationDAO;

    @EJB(mappedName = NodeAuthenticationService.JNDI_BINDING)
    private NodeAuthenticationService                 nodeAuthenticationService;

    @EJB(mappedName = ApplicationAuthenticationService.JNDI_BINDING)
    private ApplicationAuthenticationService          applicationAuthenticationService;

    @EJB(mappedName = PkiValidator.JNDI_BINDING)
    private PkiValidator                              pkiValidator;

    @EJB(mappedName = UserIdMappingService.JNDI_BINDING)
    private UserIdMappingService                      userIdMappingService;

    private SubjectEntity                             authenticatedSubject;

    private ApplicationEntity                         logoutApplication;

    private String                                    expectedChallengeId;

    private String                                    expectedTarget;

    private String                                    session;

    private Map<ApplicationEntity, LogoutState>       ssoApplicationStates;

    private Map<ApplicationEntity, Challenge<String>> ssoApplicationChallenges;

    private SingleSignOnService                       ssoService;


    @PostConstruct
    public void postConstruct() {

        ssoApplicationStates = new HashMap<ApplicationEntity, LogoutState>();
        ssoApplicationChallenges = new HashMap<ApplicationEntity, Challenge<String>>();
    }

    /**
     * {@inheritDoc}
     */
    public LogoutState getSSoApplicationState(ApplicationEntity application) {

        return ssoApplicationStates.get(application);
    }

    @PreDestroy
    public void preDestroyCallback() {

        if (null != ssoService) {
            ssoService.abort();
        }
    }

    /**
     * {@inheritDoc}
     */
    public ApplicationEntity findSsoApplicationToLogout() {

        ApplicationEntity nextApplication = null;

        for (Map.Entry<ApplicationEntity, LogoutState> ssoApplicationState : ssoApplicationStates.entrySet())
            if (LogoutState.INITIALIZED.equals(ssoApplicationState.getValue())) {
                nextApplication = ssoApplicationState.getKey();
                break;
            }

        if (nextApplication != null) {
            ssoApplicationStates.put(nextApplication, LogoutState.INITIALIZED);
        }

        return nextApplication;
    }

    @Remove
    public void abort() {

        LOG.debug("abort");
        authenticatedSubject = null;
        logoutApplication = null;
        expectedChallengeId = null;
        expectedTarget = null;
    }

    /**
     * {@inheritDoc}
     */
    public LogoutProtocolContext initialize(@NotNull LogoutRequest logoutRequest)
            throws ApplicationNotFoundException, TrustDomainNotFoundException, SubjectNotFoundException, SignatureValidationException,
            LogoutInitializationException {

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

        session = null;
        if (null != logoutRequest.getExtensions()) {
            if (null != logoutRequest.getExtensions().getUnknownXMLObjects(SessionInfo.DEFAULT_ELEMENT_NAME)) {
                List<XMLObject> sessionInfos = logoutRequest.getExtensions().getUnknownXMLObjects(SessionInfo.DEFAULT_ELEMENT_NAME);
                if (sessionInfos.size() > 1) {
                    LOG.error("only 1 sesssion info element supported");
                    throw new LogoutInitializationException("Only 1 session info element supported");
                }
                session = ((SessionInfo) sessionInfos.get(0)).getSession();
                LOG.debug("session tracking is on: " + session);
            }
        }

        logoutApplication = application;
        expectedChallengeId = samlAuthnRequestId;
        if (null == application.getSsoLogoutUrl()) {
            String errorMessage = "want to use single logout but application " + application.getName() + " has no logout url";
            LOG.error(errorMessage);
            throw new LogoutInitializationException(errorMessage);
        }
        expectedTarget = application.getSsoLogoutUrl().toString();
        authenticatedSubject = subject;

        return new LogoutProtocolContext(application.getName(), expectedTarget);

    }

    /**
     * {@inheritDoc}
     */
    public void logout(List<Cookie> ssoCookies) {

        if (!logoutApplication.isSsoEnabled())
            return;

        ssoService = EjbUtils.getEJB(SingleSignOnService.JNDI_BINDING, SingleSignOnService.class);

        List<ApplicationEntity> applicationsToLogout = ssoService.getApplicationsToLogout(session, logoutApplication, ssoCookies);
        if (null != applicationsToLogout) {
            for (ApplicationEntity application : applicationsToLogout) {
                LOG.debug("add application " + application.getName() + " for logout");
                ssoApplicationStates.put(application, LogoutState.INITIALIZED);
            }
        }

        LOG.debug("found " + ssoApplicationStates.size() + " applications to logout");
    }

    /**
     * {@inheritDoc}
     */
    public List<Cookie> getInvalidCookies() {

        return ssoService.getInvalidCookies();
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
            throws ApplicationNotFoundException, NodeNotFoundException {

        LOG.debug("get logout request for " + application.getName());
        LogoutState applicationState = ssoApplicationStates.get(application);
        if (applicationState == null)
            throw new IllegalArgumentException("this application isn't part of the sso logout pool");
        if (applicationState != LogoutState.INITIALIZED)
            throw new IllegalStateException("this application is not in the initialized phase (already being/been logged out?)");

        NodeEntity localNode = nodeAuthenticationService.getLocalNode();
        String userId = userIdMappingService.getApplicationUserId(application, authenticatedSubject);

        Challenge<String> expectedLogoutChallenge = new Challenge<String>();

        String samlLogoutRequestToken = LogoutRequestFactory.createLogoutRequest(userId, localNode.getName(), nodeKeyStore.getKeyPair(),
                application.getSsoLogoutUrl().toString(), expectedLogoutChallenge, null);

        String encodedSamlLogoutRequestToken = Base64.encode(samlLogoutRequestToken.getBytes());

        /*
         * Save the state in this stateful session bean.
         */
        ssoApplicationStates.put(application, LogoutState.LOGGING_OUT);
        ssoApplicationChallenges.put(application, expectedLogoutChallenge);

        return encodedSamlLogoutRequestToken;
    }

    /**
     * {@inheritDoc}
     */
    public String handleLogoutResponse(@NotNull LogoutResponse logoutResponse)
            throws ServletException, NodeNotFoundException, ApplicationNotFoundException, TrustDomainNotFoundException,
            SignatureValidationException {

        Issuer issuer = logoutResponse.getIssuer();
        String issuerName = issuer.getValue();
        ApplicationEntity application = applicationDAO.getApplication(issuerName);
        LOG.debug("handle logout response for application: " + application.getName());

        // Validate application logout state.
        if (!LogoutState.LOGGING_OUT.equals(ssoApplicationStates.get(application)))
            throw new IllegalStateException("Received a logout response from an application that's not in the logging_out state.");

        // Validate signature.
        for (X509Certificate certificate : applicationAuthenticationService.getCertificates(issuerName)) {
            if (!validateSignature(certificate, logoutResponse.getSignature()))
                throw new SignatureValidationException("signature validation error");
        }

        // Validate challenge ID.
        if (!logoutResponse.getInResponseTo().equals(ssoApplicationChallenges.get(application).getValue()))
            throw new ServletException("SAML logout response is not a response belonging to the original request.");

        // Evaluate response content.
        if (!logoutResponse.getStatus().getStatusCode().getValue().equals(StatusCode.SUCCESS_URI)) {
            ssoApplicationStates.put(application, LogoutState.LOGOUT_FAILED);
            LOG.debug(" - logout failed.");

            return null;
        }

        ssoApplicationStates.put(application, LogoutState.LOGOUT_SUCCESS);
        LOG.debug(" - logout success.");

        return application.getName();
    }

    @Remove
    public String finalizeLogout()
            throws NodeNotFoundException {

        boolean partialLogout = isPartial();
        LOG.debug("finalize logout (partial: " + partialLogout + ")");

        NodeEntity localNode = nodeAuthenticationService.getLocalNode();
        String samlLogoutResponseToken = LogoutResponseFactory.createLogoutResponse(partialLogout, expectedChallengeId,
                localNode.getName(), nodeKeyStore.getKeyPair(), expectedTarget);

        return Base64.encode(samlLogoutResponseToken.getBytes());
    }

    public boolean isPartial() {

        for (LogoutState state : ssoApplicationStates.values())
            if (!LogoutState.LOGOUT_SUCCESS.equals(state))
                return true;

        return false;
    }
}
