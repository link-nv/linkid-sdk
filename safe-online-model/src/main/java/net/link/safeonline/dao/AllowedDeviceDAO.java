/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.dao;

import java.util.List;

import javax.ejb.Local;

import net.link.safeonline.entity.AllowedDeviceEntity;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.DeviceEntity;


@Local
public interface AllowedDeviceDAO {

    AllowedDeviceEntity addAllowedDevice(ApplicationEntity application, DeviceEntity device, int weight);

    AllowedDeviceEntity findAllowedDevice(ApplicationEntity application, DeviceEntity device);

    List<AllowedDeviceEntity> listAllowedDevices(ApplicationEntity application);

    void deleteAllowedDevices(ApplicationEntity application);
}
