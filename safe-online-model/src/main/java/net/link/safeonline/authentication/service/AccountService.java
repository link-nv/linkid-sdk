/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.authentication.service;

import javax.ejb.Local;

import net.link.safeonline.authentication.exception.SubjectNotFoundException;

@Local
public interface AccountService {

	void removeAccount();

	/**
	 * Removes the specified account, not removing the subject identifers.
	 * 
	 * @param userId
	 * @throws SubjectNotFoundException
	 */
	void removeAccount(String userId) throws SubjectNotFoundException;
}
