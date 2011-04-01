/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.auth.servlet;

import com.google.common.base.Function;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.*;
import net.link.safeonline.sdk.auth.filter.LoginManager;
import net.link.safeonline.sdk.auth.protocol.AuthnProtocolResponseContext;
import net.link.safeonline.sdk.auth.protocol.ProtocolManager;
import net.link.safeonline.sdk.configuration.AuthenticationContext;
import net.link.safeonline.sdk.servlet.AbstractConfidentialLinkIDInjectionServlet;
import net.link.util.error.ValidationFailedException;
import net.link.util.servlet.ErrorMessage;
import net.link.util.servlet.ServletUtils;
import net.link.util.servlet.annotation.Init;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Login Servlet. This servlet contains the landing page to finalize the authentication process initiated by the web application.
 *
 * @author fcorneli
 */
public class LoginServlet extends AbstractConfidentialLinkIDInjectionServlet {

    private static final Log LOG = LogFactory.getLog( LoginServlet.class );

    @Init(name = "ErrorPage", optional = true)
    private String errorPage;

    @Override
    protected void invokeGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        handleLanding( request, response );
    }

    @Override
    protected void invokePost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        handleLanding( request, response );
    }

    private void handleLanding(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        try {
            AuthnProtocolResponseContext authnResponse = ProtocolManager.findAndValidateAuthnResponse( request );
            if (null == authnResponse)
                authnResponse = ProtocolManager.findAndValidateAuthnAssertion( request, getContextFunction() );
            if (null == authnResponse) {
                LOG.error( ServletUtils.redirectToErrorPage( request, response, errorPage, null,
                        new ErrorMessage( "No expected or detached authentication responses found in request." ) ) );
                return;
            }

            onLogin( request.getSession(), authnResponse );

            response.sendRedirect( authnResponse.getRequest().getTarget() );
        }
        catch (ValidationFailedException ignored) {
            LOG.error( ServletUtils.redirectToErrorPage( request, response, errorPage, null,
                    new ErrorMessage( "Validation of authentication response failed." ) ) );
        }
    }

    /**
     * Override this method if you want to create a custom context for detached authentication responses.
     * <p/>
     * The standard implementation uses {@link AuthenticationContext#AuthenticationContext()}.
     *
     * @return A function that provides the context for validating detached authentication responses (assertions).
     */
    protected Function<AuthnProtocolResponseContext, AuthenticationContext> getContextFunction() {

        return new Function<AuthnProtocolResponseContext, AuthenticationContext>() {
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
    protected static void onLogin(HttpSession session, AuthnProtocolResponseContext authnResponse) {

        if (authnResponse.isSuccess()) {
            LOG.debug( "username: " + authnResponse.getUserId() );
            LoginManager.set( session, authnResponse.getUserId(), authnResponse.getAttributes(), authnResponse.getAuthenticatedDevices(),
                    authnResponse.getCertificateChain() );
        }
    }
}
