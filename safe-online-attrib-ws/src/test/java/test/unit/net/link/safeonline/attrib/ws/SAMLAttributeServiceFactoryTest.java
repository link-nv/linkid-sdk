/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.attrib.ws;

import junit.framework.TestCase;
import net.link.safeonline.attrib.ws.SAMLAttributeServiceFactory;
import oasis.names.tc.saml._2_0.protocol.SAMLAttributeService;


public class SAMLAttributeServiceFactoryTest extends TestCase {

    public void testNewInstance()
            throws Exception {

        // Test
        SAMLAttributeService result = SAMLAttributeServiceFactory.newInstance();

        // Verify
        assertNotNull( result );
    }
}
