package net.link.safeonline.authentication.service;

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
