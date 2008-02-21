/*
 * SafeOnline project.
 *
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.service.bean;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import net.link.safeonline.authentication.exception.DeviceNotFoundException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.service.DevicePolicyService;
import net.link.safeonline.dao.RegisteredDeviceDAO;
import net.link.safeonline.entity.DeviceEntity;
import net.link.safeonline.entity.RegisteredDeviceEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.service.RegisteredDeviceService;
import net.link.safeonline.service.SubjectService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <h2>{@link RegisteredDeviceServiceBean} - Service bean for device
 * registrations.</h2>
 * 
 * <p>
 * <i>Jan 29, 2008</i>
 * </p>
 * 
 * @author mbillemo
 */
@Stateless
public class RegisteredDeviceServiceBean implements RegisteredDeviceService {

	private final static Log LOG = LogFactory
			.getLog(RegisteredDeviceServiceBean.class);

	@EJB
	private RegisteredDeviceDAO registeredDeviceDAO;

	@EJB
	private SubjectService subjectService;

	@EJB
	private DevicePolicyService devicePolicyService;

	/**
	 * {@inheritDoc}
	 */
	public RegisteredDeviceEntity registerDevice(String userId,
			String deviceName) throws SubjectNotFoundException,
			DeviceNotFoundException {
		SubjectEntity subject = this.subjectService.getSubject(userId);
		DeviceEntity device = this.devicePolicyService.getDevice(deviceName);
		return this.registeredDeviceDAO.addRegisteredDevice(subject, device);
	}

	/**
	 * {@inheritDoc}
	 */
	public RegisteredDeviceEntity getRegisteredDevice(String id) {
		LOG.debug("get registered device: " + id);
		return this.registeredDeviceDAO.findRegisteredDevice(id);
	}

}
