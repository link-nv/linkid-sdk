/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.integ.net.link.safeonline.ws;

import net.link.safeonline.sdk.ws.ping.PingClient;
import net.link.safeonline.sdk.ws.ping.PingClientImpl;

import org.junit.Test;


public class PingWebServiceTest {

    @Test
    public void testInvocation() throws Exception {

        // setup
        PingClient client = new PingClientImpl("http://localhost:8080");

        // operate
        client.ping();
    }
}
