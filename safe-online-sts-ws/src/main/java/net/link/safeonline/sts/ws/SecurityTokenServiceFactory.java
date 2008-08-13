/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sts.ws;

import java.net.URL;

import javax.xml.namespace.QName;

import org.oasis_open.docs.ws_sx.ws_trust._200512.SecurityTokenService;


public class SecurityTokenServiceFactory {

    private SecurityTokenServiceFactory() {

        // empty
    }

    public static SecurityTokenService newInstance() {

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL wsdlUrl = classLoader.getResource("ws-trust-1.3.wsdl");
        if (null == wsdlUrl) {
            throw new RuntimeException("SAML protocol WSDL not found");
        }
        SecurityTokenService service = new SecurityTokenService(wsdlUrl, new QName(
                "http://docs.oasis-open.org/ws-sx/ws-trust/200512/", "SecurityTokenService"));
        return service;
    }
}
