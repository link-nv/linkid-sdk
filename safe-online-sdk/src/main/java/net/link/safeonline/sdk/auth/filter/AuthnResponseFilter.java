/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.auth.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.link.safeonline.sdk.auth.AuthenticationProtocolHandler;
import net.link.safeonline.sdk.auth.AuthenticationProtocolManager;
import net.link.safeonline.util.servlet.AbstractInjectionFilter;
import net.link.safeonline.util.servlet.annotation.Init;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * This filter performs the actual login using the identity as received from the SafeOnline authentication web
 * application.
 * 
 * @author fcorneli
 * 
 */
public class AuthnResponseFilter extends AbstractInjectionFilter {

    private static final Log LOG = LogFactory.getLog(AuthnResponseFilter.class);

    @Init(name = "UsernameSessionParameter", defaultValue = LoginManager.USERID_SESSION_ATTRIBUTE)
    private String           sessionParameter;


    public void destroy() {

        LOG.debug("destroy");
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
            ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        LOG.debug("doFilter: " + httpRequest.getRequestURL());

        AuthenticationProtocolHandler protocolHandler = AuthenticationProtocolManager
                .findAuthenticationProtocolHandler(httpRequest);
        if (null == protocolHandler) {
            /*
             * This means that no authentication process is active. Two possibilities: (1) user still needs to start the
             * login process, or (2) the user is already authenticated. Either way, we simply continue without doing
             * anything.
             */
            chain.doFilter(request, response);
            return;
        }

        /*
         * In this case there is an authentication process active. Possibilities: (1) the handler is capable of
         * processing the incoming authentication response yielding an authenticated user. (2) the incoming request has
         * nothing to do with authentication, thus the authentication handler stays quite. (3) the authentication
         * handler explodes on the incoming authentication response because for example it has an invalid signature or
         * it failed to link the authentication response with the current session.
         */

        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String userId = protocolHandler.finalizeAuthentication(httpRequest, httpResponse);
        if (null != userId) {
            LoginManager.setUserId(userId, httpRequest, this.sessionParameter);
            AuthenticationProtocolManager.cleanupAuthenticationHandler(httpRequest);
            chain.doFilter(request, response);
            return;
        }

        LOG.debug("authentication process busy, but will not finalize right now");
        chain.doFilter(request, response);
    }
}
