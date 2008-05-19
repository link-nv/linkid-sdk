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
import net.link.safeonline.entity.DeviceMappingEntity;
import net.link.safeonline.entity.SubjectEntity;

@Local
public interface DeviceMappingDAO {

	public DeviceMappingEntity addDeviceMapping(SubjectEntity subject,
			DeviceEntity device);

	public List<DeviceMappingEntity> listDeviceMappings(SubjectEntity subject);

	public DeviceMappingEntity findDeviceMapping(SubjectEntity subject,
			DeviceEntity device);

	public DeviceMappingEntity findDeviceMapping(String id);

	public void removeDeviceMappings(SubjectEntity subject);

	public List<DeviceMappingEntity> listDeviceMappings(DeviceEntity device);

}
