/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.otpoversms.ws;

import java.net.URL;

import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import sis.mobile.SmsService;

public class SmsServiceFactory {

    private static final Log LOG = LogFactory.getLog(SmsServiceFactory.class);


    private SmsServiceFactory() {

        // empty
    }


    /**
     * Gives back a new instance of a ping service JAX-WS stub.
     * 
     */
    public static SmsService newInstance() {

        ClassLoader classLoader = Thread.currentThread()
                .getContextClassLoader();
        URL wsdlUrl = classLoader.getResource("safe-online-sms.wsdl");
        if (null == wsdlUrl)
            throw new RuntimeException("sms WSDL not found");
        LOG.debug("wsdl url: " + wsdlUrl);
        SmsService service = new SmsService(wsdlUrl, new QName(
                "urn:sis:mobile", "SmsService"));
        return service;
    }
}
