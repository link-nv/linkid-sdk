/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.siemens.osgi.sms;

import java.net.ConnectException;

import net.link.safeonline.osgi.OlasConfigurationService;
import net.link.safeonline.osgi.sms.SmsService;
import net.link.safeonline.sdk.ws.otpoversms.SmsClient;
import net.link.safeonline.sdk.ws.otpoversms.SmsClientImpl;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;


/**
 * <h2>{@link SiemensSmsService}<br>
 * <sub>Siemens SMS service OSGi bundle</sub></h2>
 * 
 * <p>
 * This service sends out an SMS using the Siemens SMS WSDL.
 * </p>
 * 
 * <p>
 * <i>Dec 8, 2008</i>
 * </p>
 * 
 * @author wvdhaute
 */
public class SiemensSmsService implements SmsService {

    private BundleContext bundleContext;

    private String        groupName = "Siemens SMS Service";
    private String        itemName  = "Location";
    private String        location  = "http://localhost:8080/safe-online-sms-ws/dummy";


    public SiemensSmsService(BundleContext bundleContext) {

        System.out.println("SiemensSmsService constructor");

        this.bundleContext = bundleContext;

        ServiceReference serviceReference = this.bundleContext.getServiceReference(OlasConfigurationService.class.getName());
        if (null != serviceReference) {
            System.out.println("OLAS Configuration service found");
            OlasConfigurationService configurationService = (OlasConfigurationService) this.bundleContext.getService(serviceReference);
            configurationService.initConfigurationValue(groupName, itemName, location);
            this.bundleContext.ungetService(serviceReference);
        }

    }

    /**
     * {@inheritDoc}
     */
    public void sendSms(String mobile, String message)
            throws ConnectException {

        System.out.println("send sms to " + mobile + " with messag:" + message);

        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        try {
            // Have to set the thread's context classloader to the bundle's classloader, jaxws loads in a resource for the Provider class
            // using Thread.currentThread.getContextClassLoader which returns the classloader of JBoss.
            Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
            SmsClient smsClient = new SmsClientImpl(getLocation());
            smsClient.sendSms(mobile, message);
        } catch (Exception e) {
            // empty
            System.out.println("ws: exception: " + e.getMessage());
            e.printStackTrace();
        } finally {
            Thread.currentThread().setContextClassLoader(cl);
        }
    }

    private String getLocation() {

        ServiceReference serviceReference = bundleContext.getServiceReference(OlasConfigurationService.class.getName());
        if (null != serviceReference) {
            OlasConfigurationService configurationService = (OlasConfigurationService) bundleContext.getService(serviceReference);
            String value = (String) configurationService.getConfigurationValue(groupName, itemName, location);
            bundleContext.ungetService(serviceReference);
            return value;
        }

        return null;

    }
}
