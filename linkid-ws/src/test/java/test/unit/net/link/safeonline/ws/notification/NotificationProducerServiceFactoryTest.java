/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.ws.notification;

import junit.framework.TestCase;
import net.lin_k.safe_online.notification.producer.NotificationProducerService;
import net.link.safeonline.ws.notification.NotificationProducerServiceFactory;


public class NotificationProducerServiceFactoryTest extends TestCase {

    public void testNewInstance()
            throws Exception {

        // Test
        NotificationProducerService result = NotificationProducerServiceFactory.newInstance();

        // Verify
        assertNotNull( result );
    }
}
