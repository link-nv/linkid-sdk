/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.notification.consumer.ws;

import junit.framework.TestCase;
import net.lin_k.safe_online.notification.consumer.NotificationConsumerService;
import net.link.safeonline.notification.consumer.ws.NotificationConsumerServiceFactory;


public class NotificationConsumerServiceFactoryTest extends TestCase {

    public void testNewInstance()
            throws Exception {

        // Test
        NotificationConsumerService result = NotificationConsumerServiceFactory.newInstance();

        // Verify
        assertNotNull( result );
    }
}
