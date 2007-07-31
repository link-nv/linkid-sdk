/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.auth;

import java.io.IOException;

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
	 */
	void init(String authnServiceUrl, String applicationName);

	/**
	 * Initiates the authentication request towards the SafeOnline
	 * authentication web application.
	 * 
	 * @param request
	 * @param response
	 * @throws IOException
	 */
	void initiateAuthentication(ServletRequest request, ServletResponse response)
			throws IOException;
}
