/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.auth.protocol;

import javax.servlet.http.HttpServletRequest;

/**
 * Interface for server-side authentication protocol handlers.
 * 
 * @author fcorneli
 * 
 */
public interface ProtocolHandler {

	/**
	 * Request handle method. The protocol handler should return the
	 * applicationId if it could handle the authentication request. If the
	 * handler cannot handle the authentication request then it should return
	 * <code>null</code>. A {@link ProtocolException} should be thrown in
	 * case this handler can handle the authentication request but the request
	 * itself violates the authentication protocol supported by this handler.
	 * 
	 * @param authnRequest
	 * @return the application Id.
	 * @throws ProtocolException
	 *             in case the authentication request violates the
	 *             authentication protocol supported by this handler.
	 */
	String handleRequest(HttpServletRequest authnRequest)
			throws ProtocolException;

	/**
	 * Gives back the informal name of the authentication protocol that this
	 * protocol handler supports.
	 * 
	 * @return
	 */
	String getName();
}
