/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.ws.session;

import junit.framework.TestCase;
import net.lin_k.safe_online.session.SessionTrackingService;
import net.link.safeonline.ws.session.SessionTrackingServiceFactory;


public class SessionTrackingServiceFactoryTest extends TestCase {

    public void testNewInstance()
            throws Exception {

        // Test
        SessionTrackingService result = SessionTrackingServiceFactory.newInstance();

        // Verify
        assertNotNull( result );
    }
}
