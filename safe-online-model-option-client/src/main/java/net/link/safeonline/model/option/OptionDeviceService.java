/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.model.option;

import javax.ejb.Local;

import net.link.safeonline.authentication.exception.DeviceDisabledException;
import net.link.safeonline.authentication.exception.DeviceRegistrationNotFoundException;
import net.link.safeonline.authentication.exception.NodeNotFoundException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;


@Local
public interface OptionDeviceService extends OptionService {

    public static final String JNDI_BINDING = OptionService.JNDI_PREFIX + "OptionDeviceServiceBean/local";


    public void register(String nodeName, String userId, String imei)
            throws NodeNotFoundException;

    public void remove(String imei)
            throws SubjectNotFoundException, DeviceRegistrationNotFoundException;

    /**
     * @return The User ID of the user who just successfully authenticated. <code>null</code> when authentication failed.
     */
    public String authenticate(String imei)
            throws SubjectNotFoundException, DeviceRegistrationNotFoundException, DeviceDisabledException;

    public void enable(String userId, String imei)
            throws SubjectNotFoundException, DeviceRegistrationNotFoundException;

    public void disable(String userId, String imei)
            throws SubjectNotFoundException, DeviceRegistrationNotFoundException;
}
