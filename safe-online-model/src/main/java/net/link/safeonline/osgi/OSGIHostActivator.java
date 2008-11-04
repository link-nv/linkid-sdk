/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.osgi;

import java.io.Serializable;

import net.link.safeonline.osgi.plugin.OlasAttributeService;
import net.link.safeonline.osgi.plugin.PluginAttributeService;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;


/**
 * <h2>{@link OSGIHostActivator}<br>
 * <sub>[in short] (TODO).</sub></h2>
 * 
 * <p>
 * [description / usage].
 * </p>
 * 
 * <p>
 * <i>Aug 19, 2008</i>
 * </p>
 * 
 * @author wvdhaute
 */
public class OSGIHostActivator implements BundleActivator, Serializable {

    private static final long   serialVersionUID                 = 1L;

    public static final String  JNDI_BINDING                     = "SafeOnline/OSGI/HostActivator";

    private ServiceTracker      pluginAttributeServiceTracker    = null;

    private ServiceRegistration olasAttributeServiceRegistration = null;


    public void start(BundleContext context) {

        // Initialize a service tracker for the plugin attribute service
        this.pluginAttributeServiceTracker = new ServiceTracker(context, PluginAttributeService.class.getName(), null);
        this.pluginAttributeServiceTracker.open();

        // Initialize olas attribute service
        OlasAttributeServiceFactory serviceFactory = new OlasAttributeServiceFactory();
        this.olasAttributeServiceRegistration = context.registerService(OlasAttributeService.class.getName(), serviceFactory, null);

    }

    public void stop(BundleContext context) {

        this.olasAttributeServiceRegistration.unregister();

    }

    public Object[] getPluginServices() {

        return this.pluginAttributeServiceTracker.getServices();
    }
}
