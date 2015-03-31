/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.ws.ltqr;

import java.net.URL;
import javax.xml.namespace.QName;
import net.lin_k.safe_online.ltqr._2.LTQRService;


public class LinkIDLTQR20ServiceFactory {

    private LinkIDLTQR20ServiceFactory() {

        // empty
    }

    public static LTQRService newInstance() {

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL wsdlUrl = classLoader.getResource( "safe-online-ltqr-2.0.wsdl" );
        if (null == wsdlUrl)
            throw new RuntimeException( "SafeOnline LTQR 2.0 WSDL not found" );

        return new LTQRService( wsdlUrl, new QName( "urn:net:lin-k:safe-online:ltqr:2.0", "LTQRService" ) );
    }
}
