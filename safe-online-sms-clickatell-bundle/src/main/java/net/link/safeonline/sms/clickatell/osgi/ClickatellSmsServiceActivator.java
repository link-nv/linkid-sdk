/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.sms.clickatell.osgi;

import net.link.safeonline.osgi.sms.SmsService;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;


public class ClickatellSmsServiceActivator implements BundleActivator {

    ServiceRegistration clickatellSmsServiceRegistration;


    /*
     * (non-Javadoc)
     * 
     * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
     */
    public void start(BundleContext context)
            throws Exception {

        ClickatellSmsServiceFactory smsServiceFactory = new ClickatellSmsServiceFactory();
        clickatellSmsServiceRegistration = context.registerService(SmsService.class.getName(), smsServiceFactory, null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    public void stop(BundleContext context)
            throws Exception {

        clickatellSmsServiceRegistration.unregister();
    }
}
