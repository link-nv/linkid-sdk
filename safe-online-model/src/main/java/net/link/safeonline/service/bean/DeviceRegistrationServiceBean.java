/*
 * SafeOnline project.
 *
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.service.bean;

import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import net.link.safeonline.authentication.exception.DeviceNotFoundException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.service.DevicePolicyService;
import net.link.safeonline.dao.DeviceRegistrationDAO;
import net.link.safeonline.entity.DeviceEntity;
import net.link.safeonline.entity.DeviceRegistrationEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.service.DeviceRegistrationService;
import net.link.safeonline.service.SubjectService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <h2>{@link DeviceRegistrationServiceBean} - Service bean for device
 * registrations.</h2>
 * 
 * <p>
 * <i>Jan 29, 2008</i>
 * </p>
 * 
 * @author mbillemo
 */
@Stateless
public class DeviceRegistrationServiceBean implements DeviceRegistrationService {

	private final static Log LOG = LogFactory
			.getLog(DeviceRegistrationServiceBean.class);

	@EJB
	private DeviceRegistrationDAO deviceRegistrationDAO;

	@EJB
	private SubjectService subjectService;

	@EJB
	private DevicePolicyService devicePolicyService;

	/**
	 * {@inheritDoc}
	 */
	public DeviceRegistrationEntity registerDevice(String userId,
			String deviceName) throws SubjectNotFoundException,
			DeviceNotFoundException {
		SubjectEntity subject = this.subjectService.getSubject(userId);
		DeviceEntity device = this.devicePolicyService.getDevice(deviceName);

		DeviceRegistrationEntity registeredDevice = this.deviceRegistrationDAO
				.addRegisteredDevice(subject, device);
		return registeredDevice;
	}

	/**
	 * {@inheritDoc}
	 */
	public DeviceRegistrationEntity getDeviceRegistration(String id) {
		LOG.debug("get device registration: " + id);
		return this.deviceRegistrationDAO.findRegisteredDevice(id);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<DeviceRegistrationEntity> listDeviceRegistrations(
			SubjectEntity subject) {
		return this.deviceRegistrationDAO.listRegisteredDevices(subject);
	}

}
