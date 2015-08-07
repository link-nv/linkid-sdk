/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.ws.auth;

import java.net.URL;
import javax.xml.namespace.QName;
import net.lin_k.safe_online.auth._2.AuthService;


public class LinkIDAuthServiceFactory {

    private LinkIDAuthServiceFactory() {

        // empty
    }

    public static AuthService newInstance() {

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL wsdlUrl = classLoader.getResource( "safe-online-auth-2.0.wsdl" );
        if (null == wsdlUrl)
            throw new RuntimeException( "SafeOnline Auth WSDL not found" );

        return new AuthService( wsdlUrl, new QName( "urn:net:lin-k:safe-online:auth:2.0", "AuthService" ) );
    }
}
