/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.device.bean;

import java.util.Date;

import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.Stateless;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.audit.SecurityAuditLogger;
import net.link.safeonline.authentication.exception.DeviceNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.dao.HistoryDAO;
import net.link.safeonline.device.PasswordDeviceService;
import net.link.safeonline.device.PasswordDeviceServiceRemote;
import net.link.safeonline.device.backend.PasswordManager;
import net.link.safeonline.entity.DeviceMappingEntity;
import net.link.safeonline.entity.HistoryEventType;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.entity.audit.SecurityThreatType;
import net.link.safeonline.entity.device.DeviceSubjectEntity;
import net.link.safeonline.service.DeviceMappingService;
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
	private DeviceMappingService deviceMappingService;

	@EJB
	private PasswordManager passwordManager;

	@EJB
	private SecurityAuditLogger securityAuditLogger;

	@EJB
	private HistoryDAO historyDAO;

	public SubjectEntity authenticate(String loginName, String password)
			throws DeviceNotFoundException, SubjectNotFoundException {
		LOG.debug("authenticate \"" + loginName + "\"");

		SubjectEntity subject = this.subjectService
				.getSubjectFromUserName(loginName);
		DeviceMappingEntity deviceMapping = this.deviceMappingService
				.getDeviceMapping(subject.getUserId(),
						SafeOnlineConstants.USERNAME_PASSWORD_DEVICE_ID);
		DeviceSubjectEntity deviceSubject = this.subjectService
				.getDeviceSubject(deviceMapping.getId());
		if (deviceSubject.getRegistrations().isEmpty())
			throw new SubjectNotFoundException();

		SubjectEntity deviceRegistration = deviceSubject.getRegistrations()
				.get(0);

		boolean validationResult = false;
		try {
			validationResult = this.passwordManager.validatePassword(
					deviceRegistration, password);
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

	public void register(String userId, String password)
			throws SubjectNotFoundException, DeviceNotFoundException {
		SubjectEntity subject = this.subjectService.getSubject(userId);
		register(subject, password);
	}

	public void register(SubjectEntity subject, String password)
			throws SubjectNotFoundException, DeviceNotFoundException {
		DeviceMappingEntity deviceMapping = this.deviceMappingService
				.getDeviceMapping(subject.getUserId(),
						SafeOnlineConstants.USERNAME_PASSWORD_DEVICE_ID);
		/*
		 * Create new device subject
		 */
		DeviceSubjectEntity deviceSubject = this.subjectService
				.findDeviceSubject(deviceMapping.getId());
		if (null == deviceSubject)
			deviceSubject = this.subjectService.addDeviceSubject(deviceMapping
					.getId());

		/*
		 * Create new device registration subject
		 */
		SubjectEntity deviceRegistration = this.subjectService
				.addDeviceRegistration();
		deviceSubject.getRegistrations().add(deviceRegistration);

		LOG.debug("register \"" + deviceRegistration.getUserId() + "\"");
		try {
			this.passwordManager.setPassword(deviceRegistration, password);
		} catch (PermissionDeniedException e) {
			throw new EJBException("Not allowed to set password");
		}

	}

	public void remove(SubjectEntity subject, String password)
			throws DeviceNotFoundException, PermissionDeniedException,
			SubjectNotFoundException {
		LOG.debug("remove " + subject.getUserId());
		DeviceMappingEntity deviceMapping = this.deviceMappingService
				.getDeviceMapping(subject.getUserId(),
						SafeOnlineConstants.USERNAME_PASSWORD_DEVICE_ID);
		DeviceSubjectEntity deviceSubject = this.subjectService
				.getDeviceSubject(deviceMapping.getId());
		if (deviceSubject.getRegistrations().isEmpty())
			throw new SubjectNotFoundException();

		SubjectEntity deviceRegistration = deviceSubject.getRegistrations()
				.get(0);

		this.passwordManager.removePassword(deviceRegistration, password);
		deviceSubject.getRegistrations().remove(deviceRegistration);
	}

	public void update(SubjectEntity subject, String oldPassword,
			String newPassword) throws PermissionDeniedException,
			DeviceNotFoundException, SubjectNotFoundException {
		LOG.debug("update \"" + subject.getUserId() + "\"");

		DeviceMappingEntity deviceMapping = this.deviceMappingService
				.getDeviceMapping(subject.getUserId(),
						SafeOnlineConstants.USERNAME_PASSWORD_DEVICE_ID);
		DeviceSubjectEntity deviceSubject = this.subjectService
				.getDeviceSubject(deviceMapping.getId());
		if (deviceSubject.getRegistrations().isEmpty())
			throw new SubjectNotFoundException();

		SubjectEntity deviceRegistration = deviceSubject.getRegistrations()
				.get(0);
		this.passwordManager.changePassword(deviceRegistration, oldPassword,
				newPassword);

	}

	public boolean isPasswordConfigured(SubjectEntity subject)
			throws SubjectNotFoundException, DeviceNotFoundException {
		DeviceMappingEntity deviceMapping = this.deviceMappingService
				.getDeviceMapping(subject.getUserId(),
						SafeOnlineConstants.USERNAME_PASSWORD_DEVICE_ID);
		DeviceSubjectEntity deviceSubject = this.subjectService
				.findDeviceSubject(deviceMapping.getId());
		if (null == deviceSubject)
			return false;
		if (deviceSubject.getRegistrations().isEmpty())
			return false;

		SubjectEntity deviceRegistration = deviceSubject.getRegistrations()
				.get(0);
		return this.passwordManager.isPasswordConfigured(deviceRegistration);
	}
}
