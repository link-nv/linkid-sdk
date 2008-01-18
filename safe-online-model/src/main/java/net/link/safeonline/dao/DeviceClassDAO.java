/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.dao;

import java.util.List;

import javax.ejb.Local;

import net.link.safeonline.authentication.exception.DeviceClassDescriptionNotFoundException;
import net.link.safeonline.authentication.exception.DeviceClassNotFoundException;
import net.link.safeonline.entity.DeviceClassDescriptionEntity;
import net.link.safeonline.entity.DeviceClassDescriptionPK;
import net.link.safeonline.entity.DeviceClassEntity;

@Local
public interface DeviceClassDAO {

	DeviceClassEntity addDeviceClass(String name);

	DeviceClassEntity findDeviceClass(String deviceClassName);

	DeviceClassEntity getDeviceClass(String deviceClassName)
			throws DeviceClassNotFoundException;

	List<DeviceClassEntity> listDeviceClasses();

	List<DeviceClassDescriptionEntity> listDescriptions(
			DeviceClassEntity deviceClass);

	void addDescription(DeviceClassEntity deviceClass,
			DeviceClassDescriptionEntity description);

	void removeDescription(DeviceClassDescriptionEntity description);

	void saveDescription(DeviceClassDescriptionEntity description);

	DeviceClassDescriptionEntity getDescription(
			DeviceClassDescriptionPK descriptionPK)
			throws DeviceClassDescriptionNotFoundException;

	DeviceClassDescriptionEntity findDescription(
			DeviceClassDescriptionPK descriptionPK);

}
