/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.integ.net.link.safeonline.ws.metro;

import static org.junit.Assert.assertNotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;


public class MetroWebServiceTest {

    private static final Log LOG = LogFactory.getLog(MetroWebServiceTest.class);


    @Test
    public void testInvocation()
            throws Exception {

        // setup
        MetroClient client = new MetroClientImpl("http://sebeco-dev-10:8080");

        // operate
        String attribute = client.getAttribute();
        assertNotNull(attribute);
        LOG.debug("returned attribute: " + attribute);
    }

    /*
     * @Test public void testSiemensWSAuthenticationService() throws Exception {
     * 
     * // setup MetroWSAuthenticationClient client = new MetroWSAuthenticationClientImpl("http://localhost:8080");
     * 
     * // operate String attribute = client.getAttribute(); assertNotNull(attribute); LOG.debug("returned attribute: " + attribute); }
     */
}
