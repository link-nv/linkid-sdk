/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.authentication.service;

import javax.ejb.Local;
import javax.ejb.Remote;

import net.link.safeonline.authentication.exception.PermissionDeniedException;

/**
 * Interface of service that manages the credentials of the caller subject.
 * 
 * @author fcorneli
 * 
 */
@Local
@Remote
public interface CredentialService {

	void changePassword(String oldPassword, String newPassword)
			throws PermissionDeniedException;
}
