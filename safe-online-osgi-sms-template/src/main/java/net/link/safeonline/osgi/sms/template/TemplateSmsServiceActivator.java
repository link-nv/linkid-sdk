/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.osgi.sms.template;

import net.link.safeonline.osgi.sms.SmsService;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;


public class TemplateSmsServiceActivator implements BundleActivator {

    ServiceRegistration templateSmsServiceRegistration;


    /*
     * (non-Javadoc)
     * 
     * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
     */
    public void start(BundleContext context)
            throws Exception {

        TemplateSmsServiceFactory smsServiceFactory = new TemplateSmsServiceFactory();
        this.templateSmsServiceRegistration = context.registerService(SmsService.class.getName(), smsServiceFactory, null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    public void stop(BundleContext context)
            throws Exception {

        this.templateSmsServiceRegistration.unregister();
    }
}
