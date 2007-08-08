/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.auth;

import java.io.IOException;
import java.security.KeyPair;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * Interface for authentication protocol handlers.
 * 
 * @author fcorneli
 * 
 */
public interface AuthenticationProtocolHandler {

	/**
	 * Initializes the authentication protocol handler.
	 * 
	 * @param authnServiceUrl
	 *            the URL of the authentication service to be used by the
	 *            handler.
	 * @param applicationName
	 *            the application name to be used by the handler.
	 * @param applicationKeyPair
	 *            the application RSA key pair used to sign the authentication
	 *            request.
	 */
	void init(String authnServiceUrl, String applicationName,
			KeyPair applicationKeyPair);

	/**
	 * Initiates the authentication request towards the SafeOnline
	 * authentication web application.
	 * 
	 * @param request
	 * @param response
	 * @param targetUrl
	 *            the optional target URL. If omitted the request URL will be
	 *            used as target URL.
	 * @throws IOException
	 * @throws ServletException
	 */
	void initiateAuthentication(ServletRequest request,
			ServletResponse response, String targetUrl) throws IOException,
			ServletException;
}
