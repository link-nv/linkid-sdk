/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.ws.reporting;

import junit.framework.TestCase;
import net.lin_k.safe_online.reporting._3.ReportingService;
import net.link.safeonline.ws.reporting.LinkIDReportingServiceFactory;


public class LinkIDReportingServiceFactoryTest extends TestCase {

    public void testNewInstance()
            throws Exception {

        // Test
        ReportingService result = LinkIDReportingServiceFactory.newInstance();

        // Verify
        assertNotNull( result );
    }
}
