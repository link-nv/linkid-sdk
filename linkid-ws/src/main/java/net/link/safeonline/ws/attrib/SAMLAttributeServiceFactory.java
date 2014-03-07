/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.ws.attrib;

import java.net.URL;
import javax.xml.namespace.QName;
import oasis.names.tc.saml._2_0.protocol.SAMLAttributeService;


public class SAMLAttributeServiceFactory {

    private SAMLAttributeServiceFactory() {

        // empty
    }

    public static SAMLAttributeService newInstance() {

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL wsdlUrl = classLoader.getResource( "saml-protocol-2.0.wsdl" );
        if (null == wsdlUrl)
            throw new RuntimeException( "SAML protocol WSDL not found" );

        SAMLAttributeService service = new SAMLAttributeService( wsdlUrl, new QName( "urn:oasis:names:tc:SAML:2.0:protocol",
                "SAMLAttributeService" ) );

        return service;
    }
}
