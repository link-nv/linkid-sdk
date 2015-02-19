/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.ws.payment;

import java.net.URL;
import javax.xml.namespace.QName;
import net.lin_k.safe_online.payment._3.PaymentService;


public class PaymentServiceFactory {

    private PaymentServiceFactory() {

        // empty
    }

    public static PaymentService newInstance() {

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL wsdlUrl = classLoader.getResource( "safe-online-payment-3.0.wsdl" );
        if (null == wsdlUrl)
            throw new RuntimeException( "SafeOnline Payment WSDL not found" );

        return new PaymentService( wsdlUrl, new QName( "urn:net:lin-k:safe-online:payment:3.0", "PaymentService" ) );
    }
}
