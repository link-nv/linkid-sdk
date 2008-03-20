/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.device.bean;

import java.util.Date;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.Stateless;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.audit.SecurityAuditLogger;
import net.link.safeonline.authentication.exception.DeviceNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.dao.DeviceDAO;
import net.link.safeonline.dao.HistoryDAO;
import net.link.safeonline.device.PasswordDeviceService;
import net.link.safeonline.device.PasswordDeviceServiceRemote;
import net.link.safeonline.device.backend.PasswordManager;
import net.link.safeonline.entity.DeviceEntity;
import net.link.safeonline.entity.DeviceRegistrationEntity;
import net.link.safeonline.entity.HistoryEventType;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.entity.audit.SecurityThreatType;
import net.link.safeonline.service.DeviceRegistrationService;
import net.link.safeonline.service.SubjectService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@Stateless
public class PasswordDeviceServiceBean implements PasswordDeviceService,
		PasswordDeviceServiceRemote {

	private final static Log LOG = LogFactory
			.getLog(PasswordDeviceServiceBean.class);

	@EJB
	private SubjectService subjectService;

	@EJB
	private DeviceDAO deviceDAO;

	@EJB
	private DeviceRegistrationService deviceRegistrationService;

	@EJB
	private PasswordManager passwordManager;

	@EJB
	private SecurityAuditLogger securityAuditLogger;

	@EJB
	private HistoryDAO historyDAO;

	public SubjectEntity authenticate(String login, String password)
			throws DeviceNotFoundException, SubjectNotFoundException {
		LOG.debug("authenticate \"" + login + "\"");

		SubjectEntity subject = this.subjectService
				.getSubjectFromUserName(login);
		DeviceEntity device = this.deviceDAO
				.getDevice(SafeOnlineConstants.USERNAME_PASSWORD_DEVICE_ID);
		List<DeviceRegistrationEntity> deviceRegistrations = this.deviceRegistrationService
				.listDeviceRegistrations(subject, device);
		if (deviceRegistrations.isEmpty())
			throw new SubjectNotFoundException();
		SubjectEntity deviceSubject = this.subjectService
				.getSubject(deviceRegistrations.get(0).getId());

		boolean validationResult = false;
		try {
			validationResult = this.passwordManager.validatePassword(
					deviceSubject, password);
		} catch (DeviceNotFoundException e) {
			this.historyDAO.addHExceptionHistoryEntry(new Date(), subject,
					HistoryEventType.LOGIN_PASSWORD_ATTRIBUTE_NOT_FOUND, null,
					null);
			throw e;
		}

		if (!validationResult) {
			this.historyDAO.addHistoryEntry(subject,
					HistoryEventType.LOGIN_INCORRECT_PASSWORD, null, null);
			this.securityAuditLogger.addSecurityAudit(
					SecurityThreatType.DECEPTION, subject.getUserId(),
					"incorrect password");
			return null;
		}
		return subject;
	}

	public void register(String login, String password)
			throws SubjectNotFoundException, DeviceNotFoundException {
		SubjectEntity subject = this.subjectService.getSubject(login);
		register(subject, password);
	}

	public void register(SubjectEntity subject, String password)
			throws SubjectNotFoundException, DeviceNotFoundException {
		DeviceRegistrationEntity deviceRegistrationEntity = this.deviceRegistrationService
				.registerDevice(subject.getUserId(),
						SafeOnlineConstants.USERNAME_PASSWORD_DEVICE_ID);
		SubjectEntity deviceSubject = this.subjectService
				.addDeviceSubject(deviceRegistrationEntity.getId());

		LOG.debug("register \"" + deviceSubject.getUserId() + "\"");
		try {
			this.passwordManager.setPassword(deviceSubject, password);
		} catch (PermissionDeniedException e) {
			throw new EJBException("Not allowed to set password");
		}

	}

	public void remove(SubjectEntity subject, String password)
			throws DeviceNotFoundException, PermissionDeniedException,
			SubjectNotFoundException {
		LOG.debug("remove " + subject.getUserId());
		DeviceEntity device = this.deviceDAO
				.getDevice(SafeOnlineConstants.USERNAME_PASSWORD_DEVICE_ID);
		List<DeviceRegistrationEntity> deviceRegistrations = this.deviceRegistrationService
				.listDeviceRegistrations(subject, device);
		if (deviceRegistrations.isEmpty())
			throw new SubjectNotFoundException();
		SubjectEntity deviceSubject = this.subjectService
				.getSubject(deviceRegistrations.get(0).getId());
		this.deviceRegistrationService
				.removeDeviceRegistration(deviceRegistrations.get(0).getId());

		this.passwordManager.removePassword(deviceSubject, password);
	}

	public void update(SubjectEntity subject, String oldPassword,
			String newPassword) throws PermissionDeniedException,
			DeviceNotFoundException, SubjectNotFoundException {
		LOG.debug("update \"" + subject.getUserId() + "\"");

		DeviceEntity device = this.deviceDAO
				.getDevice(SafeOnlineConstants.USERNAME_PASSWORD_DEVICE_ID);
		List<DeviceRegistrationEntity> deviceRegistrations = this.deviceRegistrationService
				.listDeviceRegistrations(subject, device);
		if (deviceRegistrations.isEmpty())
			throw new SubjectNotFoundException();
		SubjectEntity deviceSubject = this.subjectService
				.getSubject(deviceRegistrations.get(0).getId());

		this.passwordManager.changePassword(deviceSubject, oldPassword,
				newPassword);

	}

	public boolean isPasswordConfigured(SubjectEntity subject)
			throws SubjectNotFoundException, DeviceNotFoundException {
		DeviceEntity device = this.deviceDAO
				.getDevice(SafeOnlineConstants.USERNAME_PASSWORD_DEVICE_ID);
		List<DeviceRegistrationEntity> deviceRegistrations = this.deviceRegistrationService
				.listDeviceRegistrations(subject, device);
		if (deviceRegistrations.isEmpty())
			throw new SubjectNotFoundException();
		SubjectEntity deviceSubject = this.subjectService
				.getSubject(deviceRegistrations.get(0).getId());
		return this.passwordManager.isPasswordConfigured(deviceSubject);
	}
}
