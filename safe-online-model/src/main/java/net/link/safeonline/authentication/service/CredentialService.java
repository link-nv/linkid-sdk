/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.authentication.service;

import javax.ejb.Local;

import net.link.safeonline.authentication.exception.DeviceNotFoundException;
import net.link.safeonline.authentication.exception.LastDeviceException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;

/**
 * Interface of service that manages the credentials of the caller subject.
 * 
 * @author fcorneli
 * 
 */
@Local
public interface CredentialService {

	/**
	 * Changes the password of the current user. Of course for that to happen
	 * the oldPassword must match.
	 * 
	 * @param oldPassword
	 * @param newPassword
	 * @throws PermissionDeniedException
	 */
	void changePassword(String oldPassword, String newPassword)
			throws PermissionDeniedException, DeviceNotFoundException;

	/**
	 * Removes the password of the current user. For this to happen the password
	 * must match.
	 * 
	 * @param password
	 * @throws DeviceNotFoundException
	 * @throws PermissionDeniedException
	 * @throws LastDeviceException
	 */
	void removePassword(String password) throws DeviceNotFoundException,
			PermissionDeniedException, LastDeviceException;

	/**
	 * Gives back <code>true</code> if the user already has a password
	 * configured.
	 * 
	 */
	boolean isPasswordConfigured();
}
