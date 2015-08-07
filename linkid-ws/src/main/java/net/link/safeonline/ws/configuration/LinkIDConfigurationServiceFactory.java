/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.ws.configuration;

import java.net.URL;
import javax.xml.namespace.QName;
import net.lin_k.safe_online.configuration._2.ConfigurationService;


public class LinkIDConfigurationServiceFactory {

    private LinkIDConfigurationServiceFactory() {

        // empty
    }

    public static ConfigurationService newInstance() {

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL wsdlUrl = classLoader.getResource( "safe-online-configuration-2.0.wsdl" );
        if (null == wsdlUrl)
            throw new RuntimeException( "SafeOnline Configuration WSDL not found" );

        return new ConfigurationService( wsdlUrl, new QName( "urn:net:lin-k:safe-online:configuration:2.0", "ConfigurationService" ) );
    }
}
