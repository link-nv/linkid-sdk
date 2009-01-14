/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.ws.auth;

import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.security.interfaces.DSAPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBElement;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.ws.soap.AddressingFeature;
import javax.xml.ws.wsaddressing.W3CEndpointReference;

import net.lin_k.safe_online.auth.AuthenticationPort;
import net.lin_k.safe_online.auth.AuthenticationService;
import net.lin_k.safe_online.auth.DeviceAuthenticationInformationType;
import net.lin_k.safe_online.auth.DeviceCredentialsType;
import net.lin_k.safe_online.auth.NameValuePairType;
import net.lin_k.safe_online.auth.WSAuthenticationGlobalUsageAgreementConfirmationType;
import net.lin_k.safe_online.auth.WSAuthenticationGlobalUsageAgreementRequestType;
import net.lin_k.safe_online.auth.WSAuthenticationGlobalUsageAgreementResponseType;
import net.lin_k.safe_online.auth.WSAuthenticationIdentityConfirmationType;
import net.lin_k.safe_online.auth.WSAuthenticationIdentityRequestType;
import net.lin_k.safe_online.auth.WSAuthenticationRequestType;
import net.lin_k.safe_online.auth.WSAuthenticationResponseType;
import net.lin_k.safe_online.auth.WSAuthenticationStepType;
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
import oasis.names.tc.saml._2_0.protocol.RequestAbstractType;
import oasis.names.tc.saml._2_0.protocol.StatusCodeType;
import oasis.names.tc.saml._2_0.protocol.StatusType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opensaml.common.SAMLVersion;
import org.opensaml.common.impl.SecureRandomIdentifierGenerator;
import org.w3._2000._09.xmldsig_.DSAKeyValueType;
import org.w3._2000._09.xmldsig_.KeyInfoType;
import org.w3._2000._09.xmldsig_.ObjectFactory;
import org.w3._2000._09.xmldsig_.RSAKeyValueType;

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

    private List<AuthenticationStep>            authenticationSteps;


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

        WSAuthenticationRequestType request = getAuthenticationRequest(applicationId, deviceName, language, deviceCredentials, publicKey);

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

        WSAuthenticationGlobalUsageAgreementRequestType request = getGlobalUsageAgreementRequest();

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

        WSAuthenticationGlobalUsageAgreementConfirmationType request = getGlobalUsageAgreementConfirmationRequest(confirmation);

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

        WSAuthenticationUsageAgreementRequestType request = getUsageAgreementRequest();

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

        WSAuthenticationUsageAgreementConfirmationType request = getUsageAgreementConfirmationRequest(confirmation);

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
    public List<AttributeType> getIdentity()
            throws RequestDeniedException, WSClientTransportException, WSAuthenticationException {

        LOG.debug("get application's identity to be confirmed");

        WSAuthenticationIdentityRequestType request = getIdentityRequest();

        SafeOnlineTrustManager.configureSsl();

        WSAuthenticationResponseType response = getIdentityResponse(request);

        checkStatus(response);
        setAssertion(response);
        setAuthenticationSteps(response);

        if (null != response.getAssertion()) {
            // either authentication is successful and SAML v2.0 assertion containing subject information is returned, or a SAML v2.0
            // assertion containing an attribute statement is returned.
            if (null == response.getAssertion().get(0).getSubject()) {
                List<AttributeType> identity = new LinkedList<AttributeType>();
                AttributeStatementType attributeStatement = (AttributeStatementType) response
                                                                                             .getAssertion()
                                                                                             .get(0)
                                                                                             .getStatementOrAuthnStatementOrAuthzDecisionStatement()
                                                                                             .get(0);
                for (Object attributeOrEncryptedAttribute : attributeStatement.getAttributeOrEncryptedAttribute()) {
                    AttributeType attribute = (AttributeType) attributeOrEncryptedAttribute;
                    identity.add(attribute);
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

        WSAuthenticationIdentityConfirmationType request = getIdentityConfirmationRequest(confirmation);

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
    public List<AuthenticationStep> getAuthenticationSteps() {

        return this.authenticationSteps;
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

    private void setRequest(RequestAbstractType request) {

        SecureRandomIdentifierGenerator idGenerator;
        try {
            idGenerator = new SecureRandomIdentifierGenerator();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("secure random init error: " + e.getMessage(), e);
        }
        String id = idGenerator.generateIdentifier();
        XMLGregorianCalendar now = getCurrentXmlGregorianCalendar();

        request.setID(id);
        request.setVersion(SAMLVersion.VERSION_20.toString());
        request.setIssueInstant(now);
    }

    @SuppressWarnings("unchecked")
    private WSAuthenticationRequestType getAuthenticationRequest(String applicationId, String deviceName, String language,
                                                                 Object deviceCredentials, PublicKey publicKey) {

        WSAuthenticationRequestType authenticationRequest = new WSAuthenticationRequestType();
        setRequest(authenticationRequest);

        // Issuer
        NameIDType issuerName = new NameIDType();
        issuerName.setValue(applicationId);
        authenticationRequest.setIssuer(issuerName);

        authenticationRequest.setApplicationId(applicationId);
        authenticationRequest.setDeviceName(deviceName);
        authenticationRequest.setLanguage(language);

        if (null != deviceCredentials) {
            DeviceCredentialsType deviceCredentialsType = new DeviceCredentialsType();
            if (deviceCredentials instanceof Map) {
                for (Map.Entry<String, String> entry : ((Map<String, String>) deviceCredentials).entrySet()) {
                    NameValuePairType nameValuePair = new NameValuePairType();
                    nameValuePair.setName(entry.getKey());
                    nameValuePair.setValue(entry.getValue());
                    deviceCredentialsType.getNameValuePair().add(nameValuePair);
                }
            } else if (deviceCredentials instanceof JAXBElement<?>) {
                // XXX: change following, need JAXB object ...
                deviceCredentialsType.getAny().add(deviceCredentials);
            }
            authenticationRequest.setDeviceCredentials(deviceCredentialsType);
        }

        if (null != publicKey) {
            KeyInfoType keyInfo = getKeyInfo(publicKey);
            authenticationRequest.setKeyInfo(keyInfo);
        }

        return authenticationRequest;
    }

    /**
     * Converts public key to XML DSig KeyInfoType
     * 
     */
    private KeyInfoType getKeyInfo(PublicKey publicKey) {

        KeyInfoType keyInfo = new KeyInfoType();
        ObjectFactory dsigObjectFactory = new ObjectFactory();

        if (publicKey instanceof RSAPublicKey) {
            RSAPublicKey rsaPublicKey = (RSAPublicKey) publicKey;
            RSAKeyValueType rsaKeyValue = new RSAKeyValueType();
            rsaKeyValue.setModulus(rsaPublicKey.getModulus().toByteArray());
            rsaKeyValue.setExponent(rsaPublicKey.getPublicExponent().toByteArray());
            keyInfo.getContent().add(dsigObjectFactory.createRSAKeyValue(rsaKeyValue));
        } else if (publicKey instanceof DSAPublicKey) {
            DSAPublicKey dsaPublicKey = (DSAPublicKey) publicKey;
            DSAKeyValueType dsaKeyValue = new DSAKeyValueType();
            dsaKeyValue.setY(dsaPublicKey.getY().toByteArray());
            dsaKeyValue.setG(dsaPublicKey.getParams().getG().toByteArray());
            dsaKeyValue.setP(dsaPublicKey.getParams().getP().toByteArray());
            dsaKeyValue.setQ(dsaPublicKey.getParams().getQ().toByteArray());
            keyInfo.getContent().add(dsaKeyValue);
        } else
            throw new IllegalArgumentException("Only RSAPublicKey and DSAPublicKey are supported");

        return keyInfo;
    }

    private XMLGregorianCalendar getCurrentXmlGregorianCalendar() {

        DatatypeFactory datatypeFactory;
        try {
            datatypeFactory = DatatypeFactory.newInstance();
        } catch (DatatypeConfigurationException e) {
            LOG.error("datatype configuration exception", e);
            throw new RuntimeException("datatype configuration exception: " + e.getMessage());
        }

        GregorianCalendar gregorianCalendar = new GregorianCalendar();
        Date now = new Date();
        gregorianCalendar.setTime(now);
        XMLGregorianCalendar currentXmlGregorianCalendar = datatypeFactory.newXMLGregorianCalendar(gregorianCalendar);
        return currentXmlGregorianCalendar;
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

    private WSAuthenticationGlobalUsageAgreementRequestType getGlobalUsageAgreementRequest() {

        WSAuthenticationGlobalUsageAgreementRequestType request = new WSAuthenticationGlobalUsageAgreementRequestType();
        setRequest(request);
        return request;
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

    private WSAuthenticationGlobalUsageAgreementConfirmationType getGlobalUsageAgreementConfirmationRequest(Confirmation confirmation) {

        WSAuthenticationGlobalUsageAgreementConfirmationType request = new WSAuthenticationGlobalUsageAgreementConfirmationType();
        setRequest(request);
        request.setConfirmation(confirmation.getValue());
        return request;
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

    private WSAuthenticationUsageAgreementRequestType getUsageAgreementRequest() {

        WSAuthenticationUsageAgreementRequestType request = new WSAuthenticationUsageAgreementRequestType();
        setRequest(request);
        return request;
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

    private WSAuthenticationUsageAgreementConfirmationType getUsageAgreementConfirmationRequest(Confirmation confirmation) {

        WSAuthenticationUsageAgreementConfirmationType request = new WSAuthenticationUsageAgreementConfirmationType();
        setRequest(request);
        request.setConfirmation(confirmation.getValue());
        return request;
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

    private WSAuthenticationIdentityRequestType getIdentityRequest() {

        WSAuthenticationIdentityRequestType request = new WSAuthenticationIdentityRequestType();
        setRequest(request);
        return request;
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

    private WSAuthenticationIdentityConfirmationType getIdentityConfirmationRequest(Confirmation confirmation) {

        WSAuthenticationIdentityConfirmationType request = new WSAuthenticationIdentityConfirmationType();
        setRequest(request);
        request.setConfirmation(confirmation.getValue());
        return request;
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

        if (null == response.getWSAuthenticationStep()) {
            this.authenticationSteps = null;
            return;
        }

        this.authenticationSteps = new LinkedList<AuthenticationStep>();
        for (WSAuthenticationStepType wsAuthenticationStep : response.getWSAuthenticationStep()) {
            this.authenticationSteps.add(AuthenticationStep.getAuthenticationStep(wsAuthenticationStep.getAuthenticationStep()));
        }

    }
}
