package net.link.safeonline.oper.app;

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

    public void setFriendlyName(String friendlyName) {

        this.friendlyName = friendlyName;
    }
}
