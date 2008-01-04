package net.link.safeonline.owner;

import net.link.safeonline.entity.DeviceEntity;

import org.jboss.seam.core.ResourceBundle;

public class DeviceEntry {

	private DeviceEntity device;

	private String friendlyName;

	private boolean allowed;

	private int weight;

	public DeviceEntry(DeviceEntity device, boolean allowed, int weight) {
		this.device = device;
		this.allowed = allowed;
		this.weight = weight;
		setFriendlyName();
	}

	public boolean isAllowed() {
		return this.allowed;
	}

	public void setAllowed(boolean allowed) {
		this.allowed = allowed;
	}

	public DeviceEntity getDevice() {
		return this.device;
	}

	public void setDevice(DeviceEntity device) {
		this.device = device;
	}

	public int getWeight() {
		return this.weight;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}

	public String getFriendlyName() {
		return this.friendlyName;
	}

	private void setFriendlyName() {
		this.friendlyName = ResourceBundle.instance().getString(
				this.device.getName());
		if (null == this.friendlyName) {
			this.friendlyName = this.device.getName();
		}
	}

}
