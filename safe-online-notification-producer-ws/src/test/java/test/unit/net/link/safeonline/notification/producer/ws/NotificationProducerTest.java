/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.notification.producer.ws;

import javax.xml.transform.dom.DOMResult;
import javax.xml.ws.wsaddressing.W3CEndpointReference;
import javax.xml.ws.wsaddressing.W3CEndpointReferenceBuilder;

import org.junit.Assert;
import org.junit.Test;


public class NotificationProducerTest {

    @Test
    public void testW3CEndpointReference() throws Exception {

        String address = "test-address";

        W3CEndpointReferenceBuilder builder = new W3CEndpointReferenceBuilder();
        builder.address(address);
        W3CEndpointReference endpoint = builder.build();
        Assert.assertNotNull(endpoint);

        DOMResult domResult = new DOMResult();
        endpoint.writeTo(domResult);
        String resultAddress = domResult.getNode().getFirstChild().getFirstChild().getFirstChild().getNodeValue();
        Assert.assertEquals(address, resultAddress);

    }
}
