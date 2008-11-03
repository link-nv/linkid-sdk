/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.oper;

import javax.ejb.Local;


import net.link.safeonline.oper.OperatorConstants;

@Local
public interface Configuration {

    public static final String JNDI_BINDING = OperatorConstants.JNDI_PREFIX + "ConfigurationBean/local";

    /*
     * Factories.
     */
    void configGroupListFactory();

    /*
     * Lifecycle.
     */
    void destroyCallback();

    /*
     * Actions.
     */
    String save();

}
