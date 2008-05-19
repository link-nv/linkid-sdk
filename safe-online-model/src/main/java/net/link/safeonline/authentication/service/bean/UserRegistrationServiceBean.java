/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.authentication.service.bean;

import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.ExistingUserException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.service.ProxyAttributeService;
import net.link.safeonline.authentication.service.UserRegistrationService;
import net.link.safeonline.authentication.service.UserRegistrationServiceRemote;
import net.link.safeonline.entity.DeviceMappingEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.model.UserRegistrationManager;
import net.link.safeonline.service.DeviceMappingService;
import net.link.safeonline.service.SubjectService;

/**
 * Implementation of user registration service interface. This component does
 * not live within the SafeOnline core security domain. This because a user that
 * is about to register himself is not yet logged on into the system.
 * 
 * @author fcorneli
 * 
 */
@Stateless
public class UserRegistrationServiceBean implements UserRegistrationService,
		UserRegistrationServiceRemote {

	@EJB
	private SubjectService subjectService;

	@EJB
	private UserRegistrationManager userRegistrationManager;

	@EJB
	private DeviceMappingService deviceMappingService;

	@EJB
	private ProxyAttributeService proxyAttributeService;

	public SubjectEntity registerUser(String login)
			throws ExistingUserException, AttributeTypeNotFoundException,
			PermissionDeniedException {
		SubjectEntity subject = this.subjectService
				.findSubjectFromUserName(login);
		if (null == subject)
			return this.userRegistrationManager.registerUser(login);

		// Subject already exists, check for attached registered devices
		List<DeviceMappingEntity> deviceMappings = this.deviceMappingService
				.listDeviceMappings(subject);
		if (deviceMappings.isEmpty())
			return subject;

		// For each device mapping, poll device issuer if registration
		// actually completed
		for (DeviceMappingEntity deviceMapping : deviceMappings) {
			Object deviceAttribute = this.proxyAttributeService
					.findDeviceAttributeValue(deviceMapping.getId(),
							deviceMapping.getDevice().getAttributeType()
									.getName());
			if (null != deviceAttribute)
				throw new ExistingUserException();
		}
		return subject;
	}
}
