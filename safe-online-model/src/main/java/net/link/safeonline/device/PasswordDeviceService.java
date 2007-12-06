/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.device;

import javax.ejb.Local;

import net.link.safeonline.authentication.exception.DeviceNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.entity.SubjectEntity;

@Local
public interface PasswordDeviceService {

	SubjectEntity authenticate(String login, String password)
			throws DeviceNotFoundException, SubjectNotFoundException;

	void register(SubjectEntity subject, String password);

	void update(SubjectEntity subject, String oldPassword, String newPassword)
			throws PermissionDeniedException, DeviceNotFoundException;

	void remove(SubjectEntity subject, String password)
			throws DeviceNotFoundException, PermissionDeniedException;

}
