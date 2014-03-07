/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.ws.auth;

import junit.framework.TestCase;
import net.lin_k.safe_online.auth.WSAuthenticationService;
import net.link.safeonline.ws.auth.WSAuthenticationServiceFactory;


public class AuthenticationServiceFactoryTest extends TestCase {

    public void testNewInstance()
            throws Exception {

        // Test
        WSAuthenticationService result = WSAuthenticationServiceFactory.newInstance();

        // Verify
        assertNotNull( result );
    }
}
