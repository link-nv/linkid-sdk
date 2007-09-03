/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.webapp.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.UnavailableException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Servlet Filter that handles browser timeout events.
 * 
 * <p>
 * The init parameters for this filter are:
 * </p>
 * <ul>
 * <li><code>TimeoutPath</code>: the path to the timeout page.</li>
 * <li><code>LoginSessionAttribute</code>: the HTTP session attribute that
 * indicated a logged in user.</li>
 * </ul>
 * 
 * @author fcorneli
 * 
 */
public class TimeoutFilter implements Filter {

	public static final String TIMEOUT_PATH_INIT_PARAM = "TimeoutPath";

	public static final String LOGIN_SESSION_ATTRIBUTE_INIT_PARAM = "LoginSessionAttribute";

	private static final Log LOG = LogFactory.getLog(TimeoutFilter.class);

	private String timeoutPath;

	private String loginSessionAttribute;

	public void destroy() {
		LOG.debug("destroy");
	}

	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		String requestedSessionId = httpRequest.getRequestedSessionId();
		if (null == requestedSessionId) {
			/*
			 * This means that the user just got here for the first time.
			 */
			chain.doFilter(request, response);
			return;
		}
		boolean requestedSessionIdValid = httpRequest
				.isRequestedSessionIdValid();
		if (true == requestedSessionIdValid) {
			/*
			 * This means that the servlet container found a matching session
			 * context for the requested session Id.
			 */
			HttpSession session = httpRequest.getSession();
			Object loginSessionAttribute = session
					.getAttribute(this.loginSessionAttribute);
			if (null != loginSessionAttribute) {
				if (false == hasLoginCookie(httpRequest)) {
					HttpServletResponse httpResponse = (HttpServletResponse) response;
					/*
					 * We communicate that the user has been properly logged in
					 * by setting a non-persistent browser cookie.
					 */
					Cookie loginCookie = new Cookie("login", "true");
					httpResponse.addCookie(loginCookie);
				}
			} else {
				/*
				 * If the user performs a logout, we need to remove the login
				 * cookie. Else the user could trigger an explicit timeout while
				 * actually he's no longer logged in.
				 */
				removeLoginCookie(httpRequest, response);
			}
			chain.doFilter(request, response);
			return;
		}
		/*
		 * In this case no corresponding session context for the given requested
		 * session Id was found. This could be an indication that the browser
		 * caused a timeout on the web application. We detect this via the login
		 * cookie.
		 */
		if (true == hasLoginCookie(httpRequest)) {
			LOG.debug("forwaring to timeout path: " + this.timeoutPath);
			removeLoginCookie(httpRequest, response);
			RequestDispatcher requestDispatcher = request
					.getRequestDispatcher(this.timeoutPath);
			requestDispatcher.forward(request, response);
			return;
		}
		/*
		 * If no login cookie was found, then the browser indeed caused a
		 * timeout on the HTTP session, but since the user was not yet logged
		 * in, it's not that harmful.
		 */
		chain.doFilter(request, response);
	}

	private void removeLoginCookie(HttpServletRequest httpRequest,
			ServletResponse response) {
		if (false == hasLoginCookie(httpRequest)) {
			return;
		}
		HttpServletResponse httpResponse = (HttpServletResponse) response;
		Cookie loginCookie = new Cookie("login", "");
		loginCookie.setMaxAge(0);
		httpResponse.addCookie(loginCookie);
	}

	private boolean hasLoginCookie(HttpServletRequest httpRequest) {
		Cookie[] cookies = httpRequest.getCookies();
		if (null == cookies) {
			return false;
		}
		for (Cookie cookie : cookies) {
			if ("login".equals(cookie.getName())) {
				return true;
			}
		}
		return false;
	}

	public void init(FilterConfig config) throws ServletException {
		LOG.debug("init");
		this.timeoutPath = getInitParameter(config, TIMEOUT_PATH_INIT_PARAM);
		this.loginSessionAttribute = getInitParameter(config,
				LOGIN_SESSION_ATTRIBUTE_INIT_PARAM);
	}

	private String getInitParameter(FilterConfig config, String parameterName)
			throws UnavailableException {
		String value = config.getInitParameter(parameterName);
		if (null == value) {
			throw new UnavailableException("missing init parameter: "
					+ parameterName);
		}
		return value;
	}
}
