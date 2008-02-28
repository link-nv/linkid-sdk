package net.link.safeonline.user;

import net.link.safeonline.entity.DeviceEntity;

public class DeviceEntry {

	private DeviceEntity device;

	private String friendlyName;

	public DeviceEntry(DeviceEntity device, String friendlyName) {
		this.device = device;
		this.friendlyName = friendlyName;
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
}