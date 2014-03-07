/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.ws.haws;

import java.net.URL;
import javax.xml.namespace.QName;
import net.lin_k.safe_online.haws.HawsService;


public class HawsServiceFactory {

    private HawsServiceFactory() {

        // empty
    }

    public static HawsService newInstance() {

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL wsdlUrl = classLoader.getResource( "safe-online-haws.wsdl" );
        if (null == wsdlUrl)
            throw new RuntimeException( "SafeOnline Haws WSDL not found" );

        return new HawsService( wsdlUrl, new QName( "urn:net:lin-k:safe-online:haws", "HawsService" ) );
    }
}
