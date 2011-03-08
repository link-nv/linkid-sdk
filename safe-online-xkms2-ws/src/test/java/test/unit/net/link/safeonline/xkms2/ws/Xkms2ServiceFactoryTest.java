/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.xkms2.ws;

import static org.junit.Assert.assertNotNull;

import net.link.safeonline.xkms2.ws.Xkms2ServiceFactory;
import org.junit.Test;
import org.w3._2002._03.xkms_wsdl.XKMSService;


public class Xkms2ServiceFactoryTest {

    @Test
    public void testNewInstance()
            throws Exception {

        // Test
        XKMSService service = Xkms2ServiceFactory.newInstance();

        // Verify
        assertNotNull( service );
    }
}
