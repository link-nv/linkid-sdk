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
import javax.servlet.UnavailableException;

import net.link.safeonline.sdk.auth.AuthenticationProtocol;
import net.link.safeonline.sdk.auth.AuthenticationProtocolHandler;
import net.link.safeonline.sdk.auth.AuthenticationProtocolManager;

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
 * <p>
 * The init parameter <code>SafeOnlineAuthenticationServiceUrl</code> should
 * point to the SafeOnline Authentication Web Application entry point.
 * </p>
 * 
 * <p>
 * The init parameter <code>ApplicationName</code> should contain the
 * application name of this service provider.
 * </p>
 * 
 * <p>
 * The init parameter <code>AuthenticationProtocol</code> should contain the
 * name of the protocol used between the SafeOnline authentication web
 * application and this service provider.
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

	public static final String AUTHN_PROTOCOL_INIT_PARAM = "AuthenticationProtocol";

	public static final AuthenticationProtocol DEFAULT_AUTHN_PROTOCOL = AuthenticationProtocol.SIMPLE_PLAIN_URL;

	private String safeOnlineAuthenticationServiceUrl;

	private String applicationName;

	private AuthenticationProtocol authenticationProtocol;

	public void init(FilterConfig config) throws ServletException {
		LOG.debug("init");
		this.safeOnlineAuthenticationServiceUrl = getInitParameter(config,
				AUTH_SERVICE_URL_INIT_PARAM);
		this.applicationName = getInitParameter(config,
				APPLICATION_NAME_INIT_PARAM);
		String authenticationProtocolString = getInitParameter(config,
				AUTHN_PROTOCOL_INIT_PARAM, DEFAULT_AUTHN_PROTOCOL.name());
		this.authenticationProtocol = AuthenticationProtocol
				.toAuthenticationProtocol(authenticationProtocolString);
		LOG.debug("authentication protocol: " + this.authenticationProtocol);
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

	private String getInitParameter(FilterConfig config, String initParamName,
			String defaultValue) {
		String initParamValue = config.getInitParameter(initParamName);
		if (null == initParamValue) {
			initParamValue = defaultValue;
		}
		return initParamValue;
	}

	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		LOG.debug("doFilter");
		boolean loggedIn = LoginManager.isAuthenticated(request);
		if (false == loggedIn) {
			initiateAuthentication(request, response);
		} else {
			chain.doFilter(request, response);
		}
	}

	private void initiateAuthentication(ServletRequest request,
			ServletResponse response) throws IOException, ServletException {
		AuthenticationProtocolHandler authenticationProtocolHandler = AuthenticationProtocolManager
				.getAuthenticationProtocolHandler(this.authenticationProtocol,
						this.safeOnlineAuthenticationServiceUrl,
						this.applicationName, null);
		authenticationProtocolHandler.initiateAuthentication(request, response,
				null);
	}

	public void destroy() {
		LOG.debug("destroy");
	}
}
