/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.auth.protocol;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Interface for server-side authentication protocol handlers.
 * 
 * Protocol handlers should be implemented as stateless POJOs.
 * 
 * @author fcorneli
 * 
 */
public interface ProtocolHandler {

	/**
	 * Request handle method. The protocol handler should return a filled in
	 * protocol context if it could handle the authentication request. If the
	 * handler cannot handle the authentication request then it should return
	 * <code>null</code>. A {@link ProtocolException} should be thrown in
	 * case this handler can handle the authentication request but the request
	 * itself violates the authentication protocol supported by this handler.
	 * 
	 * @param authnRequest
	 * @return the protocol context or <code>null</code>.
	 * @throws ProtocolException
	 *             in case the authentication request violates the
	 *             authentication protocol supported by this handler.
	 */
	ProtocolContext handleRequest(HttpServletRequest authnRequest)
			throws ProtocolException;

	/**
	 * Performs the authentication response according to the protocol supported
	 * by the handler that implements this interface.
	 * 
	 * @param session
	 * @param authnResponse
	 * @throws ProtocolException
	 */
	void authnResponse(HttpSession session, HttpServletResponse authnResponse)
			throws ProtocolException;

	/**
	 * Gives back the informal human-readable name of the authentication
	 * protocol that this protocol handler supports. This name can be used on
	 * error pages.
	 * 
	 * @return
	 */
	String getName();
}
