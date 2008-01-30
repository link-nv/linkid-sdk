/*
 * SafeOnline project.
 *
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.service.bean;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import net.link.safeonline.dao.RegisteredDeviceDAO;
import net.link.safeonline.entity.DeviceEntity;
import net.link.safeonline.entity.RegisteredDeviceEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.service.RegisteredDeviceService;

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

	@EJB
	private RegisteredDeviceDAO registeredDeviceDAO;

	/**
	 * {@inheritDoc}
	 */
	public RegisteredDeviceEntity getDeviceRegistration(SubjectEntity subject,
			DeviceEntity device) {

		RegisteredDeviceEntity registeredDevice = this.registeredDeviceDAO
				.findRegisteredDevice(subject, device);

		if (registeredDevice == null)
			registeredDevice = this.registeredDeviceDAO.addRegisteredDevice(
					subject, device);

		return registeredDevice;
	}

}
