/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.authentication.service;

import javax.ejb.Local;
import javax.ejb.Remote;

import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.exception.ExistingUserException;

/**
 * User registration service interface.
 * 
 * The component implementing this interface will allow for registration of new
 * users within the SafeOnline core. This means creating a new Subject and
 * subscribing the new Subject to the safe-online-user application.
 * 
 * @author fcorneli
 * 
 */
@Local
@Remote
public interface UserRegistrationService {

	/**
	 * Registers a new user within the system.
	 * 
	 * @param login
	 * @param password
	 * @param name
	 * @throws ExistingUserException
	 * @throws ApplicationNotFoundException
	 */
	void registerUser(String login, String password, String name)
			throws ExistingUserException, ApplicationNotFoundException;
}
