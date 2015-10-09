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
import java.util.Collections;
import java.util.Locale;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.link.safeonline.sdk.api.auth.LinkIDAuthnResponse;
import net.link.safeonline.sdk.api.ws.linkid.auth.LinkIDAuthException;
import net.link.safeonline.sdk.api.ws.linkid.auth.LinkIDAuthPollException;
import net.link.safeonline.sdk.api.ws.linkid.auth.LinkIDAuthPollResponse;
import net.link.safeonline.sdk.api.ws.linkid.auth.LinkIDAuthSession;
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

        LinkIDAuthSession linkIDAuthSession = (LinkIDAuthSession) request.getSession().getAttribute( RESPONSE_SESSION_PARAM );
        if (null == linkIDAuthSession) {
            try {
                LinkIDAuthenticationContext authenticationContext = new LinkIDAuthenticationContext();
                authenticationContext.setApplicationName( config().linkID().app().name() );
                authenticationContext.setIdentityProfiles( Collections.singletonList( "linkid_basic" ) );
                authenticationContext.setLanguage( Locale.ENGLISH );

                linkIDAuthSession = LinkIDAuthWSUtils.startAuthentication( LinkIDServiceFactory.getLinkIDService(), authenticationContext, null );

                // push on session
                request.getSession().setAttribute( RESPONSE_SESSION_PARAM, linkIDAuthSession );

                response.setContentType( "image/png" );
                OutputStream o = response.getOutputStream();
                o.write( linkIDAuthSession.getQrCodeInfo().getQrImage() );
                o.flush();
                o.close();
            }
            catch (LinkIDAuthException e) {
                throw new InternalInconsistencyException( e );
            }
        } else {

            // poll
            try {
                LinkIDAuthPollResponse<Response> linkIDAuthPollResponse = LinkIDAuthWSUtils.pollAuthentication( linkIDAuthSession.getSessionId(),
                        Locale.ENGLISH );
                showPollResult( linkIDAuthPollResponse, response );
            }
            catch (LinkIDAuthPollException e) {
                throw new InternalInconsistencyException( e );
            }
        }
    }

    private void showPollResult(final LinkIDAuthPollResponse<Response> linkIDAuthPollResponse, final HttpServletResponse response)
            throws IOException {

        response.getWriter().write( "<html>" );

        response.getWriter().write( "<body>" );

        response.getWriter().write( "<p>" );
        response.getWriter().write( "<h2>Session state</h2>" );
        response.getWriter().write( "AuthnState  : " + linkIDAuthPollResponse.getLinkIDAuthenticationState() + "<br/>" );
        response.getWriter().write( "PaymentState: " + linkIDAuthPollResponse.getPaymentState() + "<br/>" );
        response.getWriter().write( "PaymentURL  : " + linkIDAuthPollResponse.getPaymentMenuURL() + "<br/>" );
        response.getWriter().write( "</p>" );

        if (null != linkIDAuthPollResponse.getResponse()) {
            LinkIDAuthnResponse authnResponse = LinkIDAuthWSUtils.parse( linkIDAuthPollResponse.getResponse() );

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
