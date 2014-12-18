/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.auth.protocol.saml2;

import static net.link.safeonline.sdk.configuration.SDKConfigHolder.config;

import com.google.common.collect.Maps;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.link.safeonline.sdk.api.attribute.AttributeSDK;
import net.link.safeonline.sdk.auth.protocol.AuthnProtocolRequestContext;
import net.link.safeonline.sdk.auth.protocol.AuthnProtocolResponseContext;
import net.link.safeonline.sdk.auth.protocol.ProtocolContext;
import net.link.safeonline.sdk.auth.protocol.ProtocolHandler;
import net.link.safeonline.sdk.auth.protocol.RequestConfig;
import net.link.safeonline.sdk.auth.util.DeviceContextUtils;
import net.link.safeonline.sdk.configuration.AuthenticationContext;
import net.link.safeonline.sdk.configuration.Protocol;
import net.link.util.common.CertificateChain;
import net.link.util.exception.ValidationFailedException;
import net.link.util.logging.Logger;
import net.link.util.saml.Saml2Utils;
import org.jetbrains.annotations.Nullable;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.AuthnRequest;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.core.StatusCode;


/**
 * Implementation class for the SAML2 browser POST authentication protocol.
 *
 * @author fcorneli
 */
public class Saml2ProtocolHandler implements ProtocolHandler {

    private static final Logger logger = Logger.get( Saml2ProtocolHandler.class );

    private AuthenticationContext authnContext;

    @Override
    public Protocol getProtocol() {

        return Protocol.SAML2;
    }

    @Override
    public AuthnProtocolRequestContext sendAuthnRequest(HttpServletResponse response, AuthenticationContext context)
            throws IOException {

        authnContext = context;

        RequestConfig requestConfig = RequestConfig.get( authnContext );

        String templateResourceName = config().proto().saml().postBindingTemplate();

        Map<String, String> deviceContext = DeviceContextUtils.generate( authnContext.getAuthenticationMessage(), authnContext.getFinishedMessage(),
                authnContext.getIdentityProfiles() );

        AuthnRequest samlRequest = AuthnRequestFactory.createAuthnRequest( authnContext.getApplicationName(), null, authnContext.getApplicationFriendlyName(),
                requestConfig.getLandingURL(), requestConfig.getAuthnService(), authnContext.isForceAuthentication(), deviceContext,
                authnContext.getSubjectAttributes(), authnContext.getPaymentContext(), authnContext.getCallback() );

        CertificateChain certificateChain = null;
        if (null != authnContext.getApplicationCertificate()) {
            certificateChain = new CertificateChain( authnContext.getApplicationCertificate() );
        }

        RequestUtil.sendRequest( requestConfig.getAuthnService(), authnContext.getSAML().getBinding(), samlRequest, authnContext.getApplicationKeyPair(),
                certificateChain, response, authnContext.getSAML().getRelayState(), templateResourceName, authnContext.getLanguage() );

        logger.dbg( "sending Authn Request for: %s issuer=%s", authnContext.getApplicationName(), samlRequest.getIssuer().getValue() );
        return new AuthnProtocolRequestContext( samlRequest.getID(), samlRequest.getIssuer().getValue(), this, requestConfig.getTargetURL(),
                authnContext.isMobileForceRegistration() );
    }

    @Nullable
    @Override
    public AuthnProtocolResponseContext findAndValidateAuthnResponse(HttpServletRequest request)
            throws ValidationFailedException {

        if (authnContext == null)
        // This protocol handler has not sent an authentication request.
        {
            return null;
        }

        Map<String, ProtocolContext> contexts = ProtocolContext.getContexts( request.getSession() );
        Saml2ResponseContext saml2ResponseContext = ResponseUtil.findAndValidateAuthnResponse( request, contexts, authnContext.getTrustedCertificates() );
        if (null == saml2ResponseContext)
        // The request does not contain an authentication response or it didn't match the request sent by this protocol handler.
        {
            return null;
        }

        return validateAuthnResponse( (Response) saml2ResponseContext.getResponse(), request, saml2ResponseContext.getCertificateChain() );
    }

    public static AuthnProtocolResponseContext validateAuthnResponse(final Response samlResponse, final HttpServletRequest request,
                                                                     final CertificateChain certificateChain)
            throws ValidationFailedException {

        String userId = null, applicationName = null;
        Map<String, List<AttributeSDK<Serializable>>> attributes = Maps.newHashMap();
        if (!samlResponse.getAssertions().isEmpty()) {
            Assertion assertion = samlResponse.getAssertions().get( 0 );
            userId = assertion.getSubject().getNameID().getValue();
            attributes.putAll( LinkIDSaml2Utils.getAttributeValues( assertion ) );
            applicationName = LinkIDSaml2Utils.findApplicationName( assertion );
        }
        logger.dbg( "userId: %s", userId );

        boolean success = samlResponse.getStatus().getStatusCode().getValue().equals( StatusCode.SUCCESS_URI );

        AuthnProtocolRequestContext authnRequest = ProtocolContext.findContext( request.getSession(), samlResponse.getInResponseTo() );

        ResponseUtil.validateResponse( samlResponse, authnRequest.getIssuer() );

        return new AuthnProtocolResponseContext( authnRequest, samlResponse.getID(), userId, applicationName, attributes, success, certificateChain,
                LinkIDSaml2Utils.findPaymentResponse( samlResponse ) );
    }

    public static AuthnProtocolResponseContext validateAuthnResponse(final Response samlResponse)
            throws ValidationFailedException {

        String userId = null, applicationName = null;
        Map<String, List<AttributeSDK<Serializable>>> attributes = Maps.newHashMap();
        if (!samlResponse.getAssertions().isEmpty()) {
            Assertion assertion = samlResponse.getAssertions().get( 0 );
            userId = assertion.getSubject().getNameID().getValue();
            attributes.putAll( LinkIDSaml2Utils.getAttributeValues( assertion ) );
            applicationName = LinkIDSaml2Utils.findApplicationName( assertion );
        }
        logger.dbg( "userId: %s", userId );
        boolean success = samlResponse.getStatus().getStatusCode().getValue().equals( StatusCode.SUCCESS_URI );

        ResponseUtil.validateResponse( samlResponse, null );

        return new AuthnProtocolResponseContext( null, samlResponse.getID(), userId, applicationName, attributes, success, null,
                LinkIDSaml2Utils.findPaymentResponse( samlResponse ) );
    }

    // TODO: get rid of me...
    @Deprecated
    @Override
    public AuthnProtocolResponseContext findAndValidateAuthnAssertion(final HttpServletRequest request)
            throws ValidationFailedException {

        Assertion assertion = ResponseUtil.findAuthnAssertion( request );
        if (null == assertion)
        // The request does not contain an authentication response or it didn't match the request sent by this protocol handler.
        {
            return null;
        }

        Map<String, List<AttributeSDK<Serializable>>> attributes = LinkIDSaml2Utils.getAttributeValues( assertion );
        String userId = assertion.getSubject().getNameID().getValue();
        String applicationName = LinkIDSaml2Utils.findApplicationName( assertion );

        AuthnProtocolRequestContext authnRequest = new AuthnProtocolRequestContext( null, applicationName, this, authnContext.getTarget(), false );

        CertificateChain certificateChain = Saml2Utils.validateSignature( assertion.getSignature(), request, authnContext.getTrustedCertificates() );

        return new AuthnProtocolResponseContext( authnRequest, null, userId, applicationName, attributes, true, certificateChain, null );
    }

    @Override
    public String toString() {

        return String.format( "{proto: SAML2, auth=%s}", authnContext );
    }
}
