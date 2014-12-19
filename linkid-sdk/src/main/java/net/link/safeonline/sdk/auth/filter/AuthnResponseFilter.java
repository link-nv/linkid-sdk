/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.auth.filter;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import net.link.safeonline.sdk.auth.protocol.AuthnProtocolResponseContext;
import net.link.safeonline.sdk.auth.protocol.ProtocolManager;
import net.link.util.exception.ValidationFailedException;
import net.link.util.logging.Logger;
import net.link.util.servlet.ErrorMessage;
import net.link.util.servlet.ServletUtils;


/**
 * This filter performs the actual login using the identity as received from the SafeOnline authentication web application.
 *
 * @author fcorneli
 */
public class AuthnResponseFilter implements Filter {

    private static final Logger logger = Logger.get( AuthnResponseFilter.class );

    public static final String ERROR_PAGE = "ErrorPage";

    private String errorPage;

    @Override
    public void destroy() {

    }

    @Override
    public void init(final FilterConfig filterConfig)
            throws ServletException {

        this.errorPage = filterConfig.getInitParameter( ERROR_PAGE );
        if (null == this.errorPage)
            this.errorPage = filterConfig.getServletContext().getInitParameter( ERROR_PAGE );
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        try {
            AuthnProtocolResponseContext authnResponse = ProtocolManager.findAndValidateAuthnResponse( httpRequest );
            if (null != authnResponse)
                onLogin( httpRequest.getSession(), authnResponse );
        }
        catch (ValidationFailedException e) {
            logger.err( e, "Validation failed: %s", e.getMessage() );
            ServletUtils.redirectToErrorPage( httpRequest, httpResponse, errorPage, null, new ErrorMessage( e ) );
        }
        catch (RuntimeException e) {
            logger.err( e, "Validation failed: %s", e.getMessage() );
        }

        chain.doFilter( request, response );
    }

    /**
     * Invoked when an authentication response is received.  The default implementation sets the user's credentials on the session if the
     * response was successful and does nothing if it wasn't.
     *
     * @param session       The HTTP session within which the response was received.
     * @param authnResponse The response that was received.
     */
    protected void onLogin(HttpSession session, AuthnProtocolResponseContext authnResponse) {

        if (authnResponse.isSuccess()) {
            logger.dbg( "userId: %s", authnResponse.getUserId() );

            LoginManager.set( session, authnResponse );
        }
    }
}
