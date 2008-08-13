/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.model.bean;

import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import net.link.safeonline.dao.AllowedDeviceDAO;
import net.link.safeonline.dao.DeviceDAO;
import net.link.safeonline.entity.AllowedDeviceEntity;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.DeviceEntity;
import net.link.safeonline.model.Devices;


@Stateless
public class DevicesBean implements Devices {

    @EJB
    private DeviceDAO        deviceDAO;

    @EJB
    private AllowedDeviceDAO allowedDeviceDAO;


    public List<AllowedDeviceEntity> listAllowedDevices(ApplicationEntity application) {

        return this.allowedDeviceDAO.listAllowedDevices(application);
    }

    public List<DeviceEntity> listDevices() {

        return this.deviceDAO.listDevices();
    }

    public void setAllowedDevices(ApplicationEntity application, List<AllowedDeviceEntity> allowedDevices) {

        this.allowedDeviceDAO.deleteAllowedDevices(application);
        if (allowedDevices != null) {
            for (AllowedDeviceEntity allowedDevice : allowedDevices) {
                this.allowedDeviceDAO.addAllowedDevice(application, allowedDevice.getDevice(), allowedDevice
                        .getWeight());
            }
        }
    }

}
