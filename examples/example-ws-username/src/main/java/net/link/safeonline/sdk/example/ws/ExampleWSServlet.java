/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.example.ws;

import static net.link.safeonline.sdk.configuration.LinkIDSDKConfigHolder.config;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Locale;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.link.safeonline.sdk.api.auth.LinkIDAuthnResponse;
import net.link.safeonline.sdk.api.ws.auth.LinkIDAuthnException;
import net.link.safeonline.sdk.api.ws.auth.LinkIDAuthnSession;
import net.link.safeonline.sdk.api.ws.auth.LinkIDPollException;
import net.link.safeonline.sdk.api.ws.auth.LinkIDPollResponse;
import net.link.safeonline.sdk.auth.protocol.ws.LinkIDAuthWSUtils;
import net.link.safeonline.sdk.configuration.LinkIDAuthenticationContext;
import net.link.safeonline.sdk.ws.LinkIDServiceFactory;
import net.link.util.InternalInconsistencyException;
import net.link.util.logging.Logger;
import org.opensaml.saml2.core.Response;


public class ExampleWSServlet extends HttpServlet {

    private static final Logger logger = Logger.get( ExampleWSServlet.class );

    public static final String RESPONSE_SESSION_PARAM = ExampleWSServlet.class.getSimpleName() + ".RESPONSE_SESSION_PARAM";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        logger.dbg( "doGet" );

        LinkIDAuthnSession linkIDAuthnSession = (LinkIDAuthnSession) request.getSession().getAttribute( RESPONSE_SESSION_PARAM );
        if (null == linkIDAuthnSession) {
            try {
                LinkIDAuthenticationContext authenticationContext = new LinkIDAuthenticationContext();
                authenticationContext.setApplicationName( config().linkID().app().name() );
                authenticationContext.setLanguage( Locale.ENGLISH );

                linkIDAuthnSession = LinkIDAuthWSUtils.startAuthentication( LinkIDServiceFactory.getAuthService(), authenticationContext, null );

                // push on session
                request.getSession().setAttribute( RESPONSE_SESSION_PARAM, linkIDAuthnSession );

                response.setContentType( "image/png" );
                OutputStream o = response.getOutputStream();
                o.write( linkIDAuthnSession.getQrCodeImage() );
                o.flush();
                o.close();
            }
            catch (LinkIDAuthnException e) {
                throw new InternalInconsistencyException( e );
            }
        } else {

            // poll
            try {
                LinkIDPollResponse<Response> linkIDPollResponse = LinkIDAuthWSUtils.pollAuthentication( linkIDAuthnSession.getSessionId(), Locale.ENGLISH );
                showPollResult( linkIDPollResponse, response );
            }
            catch (LinkIDPollException e) {
                throw new InternalInconsistencyException( e );
            }
        }
    }

    private void showPollResult(final LinkIDPollResponse<Response> linkIDPollResponse, final HttpServletResponse response)
            throws IOException {

        response.getWriter().write( "<html>" );

        response.getWriter().write( "<body>" );

        response.getWriter().write( "<p>" );
        response.getWriter().write( "<h2>Session state</h2>" );
        response.getWriter().write( "AuthnState  : " + linkIDPollResponse.getLinkIDAuthenticationState() + "<br/>" );
        response.getWriter().write( "PaymentState: " + linkIDPollResponse.getPaymentState() + "<br/>" );
        response.getWriter().write( "PaymentURL  : " + linkIDPollResponse.getPaymentMenuURL() + "<br/>" );
        response.getWriter().write( "</p>" );

        if (null != linkIDPollResponse.getResponse()) {
            LinkIDAuthnResponse authnResponse = LinkIDAuthWSUtils.parse( linkIDPollResponse.getResponse() );

            response.getWriter().write( "<p>" );
            response.getWriter().write( "<h2>AuthnResponse</h2>" );
            response.getWriter().write( "UserID: " + authnResponse.getUserId() + "<br/>" );
            response.getWriter().write( "</p>" );
        }

        response.getWriter().write( "<p>" );
        response.getWriter().write( "<a href=\"./restart\">Restart</a>" );
        response.getWriter().write( "</p>" );

        response.getWriter().write( "</body>" );

        response.getWriter().write( "</html>" );
    }
}
