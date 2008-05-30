/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
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

import net.link.safeonline.sdk.auth.AuthenticationProtocolHandler;
import net.link.safeonline.sdk.auth.AuthenticationProtocolManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This filter performs the actual login using the identity as received from the
 * SafeOnline authentication web application.
 * 
 * @author fcorneli
 * 
 */
public class AuthnResponseFilter implements Filter {

	private static final Log LOG = LogFactory.getLog(AuthnResponseFilter.class);

	public static final String USERNAME_SESSION_PARAMETER = "UsernameSessionParameter";

	String sessionParameter;

	public void destroy() {
		LOG.debug("destroy");
	}

	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		LOG.debug("doFilter: " + httpRequest.getRequestURL());

		AuthenticationProtocolHandler protocolHandler = AuthenticationProtocolManager
				.findAuthenticationProtocolHandler(httpRequest);
		if (null == protocolHandler) {
			/*
			 * This means that no authentication process is active. Two
			 * possibilities: (1) user still needs to start the login process,
			 * or (2) the user is already authenticated. Either way, we simply
			 * continue without doing anything.
			 */
			chain.doFilter(request, response);
			return;
		}

		/*
		 * In this case there is an authentication process active.
		 * Possibilities: (1) the handler is capable of processing the incoming
		 * authentication response yielding an authenticated user. (2) the
		 * incoming request has nothing to do with authentication, thus the
		 * authentication handler stays quite. (3) the authentication handler
		 * explodes on the incoming authentication response because for example
		 * it has an invalid signature or it failed to link the authentication
		 * response with the current session.
		 */

		HttpServletResponse httpResponse = (HttpServletResponse) response;
		String username = protocolHandler.finalizeAuthentication(httpRequest,
				httpResponse);
		if (null != username) {
			LoginManager.setUsername(username, httpRequest,
					this.sessionParameter);
			AuthenticationProtocolManager
					.cleanupAuthenticationHandler(httpRequest);
			chain.doFilter(request, response);
			return;
		}

		LOG
				.debug("authentication process busy, but will not finalize right now");
		chain.doFilter(request, response);
	}

	public void init(FilterConfig config) {
		LOG.debug("init");
		this.sessionParameter = config
				.getInitParameter(USERNAME_SESSION_PARAMETER);
		if (this.sessionParameter == null) {
			this.sessionParameter = LoginManager.USERNAME_SESSION_ATTRIBUTE;
		}
	}
}
