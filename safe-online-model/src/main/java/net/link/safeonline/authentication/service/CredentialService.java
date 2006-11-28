package net.link.safeonline.authentication.service;

import javax.ejb.Local;

import net.link.safeonline.authentication.exception.EntityNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;

@Local
public interface CredentialService {

	void changePassword(String login, String oldPassword, String newPassword)
			throws EntityNotFoundException, PermissionDeniedException;
}
