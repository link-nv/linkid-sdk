package net.link.safeonline.authentication.service.bean;

import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.Stateless;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.DeviceNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.service.PasswordManager;
import net.link.safeonline.dao.AttributeDAO;
import net.link.safeonline.dao.AttributeTypeDAO;
import net.link.safeonline.entity.AttributeEntity;
import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.entity.SubjectEntity;

@Stateless
public class PasswordManagerBean implements PasswordManager {

	@EJB
	private AttributeDAO attributeDAO;

	@EJB
	private AttributeTypeDAO attributeTypeDAO;

	public void changePassword(SubjectEntity subject, String oldPassword,
			String newPassword) throws PermissionDeniedException,
			DeviceNotFoundException {

		if (!validatePassword(subject, oldPassword)) {
			throw new PermissionDeniedException();
		}

		setPassword(subject, newPassword, true);

	}

	public void setPassword(SubjectEntity subject, String password,
			boolean forceOverwrite) throws PermissionDeniedException {

		AttributeTypeEntity passwordAttributeType;
		try {
			passwordAttributeType = this.attributeTypeDAO
					.getAttributeType(SafeOnlineConstants.PASSWORD_ATTRIBUTE);
		} catch (AttributeTypeNotFoundException e) {
			throw new EJBException("password attribute type not found");
		}

		AttributeEntity passwordAttribute;

		try {
			passwordAttribute = getPasswordAttribute(subject.getLogin());
			if (!forceOverwrite)
				throw new PermissionDeniedException();
			passwordAttribute.setStringValue(password);
		} catch (DeviceNotFoundException e) {
			this.attributeDAO.addAttribute(passwordAttributeType, subject,
					password);
		}
	}

	public boolean validatePassword(SubjectEntity subject, String password)
			throws DeviceNotFoundException {
		String expectedPassword = getPasswordAttribute(subject.getLogin())
				.getStringValue();
		if (expectedPassword.equals(password)) {
			return true;
		}
		return false;
	}

	private AttributeEntity getPasswordAttribute(String login)
			throws DeviceNotFoundException {
		AttributeEntity passwordAttribute = this.attributeDAO.findAttribute(
				SafeOnlineConstants.PASSWORD_ATTRIBUTE, login);
		if (null == passwordAttribute) {
			throw new DeviceNotFoundException();
		}
		String password = passwordAttribute.getStringValue();
		if (null == password) {
			throw new DeviceNotFoundException();
		}

		return passwordAttribute;
	}

	public boolean isPasswordConfigured(SubjectEntity subject) {
		try {
			getPasswordAttribute(subject.getLogin());
		} catch (DeviceNotFoundException e) {
			return false;
		}
		return true;
	}
}
