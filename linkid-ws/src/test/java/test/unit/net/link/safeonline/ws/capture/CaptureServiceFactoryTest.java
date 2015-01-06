/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.ws.capture;

import junit.framework.TestCase;
import net.lin_k.safe_online.capture.CaptureService;
import net.link.safeonline.ws.capture.CaptureServiceFactory;


public class CaptureServiceFactoryTest extends TestCase {

    public void testNewInstance()
            throws Exception {

        // Test
        CaptureService result = CaptureServiceFactory.newInstance();

        // Verify
        assertNotNull( result );
    }
}
