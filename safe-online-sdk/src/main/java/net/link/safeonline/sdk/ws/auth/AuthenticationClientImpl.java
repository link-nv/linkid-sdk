/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.ws.auth;

import com.lyndir.lhunath.opal.system.logging.Logger;
import com.sun.xml.ws.client.ClientTransportException;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.LinkedList;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.ws.soap.AddressingFeature;
import javax.xml.ws.wsaddressing.W3CEndpointReference;
import net.lin_k.safe_online.auth.*;
import net.link.safeonline.sdk.api.attribute.AttributeIdentitySDK;
import net.link.safeonline.sdk.api.exception.*;
import net.link.safeonline.sdk.api.ws.auth.*;
import net.link.safeonline.sdk.api.ws.auth.client.AuthenticationClient;
import net.link.safeonline.ws.auth.WSAuthenticationServiceFactory;
import net.link.util.ws.AbstractWSClient;
import net.link.util.ws.security.WSSecurityConfiguration;
import net.link.util.ws.security.WSSecurityHandler;
import oasis.names.tc.saml._2_0.assertion.*;
import oasis.names.tc.saml._2_0.protocol.StatusCodeType;
import oasis.names.tc.saml._2_0.protocol.StatusType;
import org.jetbrains.annotations.Nullable;


/**
 * Implementation of authentication client. This class is using JAX-WS and server-side SSL.
 *
 * @author wvdhaute
 */
public class AuthenticationClientImpl extends AbstractWSClient<WSAuthenticationPort> implements
        AuthenticationClient<AssertionType, DeviceAuthenticationInformationType> {

    private static final Logger logger = Logger.get( AuthenticationClientImpl.class );

    private DeviceAuthenticationInformationType deviceAuthenticationInformation;
    private AuthenticationStep                  authenticationStep;
    private AssertionType                       assertion;

    /**
     * Main constructor.
     *
     * @param endpoint       endpoint
     * @param sslCertificate If not {@code null} will verify the server SSL {@link X509Certificate}.
     * @param configuration  The WS-Security configuration.
     */
    public AuthenticationClientImpl(W3CEndpointReference endpoint, X509Certificate sslCertificate,
                                    final WSSecurityConfiguration configuration) {

        super( WSAuthenticationServiceFactory.newInstance()
                                             .getPort( endpoint, WSAuthenticationPort.class, new AddressingFeature( true ) ) );

        registerTrustManager( sslCertificate );
        WSSecurityHandler.install( getBindingProvider(), configuration );
    }

    @Nullable
    @Override
    public String authenticate(String applicationName, String deviceName, String language, Object deviceCredentials, PublicKey publicKey)
            throws RequestDeniedException, WSAuthenticationException, WSClientTransportException {

        logger.dbg( "authentication for application %s using device %s", applicationName, deviceName );

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

    @Nullable
    @Override
    public String getGlobalUsageAgreement()
            throws WSClientTransportException, RequestDeniedException, WSAuthenticationException {

        logger.dbg( "get global usage agreement to be confirmed" );

        WSAuthenticationGlobalUsageAgreementRequestType request = AuthenticationUtil.getGlobalUsageAgreementRequest();

        WSAuthenticationGlobalUsageAgreementResponseType response = getGlobalUsageAgreementResponse( request );

        validateStatus( response );

        if (null != response.getGlobalUsageAgreement())
            return response.getGlobalUsageAgreement();

        setAssertion( response );
        setAuthenticationSteps( response );

        return null;
    }

    @Nullable
    @Override
    public String confirmGlobalUsageAgreement(Confirmation confirmation)
            throws RequestDeniedException, WSClientTransportException, WSAuthenticationException {

        logger.dbg( "confirm or reject global usage agreement: %s", confirmation.getValue() );

        WSAuthenticationGlobalUsageAgreementConfirmationType request = AuthenticationUtil.getGlobalUAConfirmationRequest( confirmation );

        WSAuthenticationResponseType response = getGlobalUsageAgreementConfirmationResponse( request );

        validateStatus( response );

        setAssertion( response );
        if (null != assertion)
            return getSubject();

        setAuthenticationSteps( response );

        return null;
    }

    @Nullable
    @Override
    public String getUsageAgreement()
            throws WSClientTransportException, RequestDeniedException, WSAuthenticationException {

        logger.dbg( "get application's usage agreement to be confirmed or subscription required" );

        WSAuthenticationUsageAgreementRequestType request = AuthenticationUtil.getUsageAgreementRequest();

        WSAuthenticationUsageAgreementResponseType response = getUsageAgreementResponse( request );

        validateStatus( response );

        if (null != response.getUsageAgreement())
            return response.getUsageAgreement();

        setAssertion( response );
        setAuthenticationSteps( response );

        return null;
    }

    @Nullable
    @Override
    public String confirmUsageAgreement(Confirmation confirmation)
            throws RequestDeniedException, WSClientTransportException, WSAuthenticationException {

        logger.dbg( "confirm or reject application usage agreement: %s", confirmation.getValue() );

        WSAuthenticationUsageAgreementConfirmationType request = AuthenticationUtil.getUAConfirmationRequest( confirmation );

        WSAuthenticationResponseType response = getUsageAgreementConfirmationResponse( request );

        validateStatus( response );

        setAssertion( response );
        if (null != assertion)
            return getSubject();

        setAuthenticationSteps( response );

        return null;
    }

    @Nullable
    @Override
    public List<AttributeIdentitySDK> getIdentity()
            throws RequestDeniedException, WSClientTransportException, WSAuthenticationException {

        logger.dbg( "get application's identity to be confirmed" );

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
                    identity.add( AuthenticationUtil.newAttributeIdentitySDK( attributeType ) );
                }
                return identity;
            }

        setAssertion( response );
        setAuthenticationSteps( response );

        return null;
    }

    @Nullable
    @Override
    public String confirmIdentity(List<AttributeIdentitySDK> attributes)
            throws RequestDeniedException, WSClientTransportException, WSAuthenticationException {

        logger.dbg( "confirm the application's identity" );

        WSAuthenticationIdentityConfirmationType request = AuthenticationUtil.getIdentityConfirmationRequest( attributes );

        WSAuthenticationResponseType response = getIdentityConfirmationResponse( request );

        validateStatus( response );

        setAssertion( response );
        if (null != assertion)
            return getSubject();

        setAuthenticationSteps( response );

        return null;
    }

    @Override
    public AssertionType getAssertion() {

        return assertion;
    }

    @Override
    public DeviceAuthenticationInformationType getDeviceAuthenticationInformation() {

        return deviceAuthenticationInformation;
    }

    @Override
    public AuthenticationStep getAuthenticationStep() {

        return authenticationStep;
    }

    @Nullable
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
            return getPort().authenticate( request );
        }
        catch (ClientTransportException e) {
            throw new WSClientTransportException( getBindingProvider(), e );
        }
    }

    private void validateStatus(WSAuthenticationResponseType response)
            throws RequestDeniedException, WSClientTransportException, WSAuthenticationException {

        StatusType status = response.getStatus();
        StatusCodeType statusCode = status.getStatusCode();
        String statusCodeValue = statusCode.getValue();
        AuthenticationStatusCode authenticationStatusCode = AuthenticationStatusCode.ofURN( statusCodeValue );
        if (AuthenticationStatusCode.SUCCESS != authenticationStatusCode) {
            logger.err( "status code: %s", statusCodeValue );
            logger.err( "status message: %s", status.getStatusMessage() );
            if (AuthenticationStatusCode.REQUEST_DENIED == authenticationStatusCode)
                throw new RequestDeniedException();
            else if (AuthenticationStatusCode.REQUEST_FAILED == authenticationStatusCode)
                throw new WSClientTransportException( getBindingProvider() );
            else
                throw new WSAuthenticationException( authenticationStatusCode, status.getStatusMessage(), null );
        }
    }

    private WSAuthenticationGlobalUsageAgreementResponseType getGlobalUsageAgreementResponse(
            WSAuthenticationGlobalUsageAgreementRequestType request)
            throws WSClientTransportException {

        try {
            return getPort().requestGlobalUsageAgreement( request );
        }
        catch (ClientTransportException e) {
            throw new WSClientTransportException( getBindingProvider(), e );
        }
    }

    private WSAuthenticationResponseType getGlobalUsageAgreementConfirmationResponse(
            WSAuthenticationGlobalUsageAgreementConfirmationType request)
            throws WSClientTransportException {

        try {
            return getPort().confirmGlobalUsageAgreement( request );
        }
        catch (ClientTransportException e) {
            throw new WSClientTransportException( getBindingProvider(), e );
        }
    }

    private WSAuthenticationUsageAgreementResponseType getUsageAgreementResponse(WSAuthenticationUsageAgreementRequestType request)
            throws WSClientTransportException {

        try {
            return getPort().requestUsageAgreement( request );
        }
        catch (ClientTransportException e) {
            throw new WSClientTransportException( getBindingProvider(), e );
        }
    }

    private WSAuthenticationResponseType getUsageAgreementConfirmationResponse(WSAuthenticationUsageAgreementConfirmationType request)
            throws WSClientTransportException {

        try {
            return getPort().confirmUsageAgreement( request );
        }
        catch (ClientTransportException e) {
            throw new WSClientTransportException( getBindingProvider(), e );
        }
    }

    private WSAuthenticationResponseType getIdentityResponse(WSAuthenticationIdentityRequestType request)
            throws WSClientTransportException {

        try {
            return getPort().requestIdentity( request );
        }
        catch (ClientTransportException e) {
            throw new WSClientTransportException( getBindingProvider(), e );
        }
    }

    private WSAuthenticationResponseType getIdentityConfirmationResponse(WSAuthenticationIdentityConfirmationType request)
            throws WSClientTransportException {

        try {
            return getPort().confirmIdentity( request );
        }
        catch (ClientTransportException e) {
            throw new WSClientTransportException( getBindingProvider(), e );
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
