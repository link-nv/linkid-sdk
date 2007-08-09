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
	 * applicationId of it thinks it can handle the authentication request. If
	 * the handler cannot handle the authentication request then it should
	 * return <code>null</code>.
	 * 
	 * @param authnRequest
	 * @return the application Id.
	 */
	String handleRequest(HttpServletRequest authnRequest);

	/**
	 * Gives back the informal name of the authentication protocol that this
	 * protocol handler supports.
	 * 
	 * @return
	 */
	String getName();
}
