/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.service;

import java.util.List;

import javax.ejb.Local;

import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.entity.device.DeviceSubjectEntity;

/**
 * Service bean that manages the mapping of the subject's userId with the login
 * attribute
 * 
 * @author wvdhaute
 * 
 */

@Local
public interface SubjectService {
	/**
	 * Finds the subject for a given user ID. Returns <code>null</code> if the
	 * entity could not be found.
	 * 
	 * @param login
	 * @return the subject or <code>null</code> if the subject was not found.
	 */
	SubjectEntity findSubject(String userId);

	/**
	 * Finds the subject for a given user login. Returns <code>null</code> if
	 * the entity could not be found.
	 * 
	 * @param login
	 * @return the subject or <code>null</code> if the subject was not found.
	 */
	SubjectEntity findSubjectFromUserName(String login);

	/**
	 * Adds a subject for a given login. Generates a new UUID and adds the LOGIN
	 * attribute and SubjectIdentifier
	 * 
	 * @param login
	 * @throws AttributeTypeNotFoundException
	 */
	SubjectEntity addSubject(String login)
			throws AttributeTypeNotFoundException;

	/**
	 * Gives back the subject for the given user ID.
	 * 
	 * @param login
	 *            the login of the subject.
	 * @return the subject.
	 * @exception SubjectNotFoundException
	 */
	SubjectEntity getSubject(String userId) throws SubjectNotFoundException;

	/**
	 * Gives back the subject for the given user login name
	 * 
	 * @param login
	 * @throws SubjectNotFoundException
	 */
	SubjectEntity getSubjectFromUserName(String login)
			throws SubjectNotFoundException;

	/**
	 * Returns the value of the login attribute associated with the given user
	 * ID. Returns <code>null</code> if not found.
	 * 
	 * @param userId
	 */
	String getSubjectLogin(String userId);

	/**
	 * Same as getSubjectLogin but this can be called from within exception
	 * handling code ( meaning a transaction is created )
	 * 
	 * @param userId
	 */
	String getExceptionSubjectLogin(String userId);

	/**
	 * Returns list of users' login names starting with the specified prefix.
	 * 
	 * @param prefix
	 * @throws AttributeTypeNotFoundException
	 * 
	 */
	List<String> listUsers(String prefix) throws AttributeTypeNotFoundException;

	/**
	 * Adds a 'device subject'. This is used by external device issuers. The
	 * userId matches the device mapping ID returned by OLAS. Does NOT create a
	 * login attribute nor add a login-userId identifier mapping.
	 * 
	 * @param userId
	 * @return the subject.
	 */
	DeviceSubjectEntity addDeviceSubject(String userId);

	/**
	 * Adds a device registration subject. This subject represent a specific
	 * device registration. This is used by external device issuers. No login
	 * attribute nor identifier mapping will be created.
	 * 
	 * @return
	 */
	SubjectEntity addDeviceRegistration();

	/**
	 * Finds the device subject for a given user ID. Returns <code>null</code>
	 * if the entity could not be found. This is used by external device
	 * issuers.
	 * 
	 * @param deviceUserId
	 * @return the subject or <code>null</code> if the subject was not found.
	 */
	DeviceSubjectEntity findDeviceSubject(String deviceUserId);

	/**
	 * Gives back the device subject for a given device registration.
	 * 
	 * @param deviceRegistration
	 * @return the subject.
	 * @exception SubjectNotFoundException
	 * 
	 */
	DeviceSubjectEntity getDeviceSubject(SubjectEntity deviceRegistration)
			throws SubjectNotFoundException;

	/**
	 * Gives back the device subject for a given user ID.
	 * 
	 * @param deviceUserId
	 * @return the subject
	 * @exception SubjectNotFoundException
	 */
	DeviceSubjectEntity getDeviceSubject(String deviceUserId)
			throws SubjectNotFoundException;

}
