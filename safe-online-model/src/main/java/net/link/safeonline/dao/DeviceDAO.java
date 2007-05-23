/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.dao;

import java.util.List;

import javax.ejb.Local;

import net.link.safeonline.authentication.exception.DeviceNotFoundException;
import net.link.safeonline.entity.DeviceEntity;

@Local
public interface DeviceDAO {

	DeviceEntity addDevice(String name);

	List<DeviceEntity> listDevices();

	DeviceEntity findDevice(String name);

	DeviceEntity getDevice(String name) throws DeviceNotFoundException;
}
