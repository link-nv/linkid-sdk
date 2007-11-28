/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.device.backend;

import javax.ejb.Local;

import net.link.safeonline.authentication.exception.DeviceNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.entity.SubjectEntity;

@Local
public interface PasswordManager {

	void setPassword(SubjectEntity subject, String password)
			throws PermissionDeniedException;

	void changePassword(SubjectEntity subject, String oldPassword,
			String newPassword) throws PermissionDeniedException,
			DeviceNotFoundException;

	boolean validatePassword(SubjectEntity subject, String password)
			throws DeviceNotFoundException;

	boolean isPasswordConfigured(SubjectEntity subject);

}
