/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.ws.ltqr;

import java.net.URL;
import javax.xml.namespace.QName;
import net.lin_k.safe_online.ltqr.LTQRService;


public class LTQRServiceFactory {

    private LTQRServiceFactory() {

        // empty
    }

    public static LTQRService newInstance() {

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL wsdlUrl = classLoader.getResource( "safe-online-ltqr.wsdl" );
        if (null == wsdlUrl)
            throw new RuntimeException( "SafeOnline LTQR WSDL not found" );

        return new LTQRService( wsdlUrl, new QName( "urn:net:lin-k:safe-online:ltqr", "LTQRService" ) );
    }
}
