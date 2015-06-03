/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.ws.idmapping;

import static org.junit.Assert.*;

import net.lin_k.safe_online.idmapping.NameIdentifierMappingService;
import net.link.safeonline.ws.idmapping.LinkIDNameIdentifierMappingServiceFactory;
import org.junit.Test;


public class LinkIDNameIdentifierMappingServiceFactoryTest {

    @Test
    public void testNewInstance()
            throws Exception {

        // Test
        NameIdentifierMappingService service = LinkIDNameIdentifierMappingServiceFactory.newInstance();

        // Verify
        assertNotNull( service );
    }
}