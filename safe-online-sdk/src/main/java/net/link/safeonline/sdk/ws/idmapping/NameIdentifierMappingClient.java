/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.ws.idmapping;

import net.link.safeonline.sdk.exception.RequestDeniedException;
import net.link.safeonline.sdk.exception.SubjectNotFoundException;
import net.link.safeonline.sdk.ws.MessageAccessor;

/**
 * Identifier Mapping Service Client interface.
 * 
 * @author fcorneli
 * 
 */
public interface NameIdentifierMappingClient extends MessageAccessor {

	/**
	 * Gives back the user Id corresponding with the given username.
	 * 
	 * @param username
	 * @return
	 * @throws SubjectNotFoundException
	 * @throws RequestDeniedException
	 */
	String getUserId(String username) throws SubjectNotFoundException,
			RequestDeniedException;
}
