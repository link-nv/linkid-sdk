/*
 * SafeOnline project.
 * 
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.authentication.service;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.link.safeonline.entity.DeviceEntity;
import net.link.safeonline.entity.SubjectEntity;

import org.joda.time.DateTime;


/**
 * <h2>{@link AuthenticationAssertion}<br>
 * <sub>[in short] (TODO).</sub></h2>
 * 
 * <p>
 * [description / usage].
 * </p>
 * 
 * <p>
 * <i>Mar 25, 2009</i>
 * </p>
 * 
 * @author wvdhaute
 */
public class AuthenticationAssertion {

    private SubjectEntity               subject;

    private Map<DateTime, DeviceEntity> authentications;


    public AuthenticationAssertion(SubjectEntity subject) {

        this.subject = subject;
        authentications = new HashMap<DateTime, DeviceEntity>();
    }

    public SubjectEntity getSubject() {

        return subject;
    }

    public void addAuthentication(DateTime authenticationTime, DeviceEntity authenticatedDevice) {

        authentications.put(authenticationTime, authenticatedDevice);
    }

    public Map<DateTime, DeviceEntity> getAuthentications() {

        return authentications;
    }

    public Collection<DeviceEntity> getDevices() {

        return authentications.values();
    }

    public List<String> getDevicesList() {

        List<String> deviceList = new LinkedList<String>();
        for (DeviceEntity device : authentications.values()) {
            deviceList.add(device.getName());
        }
        return deviceList;
    }

    public String getDevicesString() {

        String devicesString = "";
        for (DeviceEntity device : authentications.values()) {
            devicesString += device.getName() + " ";
        }
        return devicesString;
    }
}
