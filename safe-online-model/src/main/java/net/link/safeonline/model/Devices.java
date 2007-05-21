package net.link.safeonline.model;

import java.util.List;

import javax.ejb.Local;

import net.link.safeonline.entity.AllowedDeviceEntity;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.DeviceEntity;

@Local
public interface Devices {

	List<AllowedDeviceEntity> listAllowedDevices(ApplicationEntity application);

	List<DeviceEntity> listDevices();

	void setAllowedDevices(ApplicationEntity application,
			List<AllowedDeviceEntity> allowedDevices);

}
