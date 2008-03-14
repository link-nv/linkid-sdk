/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.authentication.service;

import java.net.MalformedURLException;
import java.security.NoSuchAlgorithmException;

import javax.ejb.Local;

import net.link.safeonline.authentication.exception.ArgumentIntegrityException;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.ExistingUserException;
import net.link.safeonline.authentication.exception.MobileException;
import net.link.safeonline.authentication.exception.MobileRegistrationException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.entity.SubjectEntity;

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
	 * Checks whether the given login name already exists and has completed
	 * device registrations. In case there are existing device registrations it
	 * will poll those device issuers if the registration actually completed. If
	 * one them has completed, return null.
	 * 
	 * If no such login exists, register and return the subject.
	 * 
	 * If no completed device registrations were found, return the subject.
	 * 
	 * @param login
	 * @throws AttributeTypeNotFoundException
	 * @throws ExistingUserException
	 * @throws PermissionDeniedException
	 * @throws SubjectNotFoundException
	 */
	SubjectEntity checkLogin(String login) throws ExistingUserException,
			AttributeTypeNotFoundException, SubjectNotFoundException,
			PermissionDeniedException;

	/**
	 * Registers a new user within the system
	 * 
	 * @param login
	 * @param mobile
	 * @return activation code for mobile client app
	 * @throws MobileRegistrationException
	 * @throws MalformedURLException
	 * @throws MobileException
	 * @throws AttributeTypeNotFoundException
	 * @throws ExistingUserException
	 * @throws ArgumentIntegrityException
	 */
	String registerMobile(String login, String mobile) throws MobileException,
			MalformedURLException, MobileRegistrationException,
			ExistingUserException, AttributeTypeNotFoundException,
			ArgumentIntegrityException;

	/**
	 * Make mobile services generate registering user an OTP
	 * 
	 * @param mobile
	 * @return challengeId of the sent OTP
	 * @throws MalformedURLException
	 * @throws MobileException
	 */
	String requestMobileOTP(String mobile) throws MalformedURLException,
			MobileException;
}
