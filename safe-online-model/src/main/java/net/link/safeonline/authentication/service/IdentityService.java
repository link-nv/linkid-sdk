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

import net.link.safeonline.authentication.exception.ApplicationIdentityNotFoundException;
import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.SubscriptionNotFoundException;
import net.link.safeonline.entity.AttributeTypeEntity;
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
	String findAttributeValue(String attributeName)
			throws PermissionDeniedException;

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

	/**
	 * Gives back a list of attributes for the current user. Only the attributes
	 * that are user visible will be returned.
	 * 
	 * @return
	 */
	List<AttributeDO> getAttributes();

	/**
	 * Checks whether confirmation is required over the usage of the identity
	 * attributes use by the given application.
	 * 
	 * @param applicationName
	 * @return
	 * @throws ApplicationNotFoundException
	 * @throws SubscriptionNotFoundException
	 * @throws ApplicationIdentityNotFoundException
	 */
	boolean isConfirmationRequired(String applicationName)
			throws ApplicationNotFoundException, SubscriptionNotFoundException,
			ApplicationIdentityNotFoundException;

	/**
	 * Confirm the current identity for the given application.
	 * 
	 * TODO: add version to be confirmed.
	 * 
	 * To make this method really bullet proof we would have to pass the version
	 * number itself. This because it's possible that the operator is changing
	 * the identity while the user is confirming it. This would make the user to
	 * confirm a more recent identity version that the one he was presented.
	 * 
	 * @param applicationName
	 * @throws ApplicationNotFoundException
	 * @throws SubscriptionNotFoundException
	 */
	void confirmIdentity(String applicationName)
			throws ApplicationNotFoundException, SubscriptionNotFoundException;

	List<AttributeTypeEntity> getIdentityAttributesToConfirm(
			String applicationName) throws ApplicationNotFoundException,
			ApplicationIdentityNotFoundException, SubscriptionNotFoundException;
}
