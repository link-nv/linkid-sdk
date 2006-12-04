package net.link.safeonline.authentication.service;

import javax.ejb.Local;

import net.link.safeonline.authentication.exception.PermissionDeniedException;

/**
 * Interface of service that manages the credentials of the caller subject.
 * 
 * @author fcorneli
 * 
 */
@Local
public interface CredentialService {

	void changePassword(String oldPassword, String newPassword)
			throws PermissionDeniedException;
}
