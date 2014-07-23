/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.ws.mandate;

import junit.framework.TestCase;
import net.lin_k.safe_online.mandate.MandateService;
import net.link.safeonline.ws.mandate.MandateServiceFactory;


public class MandateServiceFactoryTest extends TestCase {

    public void testNewInstance()
            throws Exception {

        // Test
        MandateService result = MandateServiceFactory.newInstance();

        // Verify
        assertNotNull( result );
    }
}