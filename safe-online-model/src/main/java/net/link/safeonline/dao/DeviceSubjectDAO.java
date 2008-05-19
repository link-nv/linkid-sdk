/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.dao;

import javax.ejb.Local;

import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.entity.device.DeviceSubjectEntity;

/**
 * Device Subject Data Access Object interface.
 * 
 * @author wvdhaute
 * 
 */
@Local
public interface DeviceSubjectDAO {
	/**
	 * Finds the device subject for a given user ID. Returns <code>null</code>
	 * if the entity could not be found.
	 * 
	 * @param userId
	 * @return the subject or <code>null</code> if the subject was not found.
	 */
	DeviceSubjectEntity findSubject(String userId);

	DeviceSubjectEntity addSubject(String userId);

	/**
	 * Gives back the device subject for the given userId.
	 * 
	 * @param userId
	 *            the userId of the subject.
	 * @return the subject.
	 * @exception SubjectNotFoundException
	 */
	DeviceSubjectEntity getSubject(String userId)
			throws SubjectNotFoundException;

	/**
	 * Removes the given attached subject from the database.
	 * 
	 * @param subject
	 */
	void removeSubject(DeviceSubjectEntity subject);

	/**
	 * Gives back the device subject for the given device registration.
	 * 
	 * @param deviceRegistration
	 * @return the device subject
	 * @exception SubjectNotFoundException
	 */
	DeviceSubjectEntity getSubject(SubjectEntity deviceRegistration)
			throws SubjectNotFoundException;
}
