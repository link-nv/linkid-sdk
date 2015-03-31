/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.ws.attrib;

import junit.framework.TestCase;
import net.link.safeonline.ws.attrib.LinkIDSAMLAttributeServiceFactory;
import oasis.names.tc.saml._2_0.protocol.SAMLAttributeService;


public class LinkIDSAMLAttributeServiceFactoryTest extends TestCase {

    public void testNewInstance()
            throws Exception {

        // Test
        SAMLAttributeService result = LinkIDSAMLAttributeServiceFactory.newInstance();

        // Verify
        assertNotNull( result );
    }
}
