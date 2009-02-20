/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.sms.clickatell.osgi;

import net.link.safeonline.osgi.sms.SmsService;

import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceRegistration;


public class ClickatellSmsServiceFactory implements ServiceFactory {

    private int usageCounter = 0;


    public Object getService(Bundle bundle, ServiceRegistration registration) {

        System.out.println("Create object of SmsService for " + bundle.getSymbolicName());
        usageCounter++;
        System.out.println("Number of bundles using service " + usageCounter);
        SmsService templateSmsService = new ClickatellSmsService();
        return templateSmsService;
    }

    public void ungetService(Bundle bundle, ServiceRegistration registration, Object service) {

        System.out.println("Release object of SmsService for " + bundle.getSymbolicName());
        usageCounter--;
        System.out.println("Number of bundles using service " + usageCounter);
    }

}
