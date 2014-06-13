/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.ws.mandate;

import java.net.URL;
import javax.xml.namespace.QName;
import net.lin_k.safe_online.mandate.MandateService;


public class MandateServiceFactory {

    private MandateServiceFactory() {

        // empty
    }

    public static MandateService newInstance() {

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL wsdlUrl = classLoader.getResource( "safe-online-mandate.wsdl" );
        if (null == wsdlUrl)
            throw new RuntimeException( "SafeOnline Mandate WSDL not found" );

        return new MandateService( wsdlUrl, new QName( "urn:net:lin-k:safe-online:mandate", "MandateService" ) );
    }
}
