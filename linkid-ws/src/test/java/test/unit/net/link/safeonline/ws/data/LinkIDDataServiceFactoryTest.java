/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.ws.data;

import junit.framework.TestCase;
import liberty.dst._2006_08.ref.safe_online.DataService;
import net.link.safeonline.ws.data.LinkIDDataServiceFactory;


public class LinkIDDataServiceFactoryTest extends TestCase {

    public void testNewInstance()
            throws Exception {

        // Test
        DataService result = LinkIDDataServiceFactory.newInstance();

        // Verify
        assertNotNull( result );
    }
}
