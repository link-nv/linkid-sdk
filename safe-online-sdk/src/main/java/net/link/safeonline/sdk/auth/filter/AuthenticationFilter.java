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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * SafeOnline Authentication Filter. This filter can be used by servlet
 * container based web applications for authentication via SafeOnline. This
 * filter initiates the authentication request towards the SafeOnline
 * authentication web application. The handling of the authentication response
 * is done via the {@link LoginFilter}.
 * 
 * <p>
 * The configuration of this filter should be managed via the
 * <code>web.xml</code> deployment descriptor.
 * </p>
 * 
 * @author fcorneli
 * @see LoginFilter
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
		this.safeOnlineAuthenticationServiceUrl = getInitParameter(config,
				AUTH_SERVICE_URL_INIT_PARAM);
		this.applicationName = getInitParameter(config,
				APPLICATION_NAME_INIT_PARAM);
	}

	private String getInitParameter(FilterConfig config, String initParamName)
			throws UnavailableException {
		String initParam = config.getInitParameter(initParamName);
		if (null == initParam) {
			String msg = "init param \"" + initParamName
					+ "\"should be specified in web.xml";
			LOG.error(msg);
			throw new UnavailableException(msg);
		}
		return initParam;
	}

	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		LOG.debug("doFilter");
		boolean loggedIn = LoginManager.isAuthenticated(request);
		if (false == loggedIn) {
			outputRedirectPage(request, response);
		} else {
			chain.doFilter(request, response);
		}
	}

	private void outputRedirectPage(ServletRequest request,
			ServletResponse response) throws IOException {
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		LOG.debug("redirecting to: " + this.safeOnlineAuthenticationServiceUrl);
		String targetUrl = httpRequest.getRequestURL().toString();
		LOG.debug("target url: " + targetUrl);
		HttpServletResponse httpServletResponse = (HttpServletResponse) response;
		String redirectUrl = this.safeOnlineAuthenticationServiceUrl
				+ "?application="
				+ URLEncoder.encode(this.applicationName, "UTF-8") + "&target="
				+ URLEncoder.encode(targetUrl, "UTF-8");
		httpServletResponse.sendRedirect(redirectUrl);
	}

	public void destroy() {
		LOG.debug("destroy");
	}
}
