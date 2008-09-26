/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.user;

import net.link.safeonline.entity.DeviceEntity;


public class DeviceEntry {

    private DeviceEntity device;

    private String       friendlyName;

    boolean              registrable = true;


    public DeviceEntry(DeviceEntity device, String friendlyName) {

        this.device = device;
        this.friendlyName = friendlyName;
    }

    public DeviceEntry(DeviceEntity device, String friendlyName, boolean registrable) {

        this.device = device;
        this.friendlyName = friendlyName;
        this.registrable = registrable;
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

    public boolean isRegistrable() {

        return null != this.device.getRegistrationPath() && this.registrable;
    }

    public boolean isUpdatable() {

        return null != this.device.getUpdatePath();
    }

    public boolean isRemovable() {

        return null != this.device.getRemovalPath();
    }
}
