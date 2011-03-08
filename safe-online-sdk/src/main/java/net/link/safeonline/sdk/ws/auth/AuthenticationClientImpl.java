/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.ws.auth;

import com.sun.xml.ws.client.ClientTransportException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.LinkedList;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.ws.soap.AddressingFeature;
import javax.xml.ws.wsaddressing.W3CEndpointReference;
import net.lin_k.safe_online.auth.*;
import net.link.safeonline.auth.ws.AuthenticationStep;
import net.link.safeonline.auth.ws.Confirmation;
import net.link.safeonline.auth.ws.WSAuthenticationServiceFactory;
import net.link.safeonline.sdk.logging.exception.RequestDeniedException;
import net.link.safeonline.sdk.logging.exception.WSAuthenticationException;
import net.link.safeonline.sdk.logging.exception.WSClientTransportException;
import net.link.safeonline.sdk.ws.AbstractWSClient;
import net.link.safeonline.sdk.ws.AuthenticationErrorCode;
import net.link.util.ws.pkix.wssecurity.WSSecurityClientHandler;
import oasis.names.tc.saml._2_0.assertion.AssertionType;
import oasis.names.tc.saml._2_0.assertion.AttributeStatementType;
import oasis.names.tc.saml._2_0.assertion.AttributeType;
import oasis.names.tc.saml._2_0.assertion.NameIDType;
import oasis.names.tc.saml._2_0.protocol.StatusCodeType;
import oasis.names.tc.saml._2_0.protocol.StatusType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Implementation of authentication client. This class is using JAX-WS and server-side SSL.
 *
 * @author wvdhaute
 */
public class AuthenticationClientImpl extends AbstractWSClient implements AuthenticationClient {

    private static final Log LOG = LogFactory.getLog( AuthenticationClientImpl.class );

    private final WSAuthenticationPort port;

    private final W3CEndpointReference endpoint;

    private AssertionType assertion;

    private DeviceAuthenticationInformationType deviceAuthenticationInformation;

    private AuthenticationStep authenticationStep;

    /**
     * Main constructor
     *
     * @param endpoint endpoint
     */
    public AuthenticationClientImpl(W3CEndpointReference endpoint) {

        this( endpoint, null, null, null, null, null );
    }

    /**
     * Main constructor.
     *
     * @param endpoint          endpoint
     * @param clientCertificate the X509 certificate to use for WS-Security signature.
     * @param clientPrivateKey  the private key corresponding with the client certificate.
     * @param serverCertificate the X509 certificate of the server
     * @param maxOffset         the maximum offset of the WS-Security timestamp received. If <code>null</code> default offset configured in
     *                          {@link WSSecurityClientHandler} will be used.
     * @param sslCertificate    If not <code>null</code> will verify the server SSL {@link X509Certificate}.
     */
    public AuthenticationClientImpl(W3CEndpointReference endpoint, X509Certificate clientCertificate, PrivateKey clientPrivateKey,
                                    X509Certificate serverCertificate, Long maxOffset, X509Certificate sslCertificate) {

        WSAuthenticationService authenticationService = WSAuthenticationServiceFactory.newInstance();
        this.endpoint = endpoint;
        port = authenticationService.getPort( endpoint, WSAuthenticationPort.class, new AddressingFeature( true ) );

        registerMessageLoggerHandler( port );

        registerTrustManager( port, sslCertificate );

        WSSecurityClientHandler.addNewHandler( port, clientCertificate, clientPrivateKey, serverCertificate, maxOffset );
    }

    /**
     * {@inheritDoc}
     */
    public String authenticate(String applicationName, String deviceName, String language, Object deviceCredentials, PublicKey publicKey)
            throws RequestDeniedException, WSAuthenticationException, WSClientTransportException {

        LOG.debug( "authentication for application " + applicationName + " using device " + deviceName );

        WSAuthenticationRequestType request = AuthenticationUtil.getAuthenticationRequest( applicationName, deviceName, language,
                                                                                           deviceCredentials, publicKey );

        WSAuthenticationResponseType response = getAuthenticateResponse( request );

        validateStatus( response );

        setAssertion( response );
        if (null != assertion)
            return getSubject();

        setDeviceAuthenticationInformation( response );
        setAuthenticationSteps( response );

        return null;
    }

    /**
     * {@inheritDoc}
     */
    public String getGlobalUsageAgreement()
            throws WSClientTransportException, RequestDeniedException, WSAuthenticationException {

        LOG.debug( "get global usage agreement to be confirmed" );

        WSAuthenticationGlobalUsageAgreementRequestType request = AuthenticationUtil.getGlobalUsageAgreementRequest();

        WSAuthenticationGlobalUsageAgreementResponseType response = getGlobalUsageAgreementResponse( request );

        validateStatus( response );

        if (null != response.getGlobalUsageAgreement())
            return response.getGlobalUsageAgreement();

        setAssertion( response );
        setAuthenticationSteps( response );

        return null;
    }

    /**
     * {@inheritDoc}
     */
    public String confirmGlobalUsageAgreement(Confirmation confirmation)
            throws RequestDeniedException, WSClientTransportException, WSAuthenticationException {

        LOG.debug( "confirm or reject global usage agreement: " + confirmation.getValue() );

        WSAuthenticationGlobalUsageAgreementConfirmationType request = AuthenticationUtil.getGlobalUsageAgreementConfirmationRequest(
                confirmation );

        WSAuthenticationResponseType response = getGlobalUsageAgreementConfirmationResponse( request );

        validateStatus( response );

        setAssertion( response );
        if (null != assertion)
            return getSubject();

        setAuthenticationSteps( response );

        return null;
    }

    /**
     * {@inheritDoc}
     */
    public String getUsageAgreement()
            throws WSClientTransportException, RequestDeniedException, WSAuthenticationException {

        LOG.debug( "get application's usage agreement to be confirmed or subscription required" );

        WSAuthenticationUsageAgreementRequestType request = AuthenticationUtil.getUsageAgreementRequest();

        WSAuthenticationUsageAgreementResponseType response = getUsageAgreementResponse( request );

        validateStatus( response );

        if (null != response.getUsageAgreement())
            return response.getUsageAgreement();

        setAssertion( response );
        setAuthenticationSteps( response );

        return null;
    }

    /**
     * {@inheritDoc}
     */
    public String confirmUsageAgreement(Confirmation confirmation)
            throws RequestDeniedException, WSClientTransportException, WSAuthenticationException {

        LOG.debug( "confirm or reject application usage agreement: " + confirmation.getValue() );

        WSAuthenticationUsageAgreementConfirmationType request = AuthenticationUtil.getUsageAgreementConfirmationRequest( confirmation );

        WSAuthenticationResponseType response = getUsageAgreementConfirmationResponse( request );

        validateStatus( response );

        setAssertion( response );
        if (null != assertion)
            return getSubject();

        setAuthenticationSteps( response );

        return null;
    }

    /**
     * {@inheritDoc}
     */
    public List<AttributeIdentitySDK> getIdentity()
            throws RequestDeniedException, WSClientTransportException, WSAuthenticationException {

        LOG.debug( "get application's identity to be confirmed" );

        WSAuthenticationIdentityRequestType request = AuthenticationUtil.getIdentityRequest();

        WSAuthenticationResponseType response = getIdentityResponse( request );

        validateStatus( response );
        setAssertion( response );
        setAuthenticationSteps( response );

        if (null != response.getAssertion())
            // either authentication is successful and SAML v2.0 assertion containing subject information is returned, or a SAML v2.0
            // assertion containing an attribute statement is returned.
            if (null == response.getAssertion().get( 0 ).getSubject()) {
                List<AttributeIdentitySDK> identity = new LinkedList<AttributeIdentitySDK>();
                AttributeStatementType attributeStatement = (AttributeStatementType) response.getAssertion()
                        .get( 0 )
                        .getStatementOrAuthnStatementOrAuthzDecisionStatement()
                        .get( 0 );
                for (Object attributeOrEncryptedAttribute : attributeStatement.getAttributeOrEncryptedAttribute()) {
                    AttributeType attributeType = (AttributeType) attributeOrEncryptedAttribute;
                    identity.add( new AttributeIdentitySDK( attributeType ) );
                }
                return identity;
            }

        setAssertion( response );
        setAuthenticationSteps( response );

        return null;
    }

    /**
     * {@inheritDoc}
     */
    public String confirmIdentity(List<AttributeIdentitySDK> attributes)
            throws RequestDeniedException, WSClientTransportException, WSAuthenticationException {

        LOG.debug( "confirm the application's identity" );

        WSAuthenticationIdentityConfirmationType request = AuthenticationUtil.getIdentityConfirmationRequest( attributes );

        WSAuthenticationResponseType response = getIdentityConfirmationResponse( request );

        validateStatus( response );

        setAssertion( response );
        if (null != assertion)
            return getSubject();

        setAuthenticationSteps( response );

        return null;
    }

    /**
     * {@inheritDoc}
     */
    public AssertionType getAssertion() {

        return assertion;
    }

    /**
     * {@inheritDoc}
     */
    public DeviceAuthenticationInformationType getDeviceAuthenticationInformation() {

        return deviceAuthenticationInformation;
    }

    /**
     * {@inheritDoc}
     */
    public AuthenticationStep getAuthenticationStep() {

        return authenticationStep;
    }

    private String getSubject() {

        for (JAXBElement<?> object : assertion.getSubject().getContent())
            if (object.getDeclaredType().equals( NameIDType.class )) {
                NameIDType nameIDType = (NameIDType) object.getValue();
                return nameIDType.getValue();
            }

        return null;
    }

    private WSAuthenticationResponseType getAuthenticateResponse(WSAuthenticationRequestType request)
            throws WSClientTransportException {

        try {
            return port.authenticate( request );
        } catch (ClientTransportException e) {
            throw new WSClientTransportException( endpoint.toString(), e );
        } catch (Exception e) {
            throw retrieveHeadersFromException( e );
        } finally {
            retrieveHeadersFromPort( port );
        }
    }

    private void validateStatus(WSAuthenticationResponseType response)
            throws RequestDeniedException, WSClientTransportException, WSAuthenticationException {

        StatusType status = response.getStatus();
        StatusCodeType statusCode = status.getStatusCode();
        String statusCodeValue = statusCode.getValue();
        AuthenticationErrorCode authenticationErrorCode = AuthenticationErrorCode.getWSAuthenticationErrorCode( statusCodeValue );
        if (AuthenticationErrorCode.SUCCESS != authenticationErrorCode) {
            LOG.error( "status code: " + statusCodeValue );
            LOG.error( "status message: " + status.getStatusMessage() );
            if (AuthenticationErrorCode.REQUEST_DENIED == authenticationErrorCode)
                throw new RequestDeniedException();
            else if (AuthenticationErrorCode.REQUEST_FAILED == authenticationErrorCode)
                throw new WSClientTransportException( endpoint.toString() );
            else
                throw new WSAuthenticationException( authenticationErrorCode, status.getStatusMessage() );
        }
    }

    private WSAuthenticationGlobalUsageAgreementResponseType getGlobalUsageAgreementResponse(
            WSAuthenticationGlobalUsageAgreementRequestType request)
            throws WSClientTransportException {

        try {
            return port.requestGlobalUsageAgreement( request );
        } catch (ClientTransportException e) {
            throw new WSClientTransportException( endpoint.toString(), e );
        } catch (Exception e) {
            throw retrieveHeadersFromException( e );
        } finally {
            retrieveHeadersFromPort( port );
        }
    }

    private WSAuthenticationResponseType getGlobalUsageAgreementConfirmationResponse(
            WSAuthenticationGlobalUsageAgreementConfirmationType request)
            throws WSClientTransportException {

        try {
            return port.confirmGlobalUsageAgreement( request );
        } catch (ClientTransportException e) {
            throw new WSClientTransportException( endpoint.toString(), e );
        } catch (Exception e) {
            throw retrieveHeadersFromException( e );
        } finally {
            retrieveHeadersFromPort( port );
        }
    }

    private WSAuthenticationUsageAgreementResponseType getUsageAgreementResponse(WSAuthenticationUsageAgreementRequestType request)
            throws WSClientTransportException {

        try {
            return port.requestUsageAgreement( request );
        } catch (ClientTransportException e) {
            throw new WSClientTransportException( endpoint.toString(), e );
        } catch (Exception e) {
            throw retrieveHeadersFromException( e );
        } finally {
            retrieveHeadersFromPort( port );
        }
    }

    private WSAuthenticationResponseType getUsageAgreementConfirmationResponse(WSAuthenticationUsageAgreementConfirmationType request)
            throws WSClientTransportException {

        try {
            return port.confirmUsageAgreement( request );
        } catch (ClientTransportException e) {
            throw new WSClientTransportException( endpoint.toString(), e );
        } catch (Exception e) {
            throw retrieveHeadersFromException( e );
        } finally {
            retrieveHeadersFromPort( port );
        }
    }

    private WSAuthenticationResponseType getIdentityResponse(WSAuthenticationIdentityRequestType request)
            throws WSClientTransportException {

        try {
            return port.requestIdentity( request );
        } catch (ClientTransportException e) {
            throw new WSClientTransportException( endpoint.toString(), e );
        } catch (Exception e) {
            throw retrieveHeadersFromException( e );
        } finally {
            retrieveHeadersFromPort( port );
        }
    }

    private WSAuthenticationResponseType getIdentityConfirmationResponse(WSAuthenticationIdentityConfirmationType request)
            throws WSClientTransportException {

        try {
            return port.confirmIdentity( request );
        } catch (ClientTransportException e) {
            throw new WSClientTransportException( endpoint.toString(), e );
        } catch (Exception e) {
            throw retrieveHeadersFromException( e );
        } finally {
            retrieveHeadersFromPort( port );
        }
    }

    private void setAssertion(WSAuthenticationResponseType response) {

        if (!response.getAssertion().isEmpty())
            if (null != response.getAssertion().get( 0 ).getSubject())
                assertion = response.getAssertion().get( 0 );
    }

    private void setDeviceAuthenticationInformation(WSAuthenticationResponseType response) {

        deviceAuthenticationInformation = response.getDeviceAuthenticationInformation();
    }

    private void setAuthenticationSteps(WSAuthenticationResponseType response) {

        if (null == response.getAuthenticationStep()) {
            authenticationStep = null;
            return;
        }

        authenticationStep = AuthenticationStep.getAuthenticationStep( response.getAuthenticationStep() );
    }
}
