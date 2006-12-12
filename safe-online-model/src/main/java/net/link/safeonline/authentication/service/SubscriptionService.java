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
@Remote
public interface SubscriptionService {

	List<SubscriptionEntity> getSubscriptions();

	void subscribe(String applicationName) throws ApplicationNotFoundException,
			AlreadySubscribedException, PermissionDeniedException;

	/**
	 * @param applicationName
	 * @throws ApplicationNotFoundException
	 * @throws SubscriptionNotFoundException
	 * @throws PermissionDeniedException
	 *             in case the user is not the owner of the subscription.
	 */
	void unsubscribe(String applicationName)
			throws ApplicationNotFoundException, SubscriptionNotFoundException,
			PermissionDeniedException;

	long getNumberOfSubscriptions(String applicationName)
			throws ApplicationNotFoundException;
}
