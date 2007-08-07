/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.auth;

import java.io.IOException;
import java.net.URLEncoder;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Implementation class for the Simple Plain URL authentication protocol.
 * 
 * <p>
 * The authentication request is done via a simple redirect towards the
 * authentication web application entry point. Two request parameters are
 * passed: <code>application</code> holds the application name and
 * <code>target</code> holds the URL of the local resource to which the
 * authentication web application should redirect after successful
 * authenticating the user. This comes with the request paramater
 * <code>username</code> containing the name of the authenticated user.
 * </p>
 * 
 * @author fcorneli
 * 
 */
@SupportedAuthenticationProtocol(AuthenticationProtocol.SIMPLE_PLAIN_URL)
public class SimplePlainUrlAuthenticationProtocolHandler implements
		AuthenticationProtocolHandler {

	private static final Log LOG = LogFactory
			.getLog(SimplePlainUrlAuthenticationProtocolHandler.class);

	private String authnServiceUrl;

	private String applicationName;

	public void init(String authnServiceUrl, String applicationName) {
		LOG.debug("init");
		this.authnServiceUrl = authnServiceUrl;
		this.applicationName = applicationName;
	}

	public void initiateAuthentication(ServletRequest request,
			ServletResponse response, String targetUrl) throws IOException {
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		LOG.debug("redirecting to: " + this.authnServiceUrl);
		if (null == targetUrl) {
			/*
			 * In this case we default to the request URL.
			 */
			targetUrl = httpRequest.getRequestURL().toString();
		}
		LOG.debug("target url: " + targetUrl);
		HttpServletResponse httpServletResponse = (HttpServletResponse) response;
		String redirectUrl = this.authnServiceUrl + "?application="
				+ URLEncoder.encode(this.applicationName, "UTF-8") + "&target="
				+ URLEncoder.encode(targetUrl, "UTF-8");
		httpServletResponse.sendRedirect(redirectUrl);
	}
}
