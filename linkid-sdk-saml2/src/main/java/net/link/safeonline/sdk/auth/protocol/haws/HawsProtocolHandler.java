/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.auth.protocol.haws;

import com.google.common.base.Function;
import net.link.util.logging.Logger;
import net.link.util.InternalInconsistencyException;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.link.safeonline.sdk.api.auth.RequestConstants;
import net.link.safeonline.sdk.api.haws.PullException;
import net.link.safeonline.sdk.api.haws.PushException;
import net.link.safeonline.sdk.api.ws.haws.HawsServiceClient;
import net.link.safeonline.sdk.auth.protocol.*;
import net.link.safeonline.sdk.auth.protocol.saml2.AuthnRequestFactory;
import net.link.safeonline.sdk.auth.protocol.saml2.Saml2ProtocolHandler;
import net.link.safeonline.sdk.configuration.*;
import net.link.safeonline.sdk.ws.LinkIDServiceFactory;
import net.link.util.common.URLUtils;
import net.link.util.exception.ValidationFailedException;
import org.jetbrains.annotations.Nullable;
import org.opensaml.saml2.core.AuthnRequest;
import org.opensaml.saml2.core.Response;


/**
 * Created by wvdhaute
 * Date: 29/01/14
 * Time: 14:46
 */
public class HawsProtocolHandler implements ProtocolHandler {

    private static final Logger logger = Logger.get( HawsProtocolHandler.class );

    private AuthenticationContext authnContext;

    @Override
    public Protocol getProtocol() {

        return Protocol.HAWS;
    }

    @Override
    public AuthnProtocolRequestContext sendAuthnRequest(final HttpServletResponse response, final AuthenticationContext context)
            throws IOException {

        authnContext = context;

        RequestConfig requestConfig = RequestConfig.get( authnContext );

        // create SAML2 request
        AuthnRequest samlRequest = AuthnRequestFactory.createAuthnRequest( authnContext.getApplicationName(), null, authnContext.getApplicationFriendlyName(),
                requestConfig.getLandingURL(), requestConfig.getAuthnService(), authnContext.getDevices(), authnContext.isForceAuthentication(),
                authnContext.getDeviceContext(), authnContext.getSubjectAttributes(), authnContext.getPaymentContext() );

        HawsServiceClient<AuthnRequest, Response> wsClient = getWsClient( authnContext );

        String sessionId;
        try {
            sessionId = wsClient.push( samlRequest, authnContext.getLanguage().getLanguage() );
        }
        catch (PushException e) {
            logger.err( e, "Failed to push SAML v2.0 authentication request: errorCode: %s - %s", e.getErrorCode(), e.getInfo() );
            throw new InternalInconsistencyException( e );
        }

        // redirect with returned sessionId
        String redirectURL = requestConfig.getAuthnService();
        redirectURL = URLUtils.addParameter( redirectURL, RequestConstants.HAWS_SESSION_ID_PARAM, sessionId );
        logger.dbg( "redirectURL=%s", redirectURL );

        try {
            response.sendRedirect( redirectURL );
        }
        catch (IOException e) {
            logger.err( e, "Unable to send redirect message" );
        }

        logger.dbg( "sending Authn Request for: %s issuer=%s", authnContext.getApplicationName(), samlRequest.getIssuer().getValue() );
        return new AuthnProtocolRequestContext( samlRequest.getID(), samlRequest.getIssuer().getValue(), this, requestConfig.getTargetURL(),
                authnContext.isMobileAuthentication(), authnContext.isMobileAuthenticationMinimal(), authnContext.isMobileForceRegistration() );
    }

    @Nullable
    @Override
    public AuthnProtocolResponseContext findAndValidateAuthnResponse(final HttpServletRequest request,
                                                                     final Function<AuthnProtocolResponseContext, AuthenticationContext> responseToContext)
            throws ValidationFailedException {

        if (authnContext == null)
            // This protocol handler has not sent an authentication request.
            return null;

        // get session ID from request
        String sessionId = request.getParameter( RequestConstants.HAWS_SESSION_ID_PARAM );
        logger.dbg( "HAWS: response session Id: %s", sessionId );

        // fetch SAML2 response via WS
        HawsServiceClient<AuthnRequest, Response> wsClient = getWsClient( authnContext );
        Response samlResponse;
        try {
            samlResponse = wsClient.pull( sessionId );
        }
        catch (PullException e) {
            logger.err( e, "Failed to pull SAML v2.0 authentication response: errorCode: %s - %s", e.getErrorCode(), e.getInfo() );
            throw new InternalInconsistencyException( e );
        }

        // validate SAML2 response
        return Saml2ProtocolHandler.validateAuthnResponse( samlResponse, request, responseToContext, this.authnContext, this, null );
    }

    @Nullable
    @Override
    public AuthnProtocolResponseContext findAndValidateAuthnAssertion(final HttpServletRequest request,
                                                                      final Function<AuthnProtocolResponseContext, AuthenticationContext> responseToContext)
            throws ValidationFailedException {

        logger.dbg( "HAWS implementation does not support detached authentication" );
        return null;
    }

    @Override
    public LogoutProtocolRequestContext sendLogoutRequest(final HttpServletResponse response, final String userId, final LogoutContext context)
            throws IOException {

        throw new UnsupportedOperationException( "HAWS implementation does not support single logout yet" );
    }

    @Nullable
    @Override
    public LogoutProtocolResponseContext findAndValidateLogoutResponse(final HttpServletRequest request)
            throws ValidationFailedException {

        throw new UnsupportedOperationException( "HAWS implementation does not support single logout yet" );
    }

    @Nullable
    @Override
    public LogoutProtocolRequestContext findAndValidateLogoutRequest(final HttpServletRequest request,
                                                                     final Function<LogoutProtocolRequestContext, LogoutContext> requestToContext)
            throws ValidationFailedException {

        return null;
    }

    @Override
    public LogoutProtocolResponseContext sendLogoutResponse(final HttpServletResponse response, final LogoutProtocolRequestContext logoutRequestContext,
                                                            final boolean partialLogout)
            throws IOException {

        throw new UnsupportedOperationException( "HAWS implementation does not support single logout yet" );
    }

    // helper methods

    private HawsServiceClient<AuthnRequest, Response> getWsClient(final AuthenticationContext authnContext) {

        // send via WS, X509 token or username token according to application key pair being available...

        HawsServiceClient<AuthnRequest, Response> wsClient;
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
