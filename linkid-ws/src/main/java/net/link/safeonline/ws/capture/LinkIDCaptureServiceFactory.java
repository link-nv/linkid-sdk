/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.ws.capture;

import java.net.URL;
import javax.xml.namespace.QName;
import net.lin_k.safe_online.capture._2.CaptureService;


public class LinkIDCaptureServiceFactory {

    private LinkIDCaptureServiceFactory() {

        // empty
    }

    public static CaptureService newInstance() {

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL wsdlUrl = classLoader.getResource( "safe-online-capture-2.0.wsdl" );
        if (null == wsdlUrl)
            throw new RuntimeException( "SafeOnline Capture WSDL not found" );

        return new CaptureService( wsdlUrl, new QName( "urn:net:lin-k:safe-online:capture:2.0", "CaptureService" ) );
    }
}
