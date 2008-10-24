/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.authentication.service.bean;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.exception.DeviceNotFoundException;
import net.link.safeonline.authentication.exception.EmptyDevicePolicyException;
import net.link.safeonline.authentication.service.DevicePolicyService;
import net.link.safeonline.dao.ApplicationDAO;
import net.link.safeonline.dao.DeviceDAO;
import net.link.safeonline.entity.AllowedDeviceEntity;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.DeviceDescriptionEntity;
import net.link.safeonline.entity.DeviceDescriptionPK;
import net.link.safeonline.entity.DeviceEntity;
import net.link.safeonline.model.Devices;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


@Stateless
public class DevicePolicyServiceBean implements DevicePolicyService {

    private static final Log LOG = LogFactory.getLog(DevicePolicyServiceBean.class);

    @EJB
    private Devices          devices;

    @EJB
    private ApplicationDAO   applicationDAO;

    @EJB
    private DeviceDAO        deviceDAO;


    public List<DeviceEntity> getDevicePolicy(String applicationId, Set<DeviceEntity> requiredDevicePolicy)
                                                                                                           throws ApplicationNotFoundException,
                                                                                                           EmptyDevicePolicyException {

        LOG.debug("get device policy for application: " + applicationId);
        ApplicationEntity application = this.applicationDAO.getApplication(applicationId);
        boolean deviceRestriction = application.isDeviceRestriction();
        List<DeviceEntity> devicePolicy = new LinkedList<DeviceEntity>();
        if (deviceRestriction) {
            /*
             * In this case we use the explicit allowed device list.
             */
            List<AllowedDeviceEntity> allowedDevices = this.devices.listAllowedDevices(application);
            for (AllowedDeviceEntity allowedDevice : allowedDevices) {
                devicePolicy.add(allowedDevice.getDevice());
            }
        } else {
            devicePolicy = this.devices.listDevices();
        }
        if (null != requiredDevicePolicy) {
            devicePolicy.retainAll(requiredDevicePolicy);
        }
        if (true == devicePolicy.isEmpty())
            throw new EmptyDevicePolicyException();
        return devicePolicy;
    }

    public List<DeviceEntity> getDevices() {

        LOG.debug("get devices");
        return this.devices.listDevices();
    }

    public String getDeviceDescription(String deviceName, Locale locale) {

        if (null == locale)
            return deviceName;
        DeviceDescriptionEntity deviceDescription = this.deviceDAO
                                                                  .findDescription(new DeviceDescriptionPK(deviceName, locale.getLanguage()));
        if (null == deviceDescription)
            return deviceName;
        return deviceDescription.getDescription();
    }

    public String getAuthenticationURL(String deviceName) throws DeviceNotFoundException {

        DeviceEntity device = this.deviceDAO.getDevice(deviceName);
        return device.getAuthenticationURL();
    }

    public String getRegistrationURL(String deviceName) throws DeviceNotFoundException {

        DeviceEntity device = this.deviceDAO.getDevice(deviceName);
        return device.getRegistrationURL();
    }

    public String getRemovalURL(String deviceName) throws DeviceNotFoundException {

        DeviceEntity device = this.deviceDAO.getDevice(deviceName);
        return device.getRemovalURL();
    }

    public String getUpdateURL(String deviceName) throws DeviceNotFoundException {

        DeviceEntity device = this.deviceDAO.getDevice(deviceName);
        return device.getUpdateURL();
    }

    public String getDisableURL(String deviceName) throws DeviceNotFoundException {

        DeviceEntity device = this.deviceDAO.getDevice(deviceName);
        return device.getDisableURL();
    }

    public List<DeviceEntity> listDevices(String authenticationContextClass) {

        List<DeviceEntity> authndevices = new LinkedList<DeviceEntity>();
        List<DeviceEntity> allDevices = this.deviceDAO.listDevices();
        for (DeviceEntity device : allDevices) {
            if (device.getAuthenticationContextClass().equals(authenticationContextClass)) {
                authndevices.add(device);
            }
        }
        if (authndevices.size() != 0)
            return authndevices;

        // if none found return all devices with
        // deviceClass.authenticationContextClass = authenticationContextClass
        return this.deviceDAO.listDevices(authenticationContextClass);
    }

    public DeviceEntity getDevice(String deviceName) throws DeviceNotFoundException {

        return this.deviceDAO.getDevice(deviceName);
    }
}
