/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.sts.ws;

import static org.junit.Assert.assertNotNull;
import net.link.safeonline.sts.ws.SecurityTokenServiceFactory;

import org.junit.Test;
import org.oasis_open.docs.ws_sx.ws_trust._200512.SecurityTokenService;


public class SecurityTokenServiceFactoryTest {

    @Test
    public void testNewInstance() throws Exception {

        // operate
        SecurityTokenService service = SecurityTokenServiceFactory.newInstance();

        // verify
        assertNotNull(service);
    }
}
