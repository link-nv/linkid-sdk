/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.authentication.service;

import java.security.NoSuchAlgorithmException;

import javax.ejb.Local;

import net.link.safeonline.authentication.exception.ArgumentIntegrityException;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.ExistingUserException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.pkix.exception.TrustDomainNotFoundException;

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
public interface UserRegistrationService {

	/**
	 * Registers a new user within the system.
	 * 
	 * @param login
	 * @param password
	 * @param name
	 * @throws ExistingUserException
	 * @throws NoSuchAlgorithmException
	 * @throws SubjectIdNotUniqueException
	 * @throws AttributeTypeNotFoundException
	 * @throws SubjectNotFoundException
	 */
	void registerUser(String login, String password)
			throws ExistingUserException, AttributeTypeNotFoundException;

	/**
	 * Registers a new user using the identity statement.
	 * 
	 * @param login
	 * @param identityStatementData
	 * @throws ExistingUserException
	 * @throws AttributeTypeNotFoundException
	 * @throws ArgumentIntegrityException
	 * @throws PermissionDeniedException
	 * @throws TrustDomainNotFoundException
	 * @throws NoSuchAlgorithmException
	 * @throws SubjectIdNotUniqueException
	 * @throws SubjectNotFoundException
	 */
	void registerUser(String login, byte[] identityStatementData)
			throws ExistingUserException, TrustDomainNotFoundException,
			PermissionDeniedException, ArgumentIntegrityException,
			AttributeTypeNotFoundException;

	/**
	 * Checks whether the given login Id is still free to use or not.
	 * 
	 * @param login
	 * @return
	 */
	boolean isLoginFree(String login);
}
