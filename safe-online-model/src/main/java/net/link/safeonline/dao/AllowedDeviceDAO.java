package net.link.safeonline.dao;

import java.util.List;

import javax.ejb.Local;

import net.link.safeonline.entity.AllowedDeviceEntity;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.DeviceEntity;

@Local
public interface AllowedDeviceDAO {

	AllowedDeviceEntity addAllowedDevice(ApplicationEntity application,
			DeviceEntity device, int weight);

	List<AllowedDeviceEntity> listAllowedDevices(ApplicationEntity application);

	void deleteAllowedDevices(ApplicationEntity application);

}
