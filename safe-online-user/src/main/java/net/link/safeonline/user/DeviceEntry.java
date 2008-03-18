package net.link.safeonline.user;

import java.util.List;

import net.link.safeonline.data.AttributeDO;
import net.link.safeonline.entity.DeviceEntity;

public class DeviceEntry {

	private DeviceEntity device;

	private String friendlyName;

	private List<AttributeDO> registeredDevices;

	public DeviceEntry(DeviceEntity device, String friendlyName,
			List<AttributeDO> registeredDevices) {
		this.device = device;
		this.friendlyName = friendlyName;
		this.registeredDevices = registeredDevices;
	}

	public DeviceEntity getDevice() {
		return this.device;
	}

	public void setDevice(DeviceEntity device) {
		this.device = device;
	}

	public String getFriendlyName() {
		return this.friendlyName;
	}

	public void setFriendlyName(String friendlyName) {
		this.friendlyName = friendlyName;
	}

	public List<AttributeDO> getRegisteredDevices() {
		return this.registeredDevices;
	}

	public void setRegisteredDevices(List<AttributeDO> registeredDevices) {
		this.registeredDevices = registeredDevices;
	}
}
