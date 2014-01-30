package net.link.safeonline.sdk.auth.protocol.haws;

import com.google.common.base.Function;
import com.lyndir.lhunath.opal.system.logging.Logger;
import com.lyndir.lhunath.opal.system.logging.exception.InternalInconsistencyException;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.link.safeonline.sdk.api.auth.RequestConstants;
import net.link.safeonline.sdk.api.haws.PushException;
import net.link.safeonline.sdk.api.ws.haws.HawsServiceClient;
import net.link.safeonline.sdk.auth.protocol.*;
import net.link.safeonline.sdk.auth.protocol.saml2.AuthnRequestFactory;
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
                authnContext.getSessionTrackingId(), authnContext.getDeviceContext(), authnContext.getSubjectAttributes(), authnContext.getPaymentContext() );

        // send via WS, X509 token or username token according to application key pair being available...
        HawsServiceClient<AuthnRequest, Response> wsClient;
        if (null != authnContext.getWsUsername()) {
            // WS-Security Username token profile
            wsClient = LinkIDServiceFactory.getHawsService( authnContext.getWsUsername(), authnContext.getWsPassword() );
        } else {
            // look at config().linkID().app().username(), none -> X509
            wsClient = LinkIDServiceFactory.getHawsService();
        }

        String sessionId;
        try {
            sessionId = wsClient.push( samlRequest, authnContext.getLanguage().getLanguage(), authnContext.getThemeName(), authnContext.getLoginMode(),
                    authnContext.getStartPage() );
        }
        catch (PushException e) {
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

        // TODO: get session ID from request

        // TODO: fetch SAML2 response via WS

        // TODO: parse SAML2 response

        return null;
    }

    @Nullable
    @Override
    public AuthnProtocolResponseContext findAndValidateAuthnAssertion(final HttpServletRequest request,
                                                                      final Function<AuthnProtocolResponseContext, AuthenticationContext> responseToContext)
            throws ValidationFailedException {

        logger.dbg( "HAWS implementation does not support detached authentication yet" );
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
}
