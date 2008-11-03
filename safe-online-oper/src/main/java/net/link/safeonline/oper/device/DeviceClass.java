/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.oper.device;

import javax.ejb.Local;

import net.link.safeonline.oper.OperatorConstants;
import net.link.safeonline.authentication.exception.DeviceClassNotFoundException;


@Local
public interface DeviceClass {

    public static final String JNDI_BINDING = OperatorConstants.JNDI_PREFIX + "DeviceClassBean/local";

    /*
     * Actions
     */
    String view();

    String add();

    String edit();

    String save() throws DeviceClassNotFoundException;

    String remove();

    /*
     * Accessors
     */
    String getName();

    void setName(String name);

    String getAuthenticationContextClass();

    void setAuthenticationContextClass(String authenticationContextClass);

    /*
     * Factories
     */
    void deviceClassListFactory();

    /*
     * Lifecycle.
     */
    void destroyCallback();

}
