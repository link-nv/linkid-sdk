/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.auth.protocol.saml2;

import static net.link.safeonline.sdk.configuration.LinkIDSDKConfigHolder.config;

import com.google.common.collect.Maps;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.link.safeonline.sdk.api.attribute.LinkIDAttribute;
import net.link.safeonline.sdk.auth.protocol.LinkIDAuthnProtocolRequestContext;
import net.link.safeonline.sdk.auth.protocol.LinkIDAuthnProtocolResponseContext;
import net.link.safeonline.sdk.auth.protocol.LinkIDProtocolContext;
import net.link.safeonline.sdk.auth.protocol.LinkIDProtocolHandler;
import net.link.safeonline.sdk.auth.protocol.LinkIDRequestConfig;
import net.link.safeonline.sdk.auth.util.LinkIDDeviceContextUtils;
import net.link.safeonline.sdk.configuration.LinkIDAuthenticationContext;
import net.link.safeonline.sdk.configuration.LinkIDProtocol;
import net.link.util.common.CertificateChain;
import net.link.util.exception.ValidationFailedException;
import net.link.util.logging.Logger;
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
public class LinkIDSaml2ProtocolHandler implements LinkIDProtocolHandler {

    private static final Logger logger = Logger.get( LinkIDSaml2ProtocolHandler.class );

    private LinkIDAuthenticationContext authnContext;

    @Override
    public LinkIDProtocol getProtocol() {

        return LinkIDProtocol.SAML2;
    }

    @Override
    public LinkIDAuthnProtocolRequestContext sendAuthnRequest(HttpServletResponse response, LinkIDAuthenticationContext context)
            throws IOException {

        authnContext = context;

        LinkIDRequestConfig linkIDRequestConfig = LinkIDRequestConfig.get( authnContext );

        String templateResourceName = config().proto().saml().postBindingTemplate();

        Map<String, String> deviceContext = LinkIDDeviceContextUtils.generate( authnContext.getAuthenticationMessage(), authnContext.getFinishedMessage(),
                authnContext.getIdentityProfiles() );

        AuthnRequest samlRequest = LinkIDAuthnRequestFactory.createAuthnRequest( authnContext.getApplicationName(), null,
                authnContext.getApplicationFriendlyName(), linkIDRequestConfig.getLandingURL(), linkIDRequestConfig.getAuthnService(), authnContext.isForceAuthentication(),
                deviceContext, authnContext.getSubjectAttributes(), authnContext.getPaymentContext(), authnContext.getCallback() );

        CertificateChain certificateChain = null;
        if (null != authnContext.getApplicationCertificate()) {
            certificateChain = new CertificateChain( authnContext.getApplicationCertificate() );
        }

        LinkIDRequestUtil.sendRequest( linkIDRequestConfig.getAuthnService(), authnContext.getSAML().getBinding(), samlRequest, authnContext.getApplicationKeyPair(),
                certificateChain, response, authnContext.getSAML().getRelayState(), templateResourceName, authnContext.getLanguage() );

        logger.dbg( "sending Authn Request for: %s issuer=%s", authnContext.getApplicationName(), samlRequest.getIssuer().getValue() );
        return new LinkIDAuthnProtocolRequestContext( samlRequest.getID(), samlRequest.getIssuer().getValue(), this, linkIDRequestConfig.getTargetURL(),
                authnContext.isMobileForceRegistration() );
    }

    @Nullable
    @Override
    public LinkIDAuthnProtocolResponseContext findAndValidateAuthnResponse(HttpServletRequest request)
            throws ValidationFailedException {

        if (authnContext == null)
        // This protocol handler has not sent an authentication request.
        {
            return null;
        }

        Map<String, LinkIDProtocolContext> contexts = LinkIDProtocolContext.getContexts( request.getSession() );
        LinkIDSaml2ResponseContext saml2ResponseContext = LinkIDResponseUtil.findAndValidateAuthnResponse( request, contexts, authnContext.getTrustedCertificates() );
        if (null == saml2ResponseContext)
        // The request does not contain an authentication response or it didn't match the request sent by this protocol handler.
        {
            return null;
        }

        return validateAuthnResponse( (Response) saml2ResponseContext.getResponse(), request, saml2ResponseContext.getCertificateChain() );
    }

    public static LinkIDAuthnProtocolResponseContext validateAuthnResponse(final Response samlResponse, final HttpServletRequest request,
                                                                     final CertificateChain certificateChain)
            throws ValidationFailedException {

        String userId = null, applicationName = null;
        Map<String, List<LinkIDAttribute<Serializable>>> attributes = Maps.newHashMap();
        if (!samlResponse.getAssertions().isEmpty()) {
            Assertion assertion = samlResponse.getAssertions().get( 0 );
            userId = assertion.getSubject().getNameID().getValue();
            attributes.putAll( LinkIDSaml2Utils.getAttributeValues( assertion ) );
            applicationName = LinkIDSaml2Utils.findApplicationName( assertion );
        }
        logger.dbg( "userId: %s", userId );

        boolean success = samlResponse.getStatus().getStatusCode().getValue().equals( StatusCode.SUCCESS_URI );

        LinkIDAuthnProtocolRequestContext authnRequest = LinkIDProtocolContext.findContext( request.getSession(), samlResponse.getInResponseTo() );

        LinkIDResponseUtil.validateResponse( samlResponse, authnRequest.getIssuer() );

        return new LinkIDAuthnProtocolResponseContext( authnRequest, samlResponse.getID(), userId, applicationName, attributes, success, certificateChain,
                LinkIDSaml2Utils.findPaymentResponse( samlResponse ), LinkIDSaml2Utils.findExternalCodeResponse( samlResponse ) );
    }

    public static LinkIDAuthnProtocolResponseContext validateAuthnResponse(final Response samlResponse)
            throws ValidationFailedException {

        String userId = null, applicationName = null;
        Map<String, List<LinkIDAttribute<Serializable>>> attributes = Maps.newHashMap();
        if (!samlResponse.getAssertions().isEmpty()) {
            Assertion assertion = samlResponse.getAssertions().get( 0 );
            userId = assertion.getSubject().getNameID().getValue();
            attributes.putAll( LinkIDSaml2Utils.getAttributeValues( assertion ) );
            applicationName = LinkIDSaml2Utils.findApplicationName( assertion );
        }
        logger.dbg( "userId: %s", userId );
        boolean success = samlResponse.getStatus().getStatusCode().getValue().equals( StatusCode.SUCCESS_URI );

        LinkIDResponseUtil.validateResponse( samlResponse, null );

        return new LinkIDAuthnProtocolResponseContext( null, samlResponse.getID(), userId, applicationName, attributes, success, null,
                LinkIDSaml2Utils.findPaymentResponse( samlResponse ), LinkIDSaml2Utils.findExternalCodeResponse( samlResponse ) );
    }

    @Override
    public String toString() {

        return String.format( "{proto: SAML2, auth=%s}", authnContext );
    }
}
