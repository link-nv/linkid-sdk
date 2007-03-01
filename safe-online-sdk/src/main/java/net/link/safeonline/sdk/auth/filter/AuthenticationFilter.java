/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.auth.filter;

import java.io.IOException;
import java.net.URLEncoder;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * SafeOnline Authentication Filter. This filter can be used by servlet
 * container based web application for authentication via SafeOnline. The
 * configuration of this filter should be managed via the web.xml deployment
 * descriptor.
 * 
 * @author fcorneli
 * 
 */
public class AuthenticationFilter implements Filter {

	private static final Log LOG = LogFactory
			.getLog(AuthenticationFilter.class);

	public static final String AUTH_SERVICE_URL_INIT_PARAM = "SafeOnlineAuthenticationServiceUrl";

	public static final String APPLICATION_NAME_INIT_PARAM = "ApplicationName";

	private String safeOnlineAuthenticationServiceUrl;

	private String applicationName;

	public void init(FilterConfig config) throws ServletException {
		LOG.debug("init");
		this.safeOnlineAuthenticationServiceUrl = config
				.getInitParameter(AUTH_SERVICE_URL_INIT_PARAM);
		this.applicationName = config
				.getInitParameter(APPLICATION_NAME_INIT_PARAM);
		LOG
				.debug("redirection url: "
						+ this.safeOnlineAuthenticationServiceUrl);
		LOG.debug("application name: " + this.applicationName);
		if (null == this.safeOnlineAuthenticationServiceUrl) {
			throw new UnavailableException(AUTH_SERVICE_URL_INIT_PARAM
					+ " init param should be specified in web.xml");
		}
		if (null == this.applicationName) {
			throw new UnavailableException(APPLICATION_NAME_INIT_PARAM
					+ " init param should be specified in web.xml");
		}
	}

	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		LOG.debug("doFilter");
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpSession session = httpRequest.getSession();

		String paramUsername = httpRequest.getParameter("username");
		if (null != paramUsername) {
			LOG.debug("doing a login via the SafeOnline username token");
			session.setAttribute("username", paramUsername);
		}

		String username = (String) session.getAttribute("username");
		if (null == username) {
			outputRedirectPage(httpRequest, response);
		} else {
			chain.doFilter(request, response);
		}
	}

	private void outputRedirectPage(HttpServletRequest request,
			ServletResponse response) throws IOException {
		LOG.debug("redirecting to: " + this.safeOnlineAuthenticationServiceUrl);
		HttpServletResponse httpServletResponse = (HttpServletResponse) response;
		String redirectUrl = this.safeOnlineAuthenticationServiceUrl
				+ "?application="
				+ URLEncoder.encode(this.applicationName, "UTF-8")
				+ "&target="
				+ URLEncoder
						.encode(request.getRequestURL().toString(), "UTF-8");
		httpServletResponse.sendRedirect(redirectUrl);
	}

	public void destroy() {
		LOG.debug("destroy");
	}
}
