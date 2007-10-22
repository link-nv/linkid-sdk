/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.auth;

import java.io.IOException;
import java.net.URLEncoder;
import java.security.KeyPair;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Implementation class for the Simple Plain URL authentication protocol. This
 * protocol is not doing any security or challenge-response at all.
 * 
 * <p>
 * The authentication request is done via a simple redirect towards the
 * authentication web application entry point. Two request parameters are
 * passed: <code>application</code> holds the application name and
 * <code>target</code> holds the URL of the local resource to which the
 * authentication web application should redirect after successful
 * authenticating the user.
 * </p>
 * 
 * <p>
 * The authentication response comes with the request parameter
 * <code>username</code> containing the name of the authenticated user.
 * </p>
 * 
 * @author fcorneli
 * 
 */
@SupportedAuthenticationProtocol(AuthenticationProtocol.SIMPLE_PLAIN_URL)
public class SimplePlainUrlAuthenticationProtocolHandler implements
		AuthenticationProtocolHandler {

	private static final long serialVersionUID = 1L;

	private static final Log LOG = LogFactory
			.getLog(SimplePlainUrlAuthenticationProtocolHandler.class);

	private String authnServiceUrl;

	private String applicationName;

	public void init(String inAuthnServiceUrl, String inApplicationName,
			KeyPair applicationKeyPair, Map<String, String> configParams) {
		LOG.debug("init");
		this.authnServiceUrl = inAuthnServiceUrl + "/entry";
		this.applicationName = inApplicationName;
	}

	public void initiateAuthentication(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse, String targetUrl)
			throws IOException {
		LOG.debug("redirecting to: " + this.authnServiceUrl);
		String Url = targetUrl;
		if (null == targetUrl) {
			/*
			 * In this case we default to the request URL.
			 */
			Url = httpRequest.getRequestURL().toString();
		}
		LOG.debug("target url: " + Url);
		String redirectUrl = this.authnServiceUrl + "?application="
				+ URLEncoder.encode(this.applicationName, "UTF-8") + "&target="
				+ URLEncoder.encode(Url, "UTF-8");
		httpResponse.sendRedirect(redirectUrl);
	}

	public String finalizeAuthentication(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse) {
		if (false == "GET".equals(httpRequest.getMethod())) {
			/*
			 * Nothing to see here, move along.
			 */
			return null;
		}
		String username = httpRequest.getParameter("username");
		return username;
	}
}
