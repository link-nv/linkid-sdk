/*
 * SafeOnline project.
 *
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.p11sc;

import junit.framework.TestCase;
import net.link.safeonline.p11sc.SmartCard;
import net.link.safeonline.p11sc.SmartCardFactory;


public class SmartCardFactoryTest extends TestCase {

    public void testNewInstance() throws Exception {

        // operate
        SmartCard smartCard = SmartCardFactory.newInstance();

        // verify
        assertNotNull(smartCard);
    }
}
