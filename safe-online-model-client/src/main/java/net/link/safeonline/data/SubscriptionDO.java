/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.data;

import java.io.Serializable;
import java.util.List;

import net.link.safeonline.entity.DeviceEntity;
import net.link.safeonline.entity.SubscriptionEntity;


public class SubscriptionDO implements Serializable {

    private static final long  serialVersionUID = 1L;

    private SubscriptionEntity subscription;

    private List<DeviceEntity> allowedDevices;


    public SubscriptionDO(SubscriptionEntity subscription, List<DeviceEntity> allowedDevices) {

        this.subscription = subscription;
        this.allowedDevices = allowedDevices;
    }

    public SubscriptionEntity getSubscription() {

        return subscription;
    }

    public List<DeviceEntity> getAllowedDevices() {

        return allowedDevices;
    }

    public String getDeviceRestrictionList() {

        if (!subscription.getApplication().isDeviceRestriction())
            return null;
        String deviceList = "";
        for (DeviceEntity allowedDevice : allowedDevices) {
            deviceList += allowedDevice.getName() + " ";
        }
        return deviceList;
    }

    public String getApplicationName() {

        if (null != subscription.getApplication().getFriendlyName())
            return subscription.getApplication().getFriendlyName();
        return subscription.getApplication().getName();

    }
}
