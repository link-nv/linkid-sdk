/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.idmapping.ws;

import java.net.URL;

import javax.xml.namespace.QName;

import oasis.names.tc.saml._2_0.protocol.NameIdentifierMappingService;


public class NameIdentifierMappingServiceFactory {

    private NameIdentifierMappingServiceFactory() {

        // empty
    }

    public static NameIdentifierMappingService newInstance() {

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL wsdlUrl = classLoader.getResource("saml-2.0-idmapping.wsdl");
        if (null == wsdlUrl) {
            throw new RuntimeException("SAML protocol WSDL not found");
        }
        NameIdentifierMappingService service = new NameIdentifierMappingService(wsdlUrl, new QName(
                "urn:oasis:names:tc:SAML:2.0:protocol", "NameIdentifierMappingService"));
        return service;
    }
}
