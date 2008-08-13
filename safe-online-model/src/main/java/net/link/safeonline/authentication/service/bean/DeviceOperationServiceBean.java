/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.authentication.service.bean;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.interceptor.Interceptors;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.audit.AccessAuditLogger;
import net.link.safeonline.audit.AuditContextManager;
import net.link.safeonline.authentication.exception.DeviceMappingNotFoundException;
import net.link.safeonline.authentication.exception.DeviceNotFoundException;
import net.link.safeonline.authentication.exception.NodeNotFoundException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.service.DeviceOperationService;
import net.link.safeonline.authentication.service.DeviceOperationServiceRemote;
import net.link.safeonline.authentication.service.NodeAuthenticationService;
import net.link.safeonline.common.SafeOnlineRoles;
import net.link.safeonline.dao.HistoryDAO;
import net.link.safeonline.entity.DeviceMappingEntity;
import net.link.safeonline.entity.HistoryEventType;
import net.link.safeonline.entity.OlasEntity;
import net.link.safeonline.sdk.auth.saml2.AuthnRequestFactory;
import net.link.safeonline.sdk.auth.saml2.AuthnResponseUtil;
import net.link.safeonline.sdk.auth.saml2.Challenge;
import net.link.safeonline.sdk.auth.saml2.DeviceOperationType;
import net.link.safeonline.sdk.ws.sts.TrustDomainType;
import net.link.safeonline.service.DeviceMappingService;
import net.link.safeonline.util.ee.AuthIdentityServiceClient;
import net.link.safeonline.util.ee.IdentityServiceClient;
import net.link.safeonline.validation.InputValidation;
import net.link.safeonline.validation.annotation.NonEmptyString;
import net.link.safeonline.validation.annotation.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xml.security.utils.Base64;
import org.jboss.annotation.security.SecurityDomain;
import org.joda.time.DateTime;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml2.core.AuthnStatement;
import org.opensaml.saml2.core.NameID;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.core.StatusCode;
import org.opensaml.saml2.core.Subject;


/**
 * Implementation of device operation service interface.
 *
 * @author wvdhaute
 *
 */
@Stateful
@Interceptors( { AuditContextManager.class, AccessAuditLogger.class, InputValidation.class })
@SecurityDomain(SafeOnlineConstants.SAFE_ONLINE_SECURITY_DOMAIN)
public class DeviceOperationServiceBean implements DeviceOperationService, DeviceOperationServiceRemote {

    private static final Log          LOG = LogFactory.getLog(DeviceOperationServiceBean.class);

    @EJB
    private NodeAuthenticationService nodeAuthenticationService;

    @EJB
    private DeviceMappingService      deviceMappingService;

    @EJB
    private HistoryDAO                historyDAO;

    private String                    expectedChallengeId;

    private DeviceOperationType       expectedDeviceOperation;


    @PostConstruct
    public void postConstructCallback() {

        LOG.debug("device operation service bean created");
    }

    @Remove
    @RolesAllowed(SafeOnlineRoles.USER_ROLE)
    public void abort() {

        LOG.debug("abort");
        this.expectedChallengeId = null;
        this.expectedDeviceOperation = null;
    }

    @RolesAllowed(SafeOnlineRoles.USER_ROLE)
    public String redirect(@NonEmptyString String serviceUrl, @NonEmptyString String targetUrl,
            @NotNull DeviceOperationType deviceOperation, @NonEmptyString String device, @NonEmptyString String userId)
            throws NodeNotFoundException, SubjectNotFoundException, DeviceNotFoundException {

        IdentityServiceClient identityServiceClient = new IdentityServiceClient();
        PrivateKey privateKey = identityServiceClient.getPrivateKey();
        PublicKey publicKey = identityServiceClient.getPublicKey();
        KeyPair keyPair = new KeyPair(publicKey, privateKey);

        OlasEntity node = this.nodeAuthenticationService.getLocalNode();

        Challenge<String> challenge = new Challenge<String>();

        DeviceMappingEntity deviceMapping = this.deviceMappingService.getDeviceMapping(userId, device);

        String samlRequestToken = AuthnRequestFactory.createDeviceOperationAuthnRequest(node.getName(), deviceMapping
                .getId(), keyPair, serviceUrl, targetUrl, deviceOperation, challenge, device);

        String encodedSamlRequestToken = Base64.encode(samlRequestToken.getBytes());

        this.expectedChallengeId = challenge.getValue();
        this.expectedDeviceOperation = deviceOperation;

        return encodedSamlRequestToken;
    }

    @Remove
    @RolesAllowed(SafeOnlineRoles.USER_ROLE)
    public DeviceMappingEntity finalize(@NotNull HttpServletRequest request) throws NodeNotFoundException,
            ServletException, DeviceMappingNotFoundException {

        LOG.debug("finalize");
        LOG.debug("expected challenge id: " + this.expectedChallengeId);
        LOG.debug("expected device operation: " + this.expectedDeviceOperation);

        DateTime now = new DateTime();

        AuthIdentityServiceClient authIdentityServiceClient = new AuthIdentityServiceClient();
        OlasEntity node = this.nodeAuthenticationService.getLocalNode();

        Response samlResponse = AuthnResponseUtil.validateResponse(now, request, this.expectedChallengeId,
                this.expectedDeviceOperation.name(), node.getLocation(), authIdentityServiceClient.getCertificate(),
                authIdentityServiceClient.getPrivateKey(), TrustDomainType.DEVICE);
        if (null == samlResponse)
            return null;

        if (samlResponse.getStatus().getStatusCode().getValue().equals(StatusCode.AUTHN_FAILED_URI)) {
            /*
             * Registration failed, reset the state
             */
            this.expectedChallengeId = null;
            return null;
        } else if (samlResponse.getStatus().getStatusCode().getValue().equals(StatusCode.REQUEST_UNSUPPORTED_URI)) {
            // TODO: add security audit
            /*
             * Registration not supported by this device, reset the state
             */
            this.expectedChallengeId = null;
            return null;
        }

        Assertion assertion = samlResponse.getAssertions().get(0);
        List<AuthnStatement> authStatements = assertion.getAuthnStatements();
        if (authStatements.isEmpty())
            throw new ServletException("missing authentication statement");

        AuthnStatement authStatement = authStatements.get(0);
        if (null == authStatement.getAuthnContext()) {
            throw new ServletException("missing authentication context in authentication statement");
        }

        AuthnContextClassRef authnContextClassRef = authStatement.getAuthnContext().getAuthnContextClassRef();
        String authenticatedDevice = authnContextClassRef.getAuthnContextClassRef();
        LOG.debug("used device: " + authenticatedDevice);

        Subject subject = assertion.getSubject();
        NameID subjectName = subject.getNameID();
        String subjectNameValue = subjectName.getValue();
        LOG.debug("subject name value: " + subjectNameValue);

        /**
         * Check if this device mapping truly exists.
         */
        DeviceMappingEntity deviceMapping = this.deviceMappingService.getDeviceMapping(subjectNameValue);

        if (this.expectedDeviceOperation.equals(DeviceOperationType.REGISTER)) {
            this.historyDAO.addHistoryEntry(deviceMapping.getSubject(), HistoryEventType.DEVICE_REGISTRATION,
                    Collections.singletonMap(SafeOnlineConstants.DEVICE_PROPERTY, deviceMapping.getDevice().getName()));
        } else if (this.expectedDeviceOperation.equals(DeviceOperationType.UPDATE)) {
            this.historyDAO.addHistoryEntry(deviceMapping.getSubject(), HistoryEventType.DEVICE_UPDATE, Collections
                    .singletonMap(SafeOnlineConstants.DEVICE_PROPERTY, deviceMapping.getDevice().getName()));
        } else if (this.expectedDeviceOperation.equals(DeviceOperationType.REMOVE)) {
            this.historyDAO.addHistoryEntry(deviceMapping.getSubject(), HistoryEventType.DEVICE_REMOVAL, Collections
                    .singletonMap(SafeOnlineConstants.DEVICE_PROPERTY, deviceMapping.getDevice().getName()));
        }

        return deviceMapping;
    }
}
