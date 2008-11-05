/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.authentication.service;

import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.ejb.Local;

import net.link.safeonline.SafeOnlineService;
import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.exception.DeviceNotFoundException;
import net.link.safeonline.authentication.exception.EmptyDevicePolicyException;
import net.link.safeonline.entity.DeviceEntity;


/**
 * Interface for device policy service bean.
 * 
 * @author fcorneli
 */
@Local
public interface DevicePolicyService extends SafeOnlineService {

    public static final String JNDI_BINDING = SafeOnlineService.JNDI_PREFIX + "DevicePolicyServiceBean/local";


    /**
     * Gives back the device policy for the given application. The device policy is a set of device entities which the application accepts
     * as authentication devices. The method signature has been optimized for ease of use in the authentication web application.
     * 
     * @param applicationId
     * @param requiredDevicePolicy
     *            the optional required device policy as communicated by the application via the authentication protocol.
     * @throws ApplicationNotFoundException
     * @throws EmptyDevicePolicyException
     */
    List<DeviceEntity> getDevicePolicy(String applicationId, Set<DeviceEntity> requiredDevicePolicy)
            throws ApplicationNotFoundException, EmptyDevicePolicyException;

    /**
     * Returns all devices
     * 
     */
    List<DeviceEntity> getDevices();

    /**
     * Gives back device description according to the specified locale.
     * 
     * @param deviceName
     * @param locale
     */
    String getDeviceDescription(String deviceName, Locale locale);

    /**
     * Returns the authentication URL for the specified device.
     * 
     * @param deviceName
     * @throws DeviceNotFoundException
     */
    String getAuthenticationURL(String deviceName)
            throws DeviceNotFoundException;

    /**
     * Returns the registration URL for the specified device.
     * 
     * @param deviceName
     * @throws DeviceNotFoundException
     */
    String getRegistrationURL(String deviceName)
            throws DeviceNotFoundException;

    /**
     * Returns the removal URL for the specified device.
     * 
     * @param deviceName
     * @throws DeviceNotFoundException
     */
    String getRemovalURL(String deviceName)
            throws DeviceNotFoundException;

    /**
     * Returns the update URL for the specified device.
     * 
     * @param deviceName
     * @throws DeviceNotFoundException
     */
    String getUpdateURL(String deviceName)
            throws DeviceNotFoundException;

    /**
     * Returns the disable URL for the specified device.
     * 
     * @param deviceName
     * @throws DeviceNotFoundException
     */
    String getDisableURL(String deviceName)
            throws DeviceNotFoundException;

    /**
     * Returns list of devices matching the specified authentication context class.
     * 
     * @param authnContextClassRefValue
     */
    List<DeviceEntity> listDevices(String authenticationContextClass);

    /**
     * @param deviceName
     * @throws DeviceNotFoundException
     */
    DeviceEntity getDevice(String deviceName)
            throws DeviceNotFoundException;

}
