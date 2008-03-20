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
import net.link.safeonline.entity.DeviceRegistrationEntity;
import net.link.safeonline.entity.SubjectEntity;

@Local
public interface DeviceRegistrationDAO {

	public DeviceRegistrationEntity addRegisteredDevice(SubjectEntity subject,
			DeviceEntity device);

	public List<DeviceRegistrationEntity> listRegisteredDevices(
			SubjectEntity subject);

	public DeviceRegistrationEntity findRegisteredDevice(String id);

	public List<DeviceRegistrationEntity> listRegisteredDevices(
			SubjectEntity subject, DeviceEntity device);

	public void removeRegisteredDevice(String id);

}
