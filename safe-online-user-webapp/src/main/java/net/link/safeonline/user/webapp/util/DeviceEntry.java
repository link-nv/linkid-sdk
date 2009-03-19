/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.user.webapp.util;

import java.io.Serializable;

import net.link.safeonline.entity.DeviceEntity;


public class DeviceEntry implements Serializable {

    private static final long serialVersionUID = 1L;

    private DeviceEntity      device;

    private String            friendlyName;


    public DeviceEntry(DeviceEntity device, String friendlyName) {

        this.device = device;
        this.friendlyName = friendlyName;
    }

    public DeviceEntity getDevice() {

        return device;
    }

    public void setDevice(DeviceEntity device) {

        this.device = device;
    }

    public String getFriendlyName() {

        return friendlyName;
    }

    public void setFriendlyName(String friendlyName) {

        this.friendlyName = friendlyName;
    }
}
