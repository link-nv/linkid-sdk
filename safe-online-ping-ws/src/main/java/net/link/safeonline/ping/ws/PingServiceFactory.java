/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.ping.ws;

import java.net.URL;

import javax.xml.namespace.QName;

import net.lin_k.safe_online.ping.PingService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class PingServiceFactory {

    private static final Log LOG = LogFactory.getLog(PingServiceFactory.class);


    private PingServiceFactory() {

        // empty
    }

    /**
     * Gives back a new instance of a ping service JAX-WS stub.
     *
     */
    public static PingService newInstance() {

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL wsdlUrl = classLoader.getResource("safe-online-ping.wsdl");
        if (null == wsdlUrl)
            throw new RuntimeException("ping WSDL not found");
        LOG.debug("wsdl url: " + wsdlUrl);
        PingService service = new PingService(wsdlUrl, new QName("urn:net:lin-k:safe-online:ping", "PingService"));
        return service;
    }
}
