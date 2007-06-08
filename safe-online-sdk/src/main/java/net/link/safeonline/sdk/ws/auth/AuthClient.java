/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.ws.auth;

/**
 * SafeOnline Authentication Web Service Client interface.
 * 
 * @author fcorneli
 * 
 */
public interface AuthClient {

	/**
	 * Echos the given message argument. This operation can be used to test the
	 * availability of the SafeOnline authentication web service.
	 * 
	 * @param message
	 *            the message to echo.
	 * @return the echo of the message.
	 */
	String echo(String message);

	/**
	 * Authenticates the user with password credential.
	 * 
	 * @param applicationName
	 *            the application.
	 * @param username
	 *            the username.
	 * @param password
	 *            the password credential.
	 * @return <code>true</code> if authenticated, <code>false</code>
	 *         otherwise.
	 */
	boolean authenticate(String applicationName, String username,
			String password);
}
