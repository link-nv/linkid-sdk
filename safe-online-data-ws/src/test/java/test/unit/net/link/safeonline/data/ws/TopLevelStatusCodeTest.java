/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.data.ws;

import junit.framework.TestCase;
import net.link.safeonline.data.ws.TopLevelStatusCode;


public class TopLevelStatusCodeTest extends TestCase {

    public void testFromCode() throws Exception {

        // setup
        String testCode = TopLevelStatusCode.OK.getCode();

        // operate
        TopLevelStatusCode result = TopLevelStatusCode.fromCode(testCode);

        // verify
        assertEquals(TopLevelStatusCode.OK, result);
    }
}
