/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.osgi;

import javax.ejb.Local;

import net.link.safeonline.Startable;
import net.link.safeonline.authentication.exception.SafeOnlineResourceException;
import net.link.safeonline.osgi.plugin.PluginAttributeService;


/**
 * <h2>{@link OSGIStartable}<br>
 * <sub>[in short] (TODO).</sub></h2>
 * 
 * <p>
 * [description / usage].
 * </p>
 * 
 * <p>
 * <i>Aug 18, 2008</i>
 * </p>
 * 
 * @author wvdhaute
 */
@Local
public interface OSGIStartable extends Startable {

    public static final String JNDI_BINDING = Startable.JNDI_PREFIX + "OSGIStartableBean";


    Object[] getPluginServices();

    PluginAttributeService getPluginService(String serviceName)
            throws SafeOnlineResourceException;
}
