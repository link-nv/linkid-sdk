/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.sms.clickatell.osgi;

import java.net.ConnectException;

import net.link.safeonline.osgi.sms.SmsService;


/**
 * <sub>Clickatell sms service.</sub></h2>
 * 
 * <p>
 * This class calls the clickatell service through their SOAP interface
 * </p>
 * 
 * <p>
 * <i>Feb 18, 2009</i>
 * </p>
 * 
 * @author dhouthoo
 */
public class ClickatellSmsService implements SmsService {

    public ClickatellSmsService() {

    }

    /**
     * {@inheritDoc}
     */
    public void sendSms(String mobile, String message)
            throws ConnectException {

        System.out.println("send sms to mobile " + mobile + " with message " + message);

    }

}
