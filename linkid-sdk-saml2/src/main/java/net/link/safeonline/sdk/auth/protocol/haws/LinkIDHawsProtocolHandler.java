/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.auth.protocol.haws;

import java.io.IOException;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.link.safeonline.sdk.api.auth.LinkIDRequestConstants;
import net.link.safeonline.sdk.api.haws.LinkIDPullException;
import net.link.safeonline.sdk.api.haws.LinkIDPushException;
import net.link.safeonline.sdk.api.ws.haws.LinkIDHawsServiceClient;
import net.link.safeonline.sdk.auth.protocol.LinkIDAuthnProtocolRequestContext;
import net.link.safeonline.sdk.auth.protocol.LinkIDAuthnProtocolResponseContext;
import net.link.safeonline.sdk.auth.protocol.LinkIDProtocolHandler;
import net.link.safeonline.sdk.auth.protocol.LinkIDRequestConfig;
import net.link.safeonline.sdk.auth.protocol.saml2.LinkIDAuthnRequestFactory;
import net.link.safeonline.sdk.auth.protocol.saml2.LinkIDSaml2ProtocolHandler;
import net.link.safeonline.sdk.auth.util.LinkIDDeviceContextUtils;
import net.link.safeonline.sdk.configuration.LinkIDAuthenticationContext;
import net.link.safeonline.sdk.configuration.LinkIDProtocol;
import net.link.safeonline.sdk.ws.LinkIDServiceFactory;
import net.link.util.InternalInconsistencyException;
import net.link.util.common.URLUtils;
import net.link.util.exception.ValidationFailedException;
import net.link.util.logging.Logger;
import org.jetbrains.annotations.Nullable;
import org.opensaml.saml2.core.AuthnRequest;
import org.opensaml.saml2.core.Response;


/**
 * Created by wvdhaute
 * Date: 29/01/14
 * Time: 14:46
 */
public class LinkIDHawsProtocolHandler implements LinkIDProtocolHandler {

    private static final Logger logger = Logger.get( LinkIDHawsProtocolHandler.class );

    private LinkIDAuthenticationContext authnContext;

    @Override
    public LinkIDProtocol getProtocol() {

        return LinkIDProtocol.HAWS;
    }

    @Override
    public LinkIDAuthnProtocolRequestContext sendAuthnRequest(final HttpServletResponse response, final LinkIDAuthenticationContext context)
            throws IOException {

        authnContext = context;

        LinkIDRequestConfig linkIDRequestConfig = LinkIDRequestConfig.get( authnContext );

        Map<String, String> deviceContext = LinkIDDeviceContextUtils.generate( authnContext.getAuthenticationMessage(), authnContext.getFinishedMessage(),
                authnContext.getIdentityProfiles() );

        // create SAML2 request
        AuthnRequest samlRequest = LinkIDAuthnRequestFactory.createAuthnRequest( authnContext.getApplicationName(), null,
                authnContext.getApplicationFriendlyName(), linkIDRequestConfig.getLandingURL(), linkIDRequestConfig.getAuthnService(), authnContext.isForceAuthentication(),
                deviceContext, authnContext.getSubjectAttributes(), authnContext.getPaymentContext(), authnContext.getCallback() );

        LinkIDHawsServiceClient<AuthnRequest, Response> wsClient = getWsClient( authnContext );

        String sessionId;
        try {
            sessionId = wsClient.push( samlRequest, authnContext.getLanguage().getLanguage() );
        }
        catch (LinkIDPushException e) {
            logger.err( e, "Failed to push SAML v2.0 authentication request: errorCode: %s - %s", e.getErrorCode(), e.getInfo() );
            throw new InternalInconsistencyException( e );
        }

        // redirect with returned sessionId
        String redirectURL = linkIDRequestConfig.getAuthnService();
        redirectURL = URLUtils.addParameter( redirectURL, LinkIDRequestConstants.HAWS_SESSION_ID_PARAM, sessionId );
        logger.dbg( "redirectURL=%s", redirectURL );

        try {
            response.sendRedirect( redirectURL );
        }
        catch (IOException e) {
            logger.err( e, "Unable to send redirect message" );
        }

        logger.dbg( "sending Authn Request for: %s issuer=%s", authnContext.getApplicationName(), samlRequest.getIssuer().getValue() );
        return new LinkIDAuthnProtocolRequestContext( samlRequest.getID(), samlRequest.getIssuer().getValue(), this, linkIDRequestConfig.getTargetURL(),
                authnContext.isMobileForceRegistration() );
    }

    @Nullable
    @Override
    public LinkIDAuthnProtocolResponseContext findAndValidateAuthnResponse(final HttpServletRequest request)
            throws ValidationFailedException {

        if (authnContext == null)
        // This protocol handler has not sent an authentication request.
        {
            return null;
        }

        // get session ID from request
        String sessionId = request.getParameter( LinkIDRequestConstants.HAWS_SESSION_ID_PARAM );
        logger.dbg( "HAWS: response session Id: %s", sessionId );

        // fetch SAML2 response via WS
        LinkIDHawsServiceClient<AuthnRequest, Response> wsClient = getWsClient( authnContext );
        Response samlResponse;
        try {
            samlResponse = wsClient.pull( sessionId );
        }
        catch (LinkIDPullException e) {
            logger.err( e, "Failed to pull SAML v2.0 authentication response: errorCode: %s - %s", e.getErrorCode(), e.getInfo() );
            throw new InternalInconsistencyException( e );
        }

        // validate SAML2 response
        return LinkIDSaml2ProtocolHandler.validateAuthnResponse( samlResponse, request, null );
    }

    // helper methods

    private LinkIDHawsServiceClient<AuthnRequest, Response> getWsClient(final LinkIDAuthenticationContext authnContext) {

        // send via WS, X509 token or username token according to application key pair being available...

        LinkIDHawsServiceClient<AuthnRequest, Response> wsClient;
        if (null != authnContext.getWsUsername()) {
            // WS-Security Username token profile
            wsClient = LinkIDServiceFactory.getHawsService( authnContext.getWsUsername(), authnContext.getWsPassword() );
        } else {
            // look at config().linkID().app().username(), none -> X509
            wsClient = LinkIDServiceFactory.getHawsService();
        }
        return wsClient;
    }
}
