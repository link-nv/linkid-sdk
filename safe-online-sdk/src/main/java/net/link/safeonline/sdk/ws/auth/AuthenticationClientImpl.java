/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.ws.auth;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.ws.soap.AddressingFeature;
import javax.xml.ws.wsaddressing.W3CEndpointReference;

import net.lin_k.safe_online.auth.AuthenticationPort;
import net.lin_k.safe_online.auth.AuthenticationService;
import net.lin_k.safe_online.auth.DeviceAuthenticationInformationType;
import net.lin_k.safe_online.auth.WSAuthenticationGlobalUsageAgreementConfirmationType;
import net.lin_k.safe_online.auth.WSAuthenticationGlobalUsageAgreementRequestType;
import net.lin_k.safe_online.auth.WSAuthenticationGlobalUsageAgreementResponseType;
import net.lin_k.safe_online.auth.WSAuthenticationIdentityConfirmationType;
import net.lin_k.safe_online.auth.WSAuthenticationIdentityRequestType;
import net.lin_k.safe_online.auth.WSAuthenticationMissingAttributesRequestType;
import net.lin_k.safe_online.auth.WSAuthenticationMissingAttributesSaveRequestType;
import net.lin_k.safe_online.auth.WSAuthenticationRequestType;
import net.lin_k.safe_online.auth.WSAuthenticationResponseType;
import net.lin_k.safe_online.auth.WSAuthenticationUsageAgreementConfirmationType;
import net.lin_k.safe_online.auth.WSAuthenticationUsageAgreementRequestType;
import net.lin_k.safe_online.auth.WSAuthenticationUsageAgreementResponseType;
import net.link.safeonline.auth.ws.AuthenticationServiceFactory;
import net.link.safeonline.auth.ws.AuthenticationStep;
import net.link.safeonline.auth.ws.Confirmation;
import net.link.safeonline.sdk.exception.RequestDeniedException;
import net.link.safeonline.sdk.trust.SafeOnlineTrustManager;
import net.link.safeonline.sdk.ws.AbstractMessageAccessor;
import net.link.safeonline.sdk.ws.LoggingHandler;
import net.link.safeonline.sdk.ws.WSSecurityClientHandler;
import net.link.safeonline.sdk.ws.exception.WSAuthenticationException;
import net.link.safeonline.sdk.ws.exception.WSClientTransportException;
import net.link.safeonline.ws.common.WSAuthenticationErrorCode;
import oasis.names.tc.saml._2_0.assertion.AssertionType;
import oasis.names.tc.saml._2_0.assertion.AttributeStatementType;
import oasis.names.tc.saml._2_0.assertion.AttributeType;
import oasis.names.tc.saml._2_0.assertion.NameIDType;
import oasis.names.tc.saml._2_0.protocol.StatusCodeType;
import oasis.names.tc.saml._2_0.protocol.StatusType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sun.xml.ws.client.ClientTransportException;


/**
 * Implementation of authentication client. This class is using JAX-WS and server-side SSL.
 * 
 * @author wvdhaute
 * 
 */
public class AuthenticationClientImpl extends AbstractMessageAccessor implements AuthenticationClient {

    private static final Log                    LOG = LogFactory.getLog(AuthenticationClientImpl.class);

    private final AuthenticationPort            port;

    private final W3CEndpointReference          endpoint;

    private AssertionType                       assertion;

    private DeviceAuthenticationInformationType deviceAuthenticationInformation;

    private AuthenticationStep                  authenticationStep;


    /**
     * Main constructor
     */
    public AuthenticationClientImpl(W3CEndpointReference endpoint) {

        this(endpoint, null, null);
    }

    /**
     * Main constructor.
     */
    public AuthenticationClientImpl(W3CEndpointReference endpoint, X509Certificate clientCertificate, PrivateKey clientPrivateKey) {

        AuthenticationService authenticationService = AuthenticationServiceFactory.newInstance();
        this.endpoint = endpoint;
        this.port = authenticationService.getPort(endpoint, AuthenticationPort.class, new AddressingFeature(true));

        registerMessageLoggerHandler(this.port);

        // TODO: disable logging when finished
        LoggingHandler.addNewHandler(this.port);
        setCaptureMessages(true);

        WSSecurityClientHandler.addNewHandler(this.port, clientCertificate, clientPrivateKey);
    }

    /**
     * {@inheritDoc}
     */
    public String authenticate(String applicationId, String deviceName, String language, Object deviceCredentials, PublicKey publicKey)
            throws RequestDeniedException, WSClientTransportException, WSAuthenticationException {

        LOG.debug("authentication for application " + applicationId + " using device " + deviceName);

        WSAuthenticationRequestType request = AuthenticationUtil.getAuthenticationRequest(applicationId, deviceName, language,
                deviceCredentials, publicKey);

        SafeOnlineTrustManager.configureSsl();

        WSAuthenticationResponseType response = getAuthenticateResponse(request);

        checkStatus(response);

        setAssertion(response);
        if (null != this.assertion)
            return getSubject();

        setDeviceAuthenticationInformation(response);
        setAuthenticationSteps(response);

        return null;
    }

    /**
     * {@inheritDoc}
     */
    public String getGlobalUsageAgreement()
            throws WSClientTransportException, RequestDeniedException, WSAuthenticationException {

        LOG.debug("get global usage agreement to be confirmed");

        WSAuthenticationGlobalUsageAgreementRequestType request = AuthenticationUtil.getGlobalUsageAgreementRequest();

        SafeOnlineTrustManager.configureSsl();

        WSAuthenticationGlobalUsageAgreementResponseType response = getGlobalUsageAgreementResponse(request);

        checkStatus(response);

        if (null != response.getGlobalUsageAgreement())
            return response.getGlobalUsageAgreement();

        setAssertion(response);
        setAuthenticationSteps(response);

        return null;
    }

    /**
     * {@inheritDoc}
     */
    public String confirmGlobalUsageAgreement(Confirmation confirmation)
            throws RequestDeniedException, WSClientTransportException, WSAuthenticationException {

        LOG.debug("confirm or reject global usage agreement: " + confirmation.getValue());

        WSAuthenticationGlobalUsageAgreementConfirmationType request = AuthenticationUtil
                                                                                         .getGlobalUsageAgreementConfirmationRequest(confirmation);

        SafeOnlineTrustManager.configureSsl();

        WSAuthenticationResponseType response = getGlobalUsageAgreementConfirmationResponse(request);

        checkStatus(response);

        setAssertion(response);
        if (null != this.assertion)
            return getSubject();

        setAuthenticationSteps(response);

        return null;

    }

    /**
     * {@inheritDoc}
     */
    public String getUsageAgreement()
            throws WSClientTransportException, RequestDeniedException, WSAuthenticationException {

        LOG.debug("get application's usage agreement to be confirmed or subscription required");

        WSAuthenticationUsageAgreementRequestType request = AuthenticationUtil.getUsageAgreementRequest();

        SafeOnlineTrustManager.configureSsl();

        WSAuthenticationUsageAgreementResponseType response = getUsageAgreementResponse(request);

        checkStatus(response);

        if (null != response.getUsageAgreement())
            return response.getUsageAgreement();

        setAssertion(response);
        setAuthenticationSteps(response);

        return null;
    }

    /**
     * {@inheritDoc}
     */
    public String confirmUsageAgreement(Confirmation confirmation)
            throws RequestDeniedException, WSClientTransportException, WSAuthenticationException {

        LOG.debug("confirm or reject application usage agreement: " + confirmation.getValue());

        WSAuthenticationUsageAgreementConfirmationType request = AuthenticationUtil.getUsageAgreementConfirmationRequest(confirmation);

        SafeOnlineTrustManager.configureSsl();

        WSAuthenticationResponseType response = getUsageAgreementConfirmationResponse(request);

        checkStatus(response);

        setAssertion(response);
        if (null != this.assertion)
            return getSubject();

        setAuthenticationSteps(response);

        return null;
    }

    /**
     * {@inheritDoc}
     */
    public List<Attribute> getIdentity()
            throws RequestDeniedException, WSClientTransportException, WSAuthenticationException {

        LOG.debug("get application's identity to be confirmed");

        WSAuthenticationIdentityRequestType request = AuthenticationUtil.getIdentityRequest();

        SafeOnlineTrustManager.configureSsl();

        WSAuthenticationResponseType response = getIdentityResponse(request);

        checkStatus(response);
        setAssertion(response);
        setAuthenticationSteps(response);

        if (null != response.getAssertion()) {
            // either authentication is successful and SAML v2.0 assertion containing subject information is returned, or a SAML v2.0
            // assertion containing an attribute statement is returned.
            if (null == response.getAssertion().get(0).getSubject()) {
                List<Attribute> identity = new LinkedList<Attribute>();
                AttributeStatementType attributeStatement = (AttributeStatementType) response
                                                                                             .getAssertion()
                                                                                             .get(0)
                                                                                             .getStatementOrAuthnStatementOrAuthzDecisionStatement()
                                                                                             .get(0);
                for (Object attributeOrEncryptedAttribute : attributeStatement.getAttributeOrEncryptedAttribute()) {
                    AttributeType attributeType = (AttributeType) attributeOrEncryptedAttribute;
                    identity.add(new Attribute(attributeType));
                }
                return identity;
            }
        }

        setAssertion(response);
        setAuthenticationSteps(response);

        return null;
    }

    /**
     * {@inheritDoc}
     */
    public String confirmIdentity(Confirmation confirmation)
            throws RequestDeniedException, WSClientTransportException, WSAuthenticationException {

        LOG.debug("confirm or reject application's identity: " + confirmation.getValue());

        WSAuthenticationIdentityConfirmationType request = AuthenticationUtil.getIdentityConfirmationRequest(confirmation);

        SafeOnlineTrustManager.configureSsl();

        WSAuthenticationResponseType response = getIdentityConfirmationResponse(request);

        checkStatus(response);

        setAssertion(response);
        if (null != this.assertion)
            return getSubject();

        setAuthenticationSteps(response);

        return null;

    }

    /**
     * {@inheritDoc}
     */
    public List<Attribute> getMissingAttributes()
            throws RequestDeniedException, WSClientTransportException, WSAuthenticationException {

        LOG.debug("get missing attributes for application");

        WSAuthenticationMissingAttributesRequestType request = AuthenticationUtil.getMissingAttributesRequest();

        SafeOnlineTrustManager.configureSsl();

        WSAuthenticationResponseType response = getMissingAttributesResponse(request);

        checkStatus(response);
        setAssertion(response);
        setAuthenticationSteps(response);

        if (null != response.getAssertion()) {
            // either authentication is successful and SAML v2.0 assertion containing subject information is returned, or a SAML v2.0
            // assertion containing an attribute statement is returned.
            if (null == response.getAssertion().get(0).getSubject()) {
                List<Attribute> missingAttributes = new LinkedList<Attribute>();
                AttributeStatementType attributeStatement = (AttributeStatementType) response
                                                                                             .getAssertion()
                                                                                             .get(0)
                                                                                             .getStatementOrAuthnStatementOrAuthzDecisionStatement()
                                                                                             .get(0);
                for (Object attributeOrEncryptedAttribute : attributeStatement.getAttributeOrEncryptedAttribute()) {
                    AttributeType attributeType = (AttributeType) attributeOrEncryptedAttribute;
                    missingAttributes.add(new Attribute(attributeType));
                }
                return missingAttributes;
            }
        }

        setAssertion(response);
        setAuthenticationSteps(response);

        return null;
    }

    /**
     * {@inheritDoc}
     */
    public String saveMissingAttributes(List<Attribute> missingAttributes)
            throws RequestDeniedException, WSClientTransportException, WSAuthenticationException {

        LOG.debug("save missing attributes");

        WSAuthenticationMissingAttributesSaveRequestType request = AuthenticationUtil.getMissingAttributesSaveRequest(missingAttributes);

        SafeOnlineTrustManager.configureSsl();

        WSAuthenticationResponseType response = getMissingAttributesSaveResponse(request);

        checkStatus(response);

        setAssertion(response);
        if (null != this.assertion)
            return getSubject();

        setAuthenticationSteps(response);

        return null;

    }

    /**
     * {@inheritDoc}
     */
    public AssertionType getAssertion() {

        return this.assertion;
    }

    /**
     * {@inheritDoc}
     */
    public DeviceAuthenticationInformationType getDeviceAuthenticationInformation() {

        return this.deviceAuthenticationInformation;
    }

    /**
     * {@inheritDoc}
     */
    public AuthenticationStep getAuthenticationStep() {

        return this.authenticationStep;
    }

    private String getSubject() {

        for (JAXBElement<?> object : this.assertion.getSubject().getContent()) {
            if (object.getDeclaredType().equals(NameIDType.class)) {
                NameIDType nameIDType = (NameIDType) object.getValue();
                return nameIDType.getValue();
            }
        }

        return null;
    }

    private WSAuthenticationResponseType getAuthenticateResponse(WSAuthenticationRequestType request)
            throws WSClientTransportException {

        try {
            return this.port.authenticate(request);
        } catch (ClientTransportException e) {
            throw new WSClientTransportException(this.endpoint.toString());
        } catch (Exception e) {
            throw retrieveHeadersFromException(e);
        } finally {
            retrieveHeadersFromPort(this.port);
        }
    }

    private void checkStatus(WSAuthenticationResponseType response)
            throws RequestDeniedException, WSClientTransportException, WSAuthenticationException {

        StatusType status = response.getStatus();
        StatusCodeType statusCode = status.getStatusCode();
        String statusCodeValue = statusCode.getValue();
        WSAuthenticationErrorCode wsAuthenticationErrorCode = WSAuthenticationErrorCode.getWSAuthenticationErrorCode(statusCodeValue);
        if (WSAuthenticationErrorCode.SUCCESS != wsAuthenticationErrorCode) {
            LOG.error("status code: " + statusCodeValue);
            LOG.error("status message: " + status.getStatusMessage());
            if (WSAuthenticationErrorCode.REQUEST_DENIED == wsAuthenticationErrorCode)
                throw new RequestDeniedException();
            else if (WSAuthenticationErrorCode.REQUEST_FAILED == wsAuthenticationErrorCode)
                throw new WSClientTransportException(this.endpoint.toString());
            else
                throw new WSAuthenticationException(wsAuthenticationErrorCode, status.getStatusMessage());
        }
    }

    private WSAuthenticationGlobalUsageAgreementResponseType getGlobalUsageAgreementResponse(
                                                                                             WSAuthenticationGlobalUsageAgreementRequestType request)
            throws WSClientTransportException {

        try {
            return this.port.requestGlobalUsageAgreement(request);
        } catch (ClientTransportException e) {
            throw new WSClientTransportException(this.endpoint.toString());
        } catch (Exception e) {
            throw retrieveHeadersFromException(e);
        } finally {
            retrieveHeadersFromPort(this.port);
        }
    }

    private WSAuthenticationResponseType getGlobalUsageAgreementConfirmationResponse(
                                                                                     WSAuthenticationGlobalUsageAgreementConfirmationType request)
            throws WSClientTransportException {

        try {
            return this.port.confirmGlobalUsageAgreement(request);
        } catch (ClientTransportException e) {
            throw new WSClientTransportException(this.endpoint.toString());
        } catch (Exception e) {
            throw retrieveHeadersFromException(e);
        } finally {
            retrieveHeadersFromPort(this.port);
        }

    }

    private WSAuthenticationUsageAgreementResponseType getUsageAgreementResponse(WSAuthenticationUsageAgreementRequestType request)
            throws WSClientTransportException {

        try {
            return this.port.requestUsageAgreement(request);
        } catch (ClientTransportException e) {
            throw new WSClientTransportException(this.endpoint.toString());
        } catch (Exception e) {
            throw retrieveHeadersFromException(e);
        } finally {
            retrieveHeadersFromPort(this.port);
        }
    }

    private WSAuthenticationResponseType getUsageAgreementConfirmationResponse(WSAuthenticationUsageAgreementConfirmationType request)
            throws WSClientTransportException {

        try {
            return this.port.confirmUsageAgreement(request);
        } catch (ClientTransportException e) {
            throw new WSClientTransportException(this.endpoint.toString());
        } catch (Exception e) {
            throw retrieveHeadersFromException(e);
        } finally {
            retrieveHeadersFromPort(this.port);
        }
    }

    private WSAuthenticationResponseType getIdentityResponse(WSAuthenticationIdentityRequestType request)
            throws WSClientTransportException {

        try {
            return this.port.requestIdentity(request);
        } catch (ClientTransportException e) {
            throw new WSClientTransportException(this.endpoint.toString());
        } catch (Exception e) {
            throw retrieveHeadersFromException(e);
        } finally {
            retrieveHeadersFromPort(this.port);
        }
    }

    private WSAuthenticationResponseType getIdentityConfirmationResponse(WSAuthenticationIdentityConfirmationType request)
            throws WSClientTransportException {

        try {
            return this.port.confirmIdentity(request);
        } catch (ClientTransportException e) {
            throw new WSClientTransportException(this.endpoint.toString());
        } catch (Exception e) {
            throw retrieveHeadersFromException(e);
        } finally {
            retrieveHeadersFromPort(this.port);
        }

    }

    private WSAuthenticationResponseType getMissingAttributesResponse(WSAuthenticationMissingAttributesRequestType request)
            throws WSClientTransportException {

        try {
            return this.port.requestMissingAttributes(request);
        } catch (ClientTransportException e) {
            throw new WSClientTransportException(this.endpoint.toString());
        } catch (Exception e) {
            throw retrieveHeadersFromException(e);
        } finally {
            retrieveHeadersFromPort(this.port);
        }
    }

    private WSAuthenticationResponseType getMissingAttributesSaveResponse(WSAuthenticationMissingAttributesSaveRequestType request)
            throws WSClientTransportException {

        try {
            return this.port.saveMissingAttributes(request);
        } catch (ClientTransportException e) {
            throw new WSClientTransportException(this.endpoint.toString());
        } catch (Exception e) {
            throw retrieveHeadersFromException(e);
        } finally {
            retrieveHeadersFromPort(this.port);
        }
    }

    private void setAssertion(WSAuthenticationResponseType response) {

        if (!response.getAssertion().isEmpty()) {
            if (null != response.getAssertion().get(0).getSubject()) {
                this.assertion = response.getAssertion().get(0);
            }
        }
    }

    private void setDeviceAuthenticationInformation(WSAuthenticationResponseType response) {

        this.deviceAuthenticationInformation = response.getDeviceAuthenticationInformation();
    }

    private void setAuthenticationSteps(WSAuthenticationResponseType response) {

        if (null == response.getAuthenticationStep()) {
            this.authenticationStep = null;
            return;
        }

        this.authenticationStep = AuthenticationStep.getAuthenticationStep(response.getAuthenticationStep());

    }
}
