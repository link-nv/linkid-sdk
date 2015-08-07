/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.ws.auth;

import junit.framework.TestCase;
import net.lin_k.safe_online.auth._2.AuthService;
import net.link.safeonline.ws.auth.LinkIDAuthServiceFactory;


public class LinkIDAuthServiceFactoryTest extends TestCase {

    public void testNewInstance()
            throws Exception {

        // Test
        AuthService result = LinkIDAuthServiceFactory.newInstance();

        // Verify
        assertNotNull( result );
    }
}
