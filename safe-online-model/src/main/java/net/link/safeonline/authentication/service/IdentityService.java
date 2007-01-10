/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.authentication.service;

import java.util.List;

import javax.ejb.Local;
import javax.ejb.Remote;

import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.entity.HistoryEntity;

/**
 * Interface of service component to access the identity data of a caller
 * subject.
 * 
 * @author fcorneli
 * 
 */
@Local
@Remote
public interface IdentityService {

	/**
	 * Gives back the authentication history of the user linked to the caller
	 * principal.
	 * 
	 * @return a list of history entries.
	 */
	List<HistoryEntity> getHistory();

	/**
	 * Gives back the value of the attribute for the current user.
	 * 
	 * @param attributeName
	 *            the name of the attribute.
	 * @return the value of the attribute, or <code>null</code> if not found.
	 * @throws PermissionDeniedException
	 *             if the user is not allowed to view the attribute.
	 */
	String findAttribute(String attributeName) throws PermissionDeniedException;

	/**
	 * Saves a (new) attribute value for the current user.
	 * 
	 * @param attributeName
	 *            the name of the attribute.
	 * @param attributeValue
	 *            the value of the attribute.
	 * @throws PermissionDeniedException
	 *             if the user is not allowed to edit the attribute.
	 */
	void saveAttribute(String attributeName, String attributeValue)
			throws PermissionDeniedException;
}
