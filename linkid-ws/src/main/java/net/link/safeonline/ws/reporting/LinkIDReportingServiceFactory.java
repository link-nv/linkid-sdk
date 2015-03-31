/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.ws.reporting;

import java.net.URL;
import javax.xml.namespace.QName;
import net.lin_k.safe_online.reporting._2.ReportingService;


public class LinkIDReportingServiceFactory {

    private LinkIDReportingServiceFactory() {

        // empty
    }

    public static ReportingService newInstance() {

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL wsdlUrl = classLoader.getResource( "safe-online-reporting-2.0.wsdl" );
        if (null == wsdlUrl)
            throw new RuntimeException( "SafeOnline Reporting WSDL not found" );

        return new ReportingService( wsdlUrl, new QName( "urn:net:lin-k:safe-online:reporting:2.0", "ReportingService" ) );
    }
}
