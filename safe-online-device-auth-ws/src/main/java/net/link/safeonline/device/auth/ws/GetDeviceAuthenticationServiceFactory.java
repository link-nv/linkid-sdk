/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.device.auth.ws;

import java.net.URL;

import javax.xml.namespace.QName;

import net.lin_k.safe_online.auth.GetDeviceAuthenticationService;

public class GetDeviceAuthenticationServiceFactory {

    private GetDeviceAuthenticationServiceFactory() {

        // empty
    }


    public static GetDeviceAuthenticationService newInstance() {

        ClassLoader classLoader = Thread.currentThread()
                .getContextClassLoader();
        URL wsdlUrl = classLoader.getResource("safe-online-device-auth.wsdl");
        if (null == wsdlUrl)
            throw new RuntimeException(
                    "SafeOnline Device Authentication WSDL not found");

        GetDeviceAuthenticationService service = new GetDeviceAuthenticationService(
                wsdlUrl, new QName("urn:net:lin-k:safe-online:auth",
                        "GetDeviceAuthenticationService"));

        return service;
    }
}
