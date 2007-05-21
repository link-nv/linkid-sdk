package net.link.safeonline.service;

import java.util.List;

import javax.ejb.Local;

import net.link.safeonline.entity.AllowedDeviceEntity;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.DeviceEntity;

@Local
public interface DeviceService {

	List<DeviceEntity> listDevices();

	List<AllowedDeviceEntity> listAllowedDevices(ApplicationEntity application);

	void setAllowedDevices(ApplicationEntity application,
			List<AllowedDeviceEntity> allowedDeviceList);

}
