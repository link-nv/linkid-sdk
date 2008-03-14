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
import net.link.safeonline.dao.DeviceMappingDAO;
import net.link.safeonline.entity.DeviceEntity;
import net.link.safeonline.entity.DeviceMappingEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.service.DeviceMappingService;
import net.link.safeonline.service.SubjectService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <h2>{@link DeviceMappingServiceBean} - Service bean for device mappings.</h2>
 * 
 * <p>
 * <i>Jan 29, 2008</i>
 * </p>
 * 
 * @author mbillemo
 */
@Stateless
public class DeviceMappingServiceBean implements DeviceMappingService {

	private final static Log LOG = LogFactory
			.getLog(DeviceMappingServiceBean.class);

	@EJB
	private DeviceMappingDAO deviceMappingDAO;

	@EJB
	private SubjectService subjectService;

	@EJB
	private DevicePolicyService devicePolicyService;

	/**
	 * {@inheritDoc}
	 */
	public DeviceMappingEntity getDeviceMapping(String userId, String deviceName)
			throws SubjectNotFoundException, DeviceNotFoundException {
		SubjectEntity subject = this.subjectService.getSubject(userId);
		DeviceEntity device = this.devicePolicyService.getDevice(deviceName);

		DeviceMappingEntity registeredDevice = this.deviceMappingDAO
				.findDeviceMapping(subject, device);
		if (null == registeredDevice)
			registeredDevice = this.deviceMappingDAO.addDeviceMapping(subject,
					device);
		return registeredDevice;
	}

	/**
	 * {@inheritDoc}
	 */
	public DeviceMappingEntity getDeviceMapping(String id) {
		LOG.debug("get device mapping: " + id);
		return this.deviceMappingDAO.findDeviceMapping(id);
	}

}
