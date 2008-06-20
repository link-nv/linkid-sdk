/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.authentication.service;

import javax.ejb.Local;

import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.exception.SubscriptionNotFoundException;

/**
 * Interface for identifier mapping service component.
 * 
 * @author fcorneli
 * 
 */
@Local
public interface ApplicationIdentifierMappingService {

	/**
	 * Returns the subject's user id using the application's id scope.
	 * 
	 * @param username
	 * @throws PermissionDeniedException
	 * @throws ApplicationNotFoundException
	 * @throws SubscriptionNotFoundException
	 * @throws SubjectNotFoundException
	 */
	String getApplicationUserId(String username)
			throws PermissionDeniedException, ApplicationNotFoundException,
			SubscriptionNotFoundException, SubjectNotFoundException;

	/**
	 * Returns the global OLAS user ID using the application's id scope. Returns
	 * null if not found.
	 * 
	 * @param applicationName
	 * @param applicationUserId
	 * @throws ApplicationNotFoundException
	 */
	String findUserId(String applicationName, String applicationUserId)
			throws ApplicationNotFoundException;
}
