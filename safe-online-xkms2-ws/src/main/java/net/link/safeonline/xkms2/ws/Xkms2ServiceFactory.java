/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.xkms2.ws;

import java.net.URL;
import javax.xml.namespace.QName;
import org.w3._2002._03.xkms_wsdl.XKMSService;


public class Xkms2ServiceFactory {

    private Xkms2ServiceFactory() {

        // empty
    }

    public static XKMSService newInstance() {

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL wsdlUrl = classLoader.getResource( "xkms.wsdl" );
        if (null == wsdlUrl)
            throw new RuntimeException( "XKMS WSDL not found" );
        XKMSService service = new XKMSService( wsdlUrl, new QName( "http://www.w3.org/2002/03/xkms#wsdl", "XKMSService" ) );
        return service;
    }
}
