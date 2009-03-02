/*
 * SafeOnline project.
 * 
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.auth.webapp;

import java.io.Serializable;

import net.link.safeonline.entity.DeviceEntity;


/**
 * <h2>{@link DeviceDO}<br>
 * <sub>[in short] (TODO).</sub></h2>
 * 
 * <p>
 * [description / usage].
 * </p>
 * 
 * <p>
 * <i>Feb 24, 2009</i>
 * </p>
 * 
 * @author wvdhaute
 */
public class DeviceDO implements Serializable {

    private static final long serialVersionUID = 1L;

    private DeviceEntity      device;

    private String            friendlyName;


    public DeviceDO(DeviceEntity device, String friendlyName) {

        this.device = device;
        this.friendlyName = friendlyName;
    }

    public DeviceEntity getDevice() {

        return device;
    }

    public String getFriendlyName() {

        return friendlyName;
    }

}
