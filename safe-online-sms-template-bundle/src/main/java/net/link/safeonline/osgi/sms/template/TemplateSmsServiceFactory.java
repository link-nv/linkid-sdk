/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.osgi.sms.template;

import net.link.safeonline.osgi.sms.SmsService;

import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.log.LogService;


public class TemplateSmsServiceFactory implements ServiceFactory {

    private int usageCounter = 0;


    public Object getService(Bundle bundle, ServiceRegistration registration) {

        LogService LOG = TemplateSmsServiceActivator.LOG;

        LOG.log(LogService.LOG_DEBUG, "Create object of SmsService for " + bundle.getSymbolicName());
        usageCounter++;
        LOG.log(LogService.LOG_DEBUG, "Number of bundles using service " + usageCounter);
        SmsService templateSmsService = new TemplateSmsService();
        return templateSmsService;

    }

    public void ungetService(Bundle bundle, ServiceRegistration registration, Object service) {

        LogService LOG = TemplateSmsServiceActivator.LOG;
        LOG.log(LogService.LOG_DEBUG, "Release object of SmsService for " + bundle.getSymbolicName());
        usageCounter--;
        LOG.log(LogService.LOG_DEBUG, "Number of bundles using service " + usageCounter);

    }

}
