/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.auth.servlet;

import com.google.common.base.Function;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import net.link.safeonline.sdk.auth.filter.LoginManager;
import net.link.safeonline.sdk.auth.protocol.AuthnProtocolResponseContext;
import net.link.safeonline.sdk.auth.protocol.ProtocolManager;
import net.link.safeonline.sdk.configuration.AuthenticationContext;
import net.link.safeonline.sdk.servlet.AbstractConfidentialLinkIDInjectionServlet;
import net.link.util.exception.ValidationFailedException;
import net.link.util.logging.Logger;
import net.link.util.servlet.ErrorMessage;
import net.link.util.servlet.ServletUtils;


/**
 * Login Servlet. This servlet contains the landing page to finalize the authentication process initiated by the web application.
 *
 * @author fcorneli
 */
public class LoginServlet extends AbstractConfidentialLinkIDInjectionServlet {

    private static final Logger logger = Logger.get( LoginServlet.class );

    public static final String ERROR_PAGE_PARAM   = "ErrorPage";
    public static final String TIMEOUT_PAGE_PARAM = "TimeoutPage";

    private String errorPage;

    private String timeoutPage;

    @Override
    public void init(ServletConfig config)
            throws ServletException {

        super.init( config );

        errorPage = config.getInitParameter( ERROR_PAGE_PARAM );
        timeoutPage = config.getInitParameter( TIMEOUT_PAGE_PARAM );
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        handleLanding( request, response );
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        handleLanding( request, response );
    }

    protected void handleLanding(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        try {
            AuthnProtocolResponseContext authnResponse = ProtocolManager.findAndValidateAuthnResponse( request, getContextFunction( request.getSession() ) );
            if (null == authnResponse)
                authnResponse = ProtocolManager.findAndValidateAuthnAssertion( request, getContextFunction( request.getSession() ) );
            if (null == authnResponse) {
                // if we don't have a response, check if perhaps the session has expired
                if (request.getSession( false ) == null || request.getSession().isNew()) {
                    logger.wrn( "Session timeout, authentication took too long." );
                    ServletUtils.redirectToErrorPage( request, response, timeoutPage, null,
                            new ErrorMessage( "Session timeout, authentication took too long." ) );
                    return;
                }
                //nope, it's something else
                logger.err( "No expected or detached authentication responses found in request." );
                ServletUtils.redirectToErrorPage( request, response, errorPage, null,
                        new ErrorMessage( "No expected or detached authentication responses found in request." ) );
                return;
            }

            onLogin( request.getSession(), authnResponse, response );

            response.setContentType( "text/html" );
            PrintWriter out = response.getWriter();
            out.println( "<html>" );
            out.println( "<head>" );
            out.println( "<script type=\"text/javascript\">" );
            out.println( "window.top.location.replace(\"" + authnResponse.getRequest().getTarget() + "\");" );
            out.println( "</script>" );
            out.println( "</head>" );
            out.println( "<body>" );
            out.println(
                    "<noscript><p>You are successfully logged in. Since your browser does not support JavaScript, you must close this popup window and refresh the original window manually.</p></noscript>" );
            out.println( "</body>" );
            out.println( "</html>" );
        }
        catch (ValidationFailedException e) {

            logger.err( e, "Validation failed: %s", e.getMessage() );

            ServletUtils.redirectToErrorPage( request, response, errorPage, null,
                    new ErrorMessage( String.format( "Validation of authentication response failed: %s", e.getMessage() ) ) );
        }
    }

    /**
     * Override this method if you want to create a custom context for detached authentication responses.
     * <p/>
     * The standard implementation uses {@link AuthenticationContext#AuthenticationContext()}.
     *
     * @return A function that provides the context for validating detached authentication responses (assertions).
     */
    @SuppressWarnings("UnusedParameters")
    protected Function<AuthnProtocolResponseContext, AuthenticationContext> getContextFunction(final HttpSession httpSession) {

        return new Function<AuthnProtocolResponseContext, AuthenticationContext>() {
            @Override
            public AuthenticationContext apply(final AuthnProtocolResponseContext from) {

                return new AuthenticationContext();
            }
        };
    }

    /**
     * Invoked when an authentication response is received.  The default implementation sets the user's credentials on the session if the
     * response was successful and does nothing if it wasn't.
     *
     * @param session       The HTTP session within which the response was received.
     * @param authnResponse The response that was received.
     */
    @SuppressWarnings("UnusedParameters")
    protected void onLogin(final HttpSession session, final AuthnProtocolResponseContext authnResponse, final HttpServletResponse httpServletResponse) {

        if (authnResponse.isSuccess()) {
            logger.dbg( "username: %s", authnResponse.getUserId() );

            LoginManager.set( session, authnResponse );
        }
    }

    public String getErrorPage() {

        return errorPage;
    }

    public String getTimeoutPage() {

        return timeoutPage;
    }
}
