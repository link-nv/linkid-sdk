package net.link.safeonline.owner;

import net.link.safeonline.entity.DeviceEntity;


public class DeviceEntry {

    private DeviceEntity device;

    private String       friendlyName;

    private boolean      allowed;

    private int          weight;


    public DeviceEntry(DeviceEntity device, String friendlyName, boolean allowed, int weight) {

        this.device = device;
        this.friendlyName = friendlyName;
        this.allowed = allowed;
        this.weight = weight;
    }

    public boolean isAllowed() {

        return allowed;
    }

    public void setAllowed(boolean allowed) {

        this.allowed = allowed;
    }

    public DeviceEntity getDevice() {

        return device;
    }

    public void setDevice(DeviceEntity device) {

        this.device = device;
    }

    public int getWeight() {

        return weight;
    }

    public void setWeight(int weight) {

        this.weight = weight;
    }

    public String getFriendlyName() {

        return friendlyName;
    }

    public void setFriendlyName(String friendlyName) {

        this.friendlyName = friendlyName;
    }
}
