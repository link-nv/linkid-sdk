/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.ws.ltqr;

import junit.framework.TestCase;
import net.lin_k.safe_online.ltqr._2.LTQRService;
import net.link.safeonline.ws.ltqr.LinkIDLTQRServiceFactory;


public class LinkIDLTQRServiceFactoryTest extends TestCase {

    public void testNewInstance()
            throws Exception {

        // Test
        LTQRService result = LinkIDLTQRServiceFactory.newInstance();

        // Verify
        assertNotNull( result );
    }
}
