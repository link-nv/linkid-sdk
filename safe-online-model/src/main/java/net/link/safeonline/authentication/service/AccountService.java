/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.authentication.service;

import javax.ejb.Local;

import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.exception.SubscriptionNotFoundException;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.notification.exception.MessageHandlerNotFoundException;

@Local
public interface AccountService {

	void removeAccount() throws SubscriptionNotFoundException,
			MessageHandlerNotFoundException;

	void removeAccount(SubjectEntity subject)
			throws SubscriptionNotFoundException,
			MessageHandlerNotFoundException;

	/**
	 * Removes the specified account, not removing the subject identifers.
	 * 
	 * @param userId
	 * @throws SubjectNotFoundException
	 * @throws MessageHandlerNotFoundException
	 */
	void removeAccount(String userId) throws SubjectNotFoundException,
			SubscriptionNotFoundException, MessageHandlerNotFoundException;
}
