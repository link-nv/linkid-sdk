/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.ws.configuration;

import junit.framework.TestCase;
import net.lin_k.safe_online.configuration.ConfigurationService;
import net.link.safeonline.ws.configuration.ConfigurationServiceFactory;


public class ConfigurationServiceFactoryTest extends TestCase {

    public void testNewInstance()
            throws Exception {

        // Test
        ConfigurationService result = ConfigurationServiceFactory.newInstance();

        // Verify
        assertNotNull( result );
    }
}
