/*
 * SafeOnline project.
 *
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.dao;

import java.util.List;

import javax.ejb.Local;

import net.link.safeonline.entity.DeviceEntity;
import net.link.safeonline.entity.RegisteredDeviceEntity;
import net.link.safeonline.entity.SubjectEntity;

@Local
public interface RegisteredDeviceDAO {

	public RegisteredDeviceEntity addRegisteredDevice(SubjectEntity subject,
			DeviceEntity device);

	public List<RegisteredDeviceEntity> listRegisteredDevices(
			SubjectEntity subject);

	public RegisteredDeviceEntity findRegisteredDevice(SubjectEntity subject,
			DeviceEntity device);

}
