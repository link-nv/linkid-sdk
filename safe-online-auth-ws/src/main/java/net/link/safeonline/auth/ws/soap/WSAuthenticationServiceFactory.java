/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.auth.ws.soap;

import java.net.URL;
import javax.xml.namespace.QName;
import net.lin_k.safe_online.auth.WSAuthenticationService;


public class WSAuthenticationServiceFactory {

    private WSAuthenticationServiceFactory() {

        // empty
    }

    public static WSAuthenticationService newInstance() {

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL wsdlUrl = classLoader.getResource( "safe-online-auth.wsdl" );
        if (null == wsdlUrl)
            throw new RuntimeException( "SafeOnline Authentication WSDL not found" );

        WSAuthenticationService service = new WSAuthenticationService( wsdlUrl, new QName( "urn:net:lin-k:safe-online:auth",
                "WSAuthenticationService" ) );

        return service;
    }
}
