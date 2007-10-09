/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.authentication.service;

import javax.ejb.Local;

import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;

/**
 * Interface for identifier mapping service component.
 * 
 * @author fcorneli
 * 
 */
@Local
public interface IdentifierMappingService {

	/**
	 * Maps from the username to the userId.
	 * 
	 * @param username
	 * @return
	 * @exception SubjectNotFoundException
	 * @throws PermissionDeniedException
	 */
	String getUserId(String username) throws SubjectNotFoundException,
			PermissionDeniedException;
}
