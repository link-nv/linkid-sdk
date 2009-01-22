/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.siemens.acceptance.ws.auth;

import java.net.URL;

import javax.xml.namespace.QName;

import net.lin_k.siemens.metro.SiemensWSAuthenticationService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SiemensWSAuthenticationServiceFactory {

    private static final Log LOG = LogFactory
                                         .getLog(SiemensWSAuthenticationServiceFactory.class);


    private SiemensWSAuthenticationServiceFactory() {

        // empty
    }


    /**
     * Gives back a new instance of a ping service JAX-WS stub.
     * 
     */
    public static SiemensWSAuthenticationService newInstance() {

        ClassLoader classLoader = Thread.currentThread()
                .getContextClassLoader();
        URL wsdlUrl = classLoader.getResource("siemens-auth-ws-service.wsdl");
        if (null == wsdlUrl)
            throw new RuntimeException("siemens-metro-service WSDL not found");
        LOG.debug("wsdl url: " + wsdlUrl);
        SiemensWSAuthenticationService service = new SiemensWSAuthenticationService(
                wsdlUrl, new QName("urn:net:lin-k:siemens:metro",
                        "SiemensWSAuthenticationService"));
        return service;
    }
}
