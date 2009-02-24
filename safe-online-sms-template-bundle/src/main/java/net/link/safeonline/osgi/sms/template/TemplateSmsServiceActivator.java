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
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.log.LogService;


public class TemplateSmsServiceActivator implements BundleActivator {

    ServiceRegistration     templateSmsServiceRegistration;

    static LogService       LOG;
    static ServiceReference serviceReference;


    /*
     * (non-Javadoc)
     * 
     * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
     */
    public void start(BundleContext context)
            throws Exception {

        serviceReference = context.getServiceReference(LogService.class.getName());
        LOG = (LogService) context.getService(serviceReference);
        TemplateSmsServiceFactory smsServiceFactory = new TemplateSmsServiceFactory();
        templateSmsServiceRegistration = context.registerService(SmsService.class.getName(), smsServiceFactory, null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    public void stop(BundleContext context)
            throws Exception {

        context.ungetService(serviceReference);
        templateSmsServiceRegistration.unregister();
    }
}
