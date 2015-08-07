/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.ws.haws;

import junit.framework.TestCase;
import net.lin_k.safe_online.haws._2.HawsService;
import net.link.safeonline.ws.haws.LinkIDHawsServiceFactory;


public class LinkIDHawsServiceFactoryTest extends TestCase {

    public void testNewInstance()
            throws Exception {

        // Test
        HawsService result = LinkIDHawsServiceFactory.newInstance();

        // Verify
        assertNotNull( result );
    }
}
