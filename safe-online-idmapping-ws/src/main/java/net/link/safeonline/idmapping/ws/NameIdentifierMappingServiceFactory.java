/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.idmapping.ws;

import java.net.URL;
import javax.xml.namespace.QName;
import net.lin_k.safe_online.idmapping.NameIdentifierMappingService;


public class NameIdentifierMappingServiceFactory {

    private NameIdentifierMappingServiceFactory() {

        // empty
    }

    public static NameIdentifierMappingService newInstance() {

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL wsdlUrl = classLoader.getResource( "safe-online-idmapping.wsdl" );
        if (null == wsdlUrl)
            throw new RuntimeException( "linkID ID Mapping WSDL not found" );
        NameIdentifierMappingService service = new NameIdentifierMappingService( wsdlUrl, new QName( "urn:net:lin-k:safe-online:idmapping",
                "NameIdentifierMappingService" ) );
        return service;
    }
}
