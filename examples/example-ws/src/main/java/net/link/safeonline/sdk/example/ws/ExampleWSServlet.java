/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.example.ws;

import static net.link.safeonline.sdk.configuration.SDKConfigHolder.config;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Locale;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.link.safeonline.sdk.api.auth.AuthnResponseDO;
import net.link.safeonline.sdk.api.ws.auth.AuthnException;
import net.link.safeonline.sdk.api.ws.auth.AuthnSession;
import net.link.safeonline.sdk.api.ws.auth.PollException;
import net.link.safeonline.sdk.api.ws.auth.PollResponse;
import net.link.safeonline.sdk.auth.protocol.ws.AuthWSUtils;
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

        AuthnSession authnSession = (AuthnSession) request.getSession().getAttribute( RESPONSE_SESSION_PARAM );
        if (null == authnSession) {
            try {
                authnSession = AuthWSUtils.startAuthentication( config().linkID().app().name(), null, null, null, null,
                        Collections.singletonList( "linkid_basic" ), Locale.ENGLISH, null, false );

                // push on session
                request.getSession().setAttribute( RESPONSE_SESSION_PARAM, authnSession );

                response.setContentType( "image/png" );
                OutputStream o = response.getOutputStream();
                o.write( authnSession.getQrCodeImage() );
                o.flush();
                o.close();
            }
            catch (AuthnException e) {
                throw new InternalInconsistencyException( e );
            }
        } else {

            // poll
            try {
                PollResponse<Response> pollResponse = AuthWSUtils.pollAuthentication( authnSession.getSessionId(), Locale.ENGLISH );
                showPollResult( pollResponse, response );
            }
            catch (PollException e) {
                throw new InternalInconsistencyException( e );
            }
        }
    }

    private void showPollResult(final PollResponse<Response> pollResponse, final HttpServletResponse response)
            throws IOException {

        response.getWriter().write( "<html>" );

        response.getWriter().write( "<body>" );

        response.getWriter().write( "<p>" );
        response.getWriter().write( "<h2>Session state</h2>" );
        response.getWriter().write( "AuthnState  : " + pollResponse.getAuthenticationState() + "<br/>" );
        response.getWriter().write( "PaymentState: " + pollResponse.getPaymentState() + "<br/>" );
        response.getWriter().write( "PaymentURL  : " + pollResponse.getPaymentMenuURL() + "<br/>" );
        response.getWriter().write( "</p>" );

        if (null != pollResponse.getResponse()) {
            AuthnResponseDO authnResponse = AuthWSUtils.parse( pollResponse.getResponse() );

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
