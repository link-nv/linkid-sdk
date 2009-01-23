/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.integ.net.link.safeonline.ws;

import static org.junit.Assert.assertNotNull;
import net.link.safeonline.siemens.auth.ws.acceptance.jaxws.ws.client.SiemensAuthWsAcceptanceClient;
import net.link.safeonline.siemens.auth.ws.acceptance.jaxws.ws.client.SiemensAuthWsAcceptanceClientImpl;
import oasis.names.tc.saml._2_0.assertion.AssertionType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;


public class SiemensWebServiceTest {

    private static final Log LOG = LogFactory.getLog(SiemensWebServiceTest.class);


    @Test
    public void testSiemensJaxWs()
            throws Exception {

        // setup
        AssertionType assertion = new AssertionType();

        SiemensAuthWsAcceptanceClient client = new SiemensAuthWsAcceptanceClientImpl("http://localhost:8080", assertion);

        // operate
        String attribute = client.getAttribute();
        assertNotNull(attribute);
        LOG.debug("returned attribute: " + attribute);
    }
}
