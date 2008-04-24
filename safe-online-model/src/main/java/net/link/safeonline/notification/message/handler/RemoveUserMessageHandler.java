/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.notification.message.handler;

import java.util.LinkedList;
import java.util.List;

import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.exception.DeviceNotFoundException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.exception.SubscriptionNotFoundException;
import net.link.safeonline.authentication.service.UserIdMappingService;
import net.link.safeonline.dao.AttributeDAO;
import net.link.safeonline.dao.SubjectDAO;
import net.link.safeonline.dao.SubjectIdentifierDAO;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.DeviceEntity;
import net.link.safeonline.entity.DeviceRegistrationEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.notification.message.MessageHandler;
import net.link.safeonline.service.DeviceRegistrationService;
import net.link.safeonline.service.SubjectService;
import net.link.safeonline.util.ee.EjbUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Message handler for topic: {@link SafeOnlineConstants#TOPIC_REMOVE_USER)
 * 
 * @author wvdhaute
 * 
 */
public class RemoveUserMessageHandler implements MessageHandler {

	private static final Log LOG = LogFactory
			.getLog(RemoveUserMessageHandler.class);

	private UserIdMappingService userIdMappingService;

	private DeviceRegistrationService deviceRegistrationService;

	private SubjectService subjectService;

	private AttributeDAO attributeDAO;

	private SubjectIdentifierDAO subjectIdentifierDAO;

	private SubjectDAO subjectDAO;

	public void init() {
		this.userIdMappingService = EjbUtils.getEJB(
				"SafeOnline/UserIdMappingServiceBean/local",
				UserIdMappingService.class);
		this.deviceRegistrationService = EjbUtils.getEJB(
				"SafeOnline/DeviceRegistrationServiceBean/local",
				DeviceRegistrationService.class);
		this.subjectService = EjbUtils.getEJB(
				"SafeOnline/SubjectServiceBean/local", SubjectService.class);
		this.attributeDAO = EjbUtils.getEJB(
				"SafeOnline/AttributeDAOBean/local", AttributeDAO.class);
		this.subjectIdentifierDAO = EjbUtils.getEJB(
				"SafeOnline/SubjectIdentifierDAOBean/local",
				SubjectIdentifierDAO.class);
		this.subjectDAO = EjbUtils.getEJB("SafeOnline/SubjectDAOBean/local",
				SubjectDAO.class);
	}

	public void handleMessage(String destination, List<String> message) {
		String id = message.get(0);
		DeviceRegistrationEntity deviceRegistration = this.deviceRegistrationService
				.getDeviceRegistration(id);
		LOG.debug("remove device registration for " + id + " (device="
				+ deviceRegistration.getDevice().getName() + ")");
		SubjectEntity deviceSubject = this.subjectService
				.findSubject(deviceRegistration.getId());
		if (null != deviceSubject) {
			this.attributeDAO.removeAttributes(deviceSubject);
			this.subjectIdentifierDAO.removeSubjectIdentifiers(deviceSubject);
			this.subjectDAO.removeSubject(deviceSubject);
		}
	}

	public List<String> createApplicationMessage(List<String> message,
			ApplicationEntity application) {
		List<String> returnMessage = new LinkedList<String>();
		String userId = message.get(0);
		String applicationUserId;
		try {
			applicationUserId = this.userIdMappingService.getApplicationUserId(
					application.getName(), userId);
		} catch (SubscriptionNotFoundException e) {
			return null;
		} catch (ApplicationNotFoundException e) {
			return null;
		}
		returnMessage.add(applicationUserId);
		return returnMessage;
	}

	public List<String> createDeviceMessage(List<String> message,
			DeviceEntity device) {
		List<String> returnMessage = new LinkedList<String>();
		String userId = message.get(0);
		List<DeviceRegistrationEntity> deviceRegistrations;
		try {
			deviceRegistrations = this.deviceRegistrationService
					.listDeviceRegistrations(userId, device.getName());
		} catch (SubjectNotFoundException e) {
			return null;
		} catch (DeviceNotFoundException e) {
			return null;
		}
		if (0 == deviceRegistrations.size())
			return null;
		for (DeviceRegistrationEntity deviceRegistration : deviceRegistrations) {
			returnMessage.add(deviceRegistration.getId());
		}
		return returnMessage;
	}

}
