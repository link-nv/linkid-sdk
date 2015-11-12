/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.ws.linkid;

import java.net.URL;
import javax.xml.namespace.QName;
import net.lin_k.linkid._3.LinkIDService;


public class LinkIDWSServiceFactory {

    private LinkIDWSServiceFactory() {

        // empty
    }

    public static LinkIDService newInstance() {

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL wsdlUrl = classLoader.getResource( "linkid-3.1.wsdl" );
        if (null == wsdlUrl)
            throw new RuntimeException( "linkID WSDL not found" );

        return new LinkIDService( wsdlUrl, new QName( "urn:net:lin-k:linkid:3.1", "LinkIDService" ) );
    }
}
