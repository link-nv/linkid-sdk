/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.webapp.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.link.safeonline.util.ee.BufferedServletResponseWrapper;
import net.link.safeonline.util.servlet.AbstractInjectionFilter;
import net.link.safeonline.util.servlet.annotation.Init;

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
public class TimeoutFilter extends AbstractInjectionFilter {

	private static final Log LOG = LogFactory.getLog(TimeoutFilter.class);

	private static final String LOGIN_COOKIE = "OLAS.login";

	@Init(name = "TimeoutPath")
	private String timeoutPath;

	@Init(name = "LoginSessionAttribute")
	private String loginSessionAttribute;

	public void destroy() {
		LOG.debug("destroy");
	}

	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		LOG.debug("doFilter");
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;

		String authenticationTimeout = httpRequest
				.getParameter("authenticationTimeout");
		LOG.debug("authenticationTimeout=" + authenticationTimeout);
		if (null != authenticationTimeout) {
			LOG.debug("return from timeout authentication webapp");
			chain.doFilter(request, response);
			return;
		}

		String requestedSessionId = httpRequest.getRequestedSessionId();
		if (null == requestedSessionId) {
			/*
			 * This means that the user just got here for the first time.
			 */
			LOG.debug("no session");
			chain.doFilter(request, response);
			return;
		}
		boolean requestedSessionIdValid = httpRequest
				.isRequestedSessionIdValid();
		if (true == requestedSessionIdValid) {
			/*
			 * We wrap the response since we need to be able to add cookies
			 * after the body has been committed.
			 */
			BufferedServletResponseWrapper timeoutResponseWrapper = new BufferedServletResponseWrapper(
					httpResponse);
			LOG.debug("chain.doFilter");
			chain.doFilter(request, timeoutResponseWrapper);
			// chain.doFilter(request, response);
			/*
			 * This means that the servlet container found a matching session
			 * context for the requested session Id.
			 */
			HttpSession session = httpRequest.getSession();
			Object tempLoginSessionAttribute = session
					.getAttribute(this.loginSessionAttribute);
			if (null != tempLoginSessionAttribute) {
				addCookie(LOGIN_COOKIE, "true", httpRequest.getContextPath(),
						httpRequest, httpResponse);
			} else {
				/*
				 * If the user performs a logout, we need to remove the login
				 * cookie. Else the user could trigger an explicit timeout while
				 * actually he's no longer logged in.
				 */
				removeCookie(LOGIN_COOKIE, httpRequest.getContextPath(),
						httpRequest, httpResponse);
			}
			timeoutResponseWrapper.commit();
			return;
		}
		/*
		 * In this case no corresponding session context for the given requested
		 * session Id was found. This could be an indication that the browser
		 * caused a timeout on the web application. We detect this via the login
		 * cookie.
		 */
		if (true == hasCookie(LOGIN_COOKIE, httpRequest)) {
			LOG.debug("forwaring to timeout path: " + this.timeoutPath);
			removeCookie(LOGIN_COOKIE, httpRequest.getContextPath(),
					httpRequest, httpResponse);
			httpResponse.sendRedirect(this.timeoutPath);
			return;
		}
		/*
		 * If no login cookie was found, then the browser indeed caused a
		 * timeout on the HTTP session, but since the user was not yet logged
		 * in, it's not that harmful.
		 */
		LOG.debug("non harmful timeout");
		chain.doFilter(request, response);
	}
}
