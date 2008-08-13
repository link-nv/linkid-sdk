/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.demo.webapp;

import static org.junit.Assert.assertNotNull;

import java.io.InputStream;

import org.junit.Test;


public class KeyStoreResourceTest {

    @Test
    public void keystoreAvailability() throws Exception {

        // setup
        Thread currenThread = Thread.currentThread();
        ClassLoader classLoader = currenThread.getContextClassLoader();

        // operate
        InputStream result = classLoader.getResourceAsStream("safe-online-demo-keystore.jks");

        // verify
        assertNotNull(result);
    }
}
