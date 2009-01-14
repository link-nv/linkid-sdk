/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.device.auth.ws;

import java.net.URL;

import javax.xml.namespace.QName;

import net.lin_k.safe_online.auth.DeviceAuthenticationService;

public class DeviceAuthenticationServiceFactory {

    private DeviceAuthenticationServiceFactory() {

        // empty
    }


    public static DeviceAuthenticationService newInstance() {

        ClassLoader classLoader = Thread.currentThread()
                .getContextClassLoader();
        URL wsdlUrl = classLoader.getResource("safe-online-device-auth.wsdl");
        if (null == wsdlUrl)
            throw new RuntimeException(
                    "SafeOnline Device Authentication WSDL not found");

        DeviceAuthenticationService service = new DeviceAuthenticationService(
                wsdlUrl, new QName("urn:net:lin-k:safe-online:auth",
                        "DeviceAuthenticationService"));

        return service;
    }
}
