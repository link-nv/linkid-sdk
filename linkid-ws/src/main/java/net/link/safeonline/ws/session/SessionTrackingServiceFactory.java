/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.ws.session;

import java.net.URL;
import javax.xml.namespace.QName;
import net.lin_k.safe_online.session.SessionTrackingService;


public class SessionTrackingServiceFactory {

    private SessionTrackingServiceFactory() {

        // empty
    }

    public static SessionTrackingService newInstance() {

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL wsdlUrl = classLoader.getResource( "safe-online-session.wsdl" );
        if (null == wsdlUrl)
            throw new RuntimeException( "SafeOnline Session Tracking WSDL not found" );

        SessionTrackingService service = new SessionTrackingService( wsdlUrl, new QName( "urn:net:lin-k:safe-online:session",
                "SessionTrackingService" ) );

        return service;
    }
}
