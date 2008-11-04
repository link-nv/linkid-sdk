/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.config.model;

import javax.ejb.Local;

import net.link.safeonline.Startable;


@Local
public interface ConfigStartable extends Startable {
    public static final String JNDI_BINDING = Startable.JNDI_PREFIX + "ConfigStartableBean";

}
