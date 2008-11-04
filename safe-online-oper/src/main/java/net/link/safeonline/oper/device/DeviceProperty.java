/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.oper.device;

import javax.ejb.Local;

import net.link.safeonline.oper.OperatorConstants;
import net.link.safeonline.authentication.exception.DeviceNotFoundException;
import net.link.safeonline.authentication.exception.DevicePropertyNotFoundException;
import net.link.safeonline.authentication.exception.ExistingDevicePropertyException;


@Local
public interface DeviceProperty {

    public static final String JNDI_BINDING = OperatorConstants.JNDI_PREFIX + "DevicePropertyBean/local";


    /*
     * Lifecycle.
     */
    void destroyCallback();

    /*
     * Factories
     */
    void devicePropertiesListFactory()
            throws DeviceNotFoundException;

    /*
     * Actions.
     */
    String add()
            throws ExistingDevicePropertyException, DeviceNotFoundException;

    String edit();

    String save();

    String remove()
            throws DevicePropertyNotFoundException, DeviceNotFoundException;

    String cancelEdit();

    /*
     * Acccessors
     */
    String getName();

    void setName(String name);

    String getValue();

    void setValue(String value);
}
