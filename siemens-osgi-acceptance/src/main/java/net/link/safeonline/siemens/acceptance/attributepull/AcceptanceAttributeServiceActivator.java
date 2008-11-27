/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.siemens.acceptance.attributepull;

import net.link.safeonline.osgi.plugin.PluginAttributeService;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;


public class AcceptanceAttributeServiceActivator implements BundleActivator {

    ServiceRegistration acceptanceAttributeServiceRegistration;


    /*
     * (non-Javadoc)
     * 
     * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
     */
    public void start(BundleContext context)
            throws Exception {

        System.out.println("registering acceptance attribute service");
        AcceptanceAttributeServiceFactory acceptanceAttributeServiceFactory = new AcceptanceAttributeServiceFactory();
        this.acceptanceAttributeServiceRegistration = context.registerService(PluginAttributeService.class.getName(),
                acceptanceAttributeServiceFactory, null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    public void stop(BundleContext context)
            throws Exception {

        System.out.println("unregistering acceptance attribute service");
        this.acceptanceAttributeServiceRegistration.unregister();
    }
}
