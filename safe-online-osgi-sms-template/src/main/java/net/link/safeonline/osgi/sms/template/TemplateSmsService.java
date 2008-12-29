/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.osgi.sms.template;

import java.net.ConnectException;

import net.link.safeonline.osgi.sms.SmsService;

import org.osgi.framework.BundleContext;


/**
 * <sub>Template sms service.</sub></h2>
 * 
 * <p>
 * The template sms service implementation serves as an example to develop new sms service bundles.
 * </p>
 * 
 * <p>
 * <i>Dec 9, 2008</i>
 * </p>
 * 
 * @author wvdhaute
 */
public class TemplateSmsService implements SmsService {

    private BundleContext bundleContext;


    public TemplateSmsService(BundleContext bundleContext) {

        this.bundleContext = bundleContext;
    }

    /**
     * {@inheritDoc}
     */
    public void sendSms(String mobile, String message)
            throws ConnectException {

        System.out.println("send sms to mobile " + mobile + " with message " + message);

    }

}