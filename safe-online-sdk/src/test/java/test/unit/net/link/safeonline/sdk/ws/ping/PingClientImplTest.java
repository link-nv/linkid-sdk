/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.sdk.ws.ping;

import net.link.safeonline.sdk.ws.ping.PingClientImpl;

import org.junit.Test;


public class PingClientImplTest {

    @Test
    public void testInstance() throws Exception {

        new PingClientImpl("http://localhost:8080");
    }
}
