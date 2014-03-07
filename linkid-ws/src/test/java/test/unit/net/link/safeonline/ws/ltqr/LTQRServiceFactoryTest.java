/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.ws.ltqr;

import junit.framework.TestCase;
import net.lin_k.safe_online.ltqr.LTQRService;
import net.link.safeonline.ws.ltqr.LTQRServiceFactory;


public class LTQRServiceFactoryTest extends TestCase {

    public void testNewInstance()
            throws Exception {

        // Test
        LTQRService result = LTQRServiceFactory.newInstance();

        // Verify
        assertNotNull( result );
    }
}
