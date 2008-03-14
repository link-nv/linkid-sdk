/*
 * SafeOnline project.
 *
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.service;

import java.util.List;

import javax.ejb.Local;

import net.link.safeonline.authentication.exception.DeviceNotFoundException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.entity.DeviceRegistrationEntity;
import net.link.safeonline.entity.SubjectEntity;

/**
 * <h2>{@link DeviceRegistrationService} - Service for device registration.</h2>
 * 
 * <p>
 * Creates device registrations for subject-device issuer pair. These
 * registrations contain a UUID that is used by the device provider to map the
 * identity provided by their device to an OLAS identity.
 * </p>
 * 
 * <p>
 * <i>Jan 29, 2008</i>
 * </p>
 * 
 * @author mbillemo
 */
@Local
public interface DeviceRegistrationService {

	public DeviceRegistrationEntity registerDevice(String userId,
			String deviceName) throws SubjectNotFoundException,
			DeviceNotFoundException;

	public DeviceRegistrationEntity getDeviceRegistration(String id);

	public List<DeviceRegistrationEntity> listDeviceRegistrations(
			SubjectEntity subject);
}
