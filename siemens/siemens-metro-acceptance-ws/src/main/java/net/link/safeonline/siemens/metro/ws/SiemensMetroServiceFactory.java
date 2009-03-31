/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.siemens.metro.ws;

import java.net.URL;

import javax.xml.namespace.QName;

import net.lin_k.siemens.metro.MetroService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SiemensMetroServiceFactory {

    private static final Log LOG = LogFactory
                                         .getLog(SiemensMetroServiceFactory.class);


    private SiemensMetroServiceFactory() {

        // empty
    }


    /**
     * Gives back a new instance of a ping service JAX-WS stub.
     * 
     */
    public static MetroService newInstance() {

        ClassLoader classLoader = Thread.currentThread()
                .getContextClassLoader();
        URL wsdlUrl = classLoader.getResource("siemens-metro-service.wsdl");
        if (null == wsdlUrl)
            throw new RuntimeException("siemens-metro-service WSDL not found");
        LOG.debug("wsdl url: " + wsdlUrl);
        MetroService service = new MetroService(wsdlUrl, new QName(
                "urn:net:lin-k:siemens:metro", "MetroService"));
        return service;
    }
}
