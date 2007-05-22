package net.link.safeonline.dao;

import java.util.List;

import javax.ejb.Local;

import net.link.safeonline.entity.DeviceEntity;

@Local
public interface DeviceDAO {

	DeviceEntity addDevice(String name);

	List<DeviceEntity> listDevices();

	DeviceEntity findDevice(String name);

}
