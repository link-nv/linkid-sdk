package net.link.safeonline.authentication.service;

import javax.ejb.Local;

import net.link.safeonline.authentication.exception.PermissionDeniedException;

@Local
public interface CredentialService {

	void changePassword(String oldPassword, String newPassword)
			throws PermissionDeniedException;
}
