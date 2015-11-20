/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.ws.linkid;

import junit.framework.TestCase;
import net.lin_k.linkid._3_1.core.LinkIDService;
import net.link.safeonline.ws.linkid.LinkIDWSServiceFactory;


public class LinkIDWSServiceFactoryTest extends TestCase {

    public void testNewInstance()
            throws Exception {

        // Test
        LinkIDService result = LinkIDWSServiceFactory.newInstance();

        // Verify
        assertNotNull( result );
    }
}
