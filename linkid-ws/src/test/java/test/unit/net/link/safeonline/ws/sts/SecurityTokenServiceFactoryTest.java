/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.ws.sts;

import static org.junit.Assert.*;

import net.link.safeonline.ws.sts.SecurityTokenServiceFactory;
import org.junit.Test;
import org.oasis_open.docs.ws_sx.ws_trust._200512.SecurityTokenService;


public class SecurityTokenServiceFactoryTest {

    @Test
    public void testNewInstance()
            throws Exception {

        // Test
        SecurityTokenService service = SecurityTokenServiceFactory.newInstance();

        // Verify
        assertNotNull( service );
    }
}
