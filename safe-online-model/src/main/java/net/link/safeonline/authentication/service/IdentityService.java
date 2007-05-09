/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.authentication.service;

import java.util.List;
import java.util.Locale;

import javax.ejb.Local;

import net.link.safeonline.authentication.exception.ApplicationIdentityNotFoundException;
import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.SubscriptionNotFoundException;
import net.link.safeonline.entity.HistoryEntity;

/**
 * Interface of service component to access the identity data of a caller
 * subject.
 * 
 * @author fcorneli
 * 
 */
@Local
public interface IdentityService {

	/**
	 * Gives back the authentication history of the user linked to the caller
	 * principal.
	 * 
	 * @return a list of history entries.
	 */
	List<HistoryEntity> listHistory();

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
	 * Saves an (new) attribute value for the current user.
	 * 
	 * @throws PermissionDeniedException
	 *             if the user is not allowed to edit the attribute.
	 */
	void saveAttribute(AttributeDO attribute) throws PermissionDeniedException;

	/**
	 * Gives back a list of attributes for the current user. Only the attributes
	 * that are user visible will be returned.
	 * 
	 * @param locale
	 *            the optional locale that should be used to i18n the response.
	 * 
	 * @return
	 */
	List<AttributeDO> listAttributes(Locale locale);

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
	 * @throws ApplicationIdentityNotFoundException
	 */
	void confirmIdentity(String applicationName)
			throws ApplicationNotFoundException, SubscriptionNotFoundException,
			ApplicationIdentityNotFoundException;

	/**
	 * Lists the attributes for which the user has confirmed an identity on the
	 * given application.
	 * 
	 * @param applicationName
	 * @param locale
	 *            the optional locale.
	 * @return
	 * @throws ApplicationNotFoundException
	 * @throws SubscriptionNotFoundException
	 * @throws ApplicationIdentityNotFoundException
	 */
	List<AttributeDO> listConfirmedIdentity(String applicationName,
			Locale locale) throws ApplicationNotFoundException,
			SubscriptionNotFoundException, ApplicationIdentityNotFoundException;

	/**
	 * Gives back a list of identity attributes that need to be confirmed by
	 * this user in order to be in-line with the latest identity requirement of
	 * the given application.
	 * 
	 * @param applicationName
	 * @param locale
	 *            the optional locale to be applied to the result.
	 * @return
	 * @throws ApplicationNotFoundException
	 * @throws ApplicationIdentityNotFoundException
	 * @throws SubscriptionNotFoundException
	 */
	List<AttributeDO> listIdentityAttributesToConfirm(String applicationName,
			Locale locale) throws ApplicationNotFoundException,
			ApplicationIdentityNotFoundException, SubscriptionNotFoundException;

	/**
	 * Checks whether the current user still needs to fill in some attribute
	 * values for being able to use the given application.
	 * 
	 * @param applicationName
	 * @return <code>true</code> if there are missing attributes,
	 *         <code>false</code> otherwise.
	 * @throws ApplicationNotFoundException
	 * @throws ApplicationIdentityNotFoundException
	 */
	boolean hasMissingAttributes(String applicationName)
			throws ApplicationNotFoundException,
			ApplicationIdentityNotFoundException;

	/**
	 * Gives back a list of the user's missing attributes for the given
	 * application.
	 * 
	 * @param applicationName
	 * @param locale
	 *            the optional locale for i18n of the result.
	 * @return
	 * @throws ApplicationNotFoundException
	 * @throws ApplicationIdentityNotFoundException
	 */
	List<AttributeDO> getMissingAttributes(String applicationName, Locale locale)
			throws ApplicationNotFoundException,
			ApplicationIdentityNotFoundException;
}
