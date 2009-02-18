/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.osgi.plugin.template;

import net.link.safeonline.osgi.plugin.PluginAttributeService;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;


public class TemplateAttributeServiceActivator implements BundleActivator {

    ServiceRegistration templateAttributeServiceRegistration;


    /*
     * (non-Javadoc)
     * 
     * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
     */
    public void start(BundleContext context)
            throws Exception {

        TemplateAttributeServiceFactory helloServiceFactory = new TemplateAttributeServiceFactory();
        templateAttributeServiceRegistration = context.registerService(PluginAttributeService.class.getName(), helloServiceFactory,
                null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    public void stop(BundleContext context)
            throws Exception {

        templateAttributeServiceRegistration.unregister();
    }
}
