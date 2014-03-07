/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.ws.xkms2;

import static org.junit.Assert.*;

import net.link.safeonline.ws.xkms2.Xkms2ServiceFactory;
import org.junit.Test;
import org.w3._2002._03.xkms.XKMSService;


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
