/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.auth;

import java.io.IOException;
import java.util.List;

import javax.ejb.Local;
import javax.faces.model.SelectItem;

import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.exception.DeviceNotFoundException;
import net.link.safeonline.authentication.exception.EmptyDevicePolicyException;


@Local
public interface DeviceRegistration {

    public static final String JNDI_BINDING = AuthenticationConstants.JNDI_PREFIX + "DeviceRegistrationBean/local";


    /*
     * Accessors.
     */
    String getDevice();

    void setDevice(String device);

    String getUsername();

    /*
     * Actions.
     */
    String deviceNext()
            throws IOException, DeviceNotFoundException;

    /*
     * Factories
     */
    List<SelectItem> applicationDevicesFactory()
            throws ApplicationNotFoundException, EmptyDevicePolicyException;

    /*
     * Lifecycle.
     */
    void destroyCallback();
}
