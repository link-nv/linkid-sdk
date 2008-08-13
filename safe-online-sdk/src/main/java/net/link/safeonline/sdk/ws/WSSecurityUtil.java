/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.ws;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPFault;
import javax.xml.ws.soap.SOAPFaultException;


public class WSSecurityUtil {

    private WSSecurityUtil() {

        // empty
    }

    public static SOAPFaultException createSOAPFaultException(String faultString, String wsseFaultCode) {

        SOAPFault soapFault;
        try {
            SOAPFactory soapFactory = SOAPFactory.newInstance();
            soapFault = soapFactory.createFault(faultString, new QName(
                    "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd", wsseFaultCode,
                    "wsse"));
        } catch (SOAPException e) {
            throw new RuntimeException("SOAP error");
        }
        return new SOAPFaultException(soapFault);
    }
}
