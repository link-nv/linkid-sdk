/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.ws.data;

import junit.framework.TestCase;
import liberty.dst._2006_08.ref.safe_online.DataService;
import net.link.safeonline.ws.data.DataServiceFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class DataServiceFactoryTest extends TestCase {

    private static final Log LOG = LogFactory.getLog( DataServiceFactoryTest.class );

    public void testNewInstance()
            throws Exception {

        // Test
        DataService result = DataServiceFactory.newInstance();

        // Verify
        assertNotNull( result );
        LOG.debug( "result service name: " + result.getServiceName() );
    }
}
