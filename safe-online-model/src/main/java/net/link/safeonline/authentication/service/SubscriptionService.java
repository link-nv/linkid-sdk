/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.authentication.service;

import java.util.List;

import javax.ejb.Local;

import net.link.safeonline.authentication.exception.AlreadySubscribedException;
import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.SubscriptionNotFoundException;
import net.link.safeonline.entity.SubscriptionEntity;

/**
 * Interface to service components that manages the application subscriptions of
 * the caller principal.
 * 
 * @author fcorneli
 * 
 */
@Local
public interface SubscriptionService {

	/**
	 * Gives back a list of all application subscriptions of the caller user.
	 * 
	 * @return
	 */
	List<SubscriptionEntity> listSubscriptions();

	/**
	 * Subscribe the caller user to the given application.
	 * 
	 * @param applicationName
	 * @throws ApplicationNotFoundException
	 * @throws AlreadySubscribedException
	 * @throws PermissionDeniedException
	 */
	void subscribe(String applicationName) throws ApplicationNotFoundException,
			AlreadySubscribedException, PermissionDeniedException;

	/**
	 * Unsubscribe the caller user from the given application.
	 * 
	 * @param applicationName
	 * @throws ApplicationNotFoundException
	 * @throws SubscriptionNotFoundException
	 * @throws PermissionDeniedException
	 *             in case the user is not the owner of the subscription.
	 */
	void unsubscribe(String applicationName)
			throws ApplicationNotFoundException, SubscriptionNotFoundException,
			PermissionDeniedException;

	/**
	 * Gives back the number of subscriptions for a given application.
	 * 
	 * @param applicationName
	 * @return
	 * @throws ApplicationNotFoundException
	 * @throws PermissionDeniedException
	 */
	long getNumberOfSubscriptions(String applicationName)
			throws ApplicationNotFoundException, PermissionDeniedException;
}
