/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.siemens.auth.ws.acceptance.jaxws.ws;

import java.net.URL;

import javax.xml.namespace.QName;

import net.lin_k.siemens.jaxws.SiemensAuthWsAcceptanceService;

public class SiemensAuthWsAcceptanceServiceFactory {

    private SiemensAuthWsAcceptanceServiceFactory() {

        // empty
    }


    public static SiemensAuthWsAcceptanceService newInstance() {

        ClassLoader classLoader = Thread.currentThread()
                .getContextClassLoader();
        URL wsdlUrl = classLoader.getResource("siemens-auth-ws.wsdl");
        if (null == wsdlUrl)
            throw new RuntimeException("SAML protocol WSDL not found");

        SiemensAuthWsAcceptanceService service = new SiemensAuthWsAcceptanceService(
                wsdlUrl, new QName("urn:net:lin-k:siemens:jaxws",
                        "SiemensAuthWsAcceptanceService"));

        return service;
    }
}
