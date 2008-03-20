package net.link.safeonline.user;

import java.util.List;

import net.link.safeonline.data.AttributeDO;
import net.link.safeonline.entity.DeviceRegistrationEntity;

public class DeviceRegistrationEntry {

	private DeviceRegistrationEntity deviceRegistration;

	private String friendlyName;

	private List<AttributeDO> attribute;

	public DeviceRegistrationEntry(DeviceRegistrationEntity deviceRegistration,
			String friendlyName, List<AttributeDO> attribute) {
		this.deviceRegistration = deviceRegistration;
		this.friendlyName = friendlyName;
		this.attribute = attribute;
	}

	public DeviceRegistrationEntity getDeviceRegistration() {
		return this.deviceRegistration;
	}

	public void setDeviceRegistration(
			DeviceRegistrationEntity deviceRegistration) {
		this.deviceRegistration = deviceRegistration;
	}

	public String getFriendlyName() {
		return this.friendlyName;
	}

	public void setFriendlyName(String friendlyName) {
		this.friendlyName = friendlyName;
	}

	public List<AttributeDO> getAttribute() {
		return this.attribute;
	}

	public void setAttribute(List<AttributeDO> attribute) {
		this.attribute = attribute;
	}
}
