/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.ws.notification;

import junit.framework.TestCase;
import net.lin_k.safe_online.notification.subscription.manager.NotificationSubscriptionManagerService;
import net.link.safeonline.ws.notification.NotificationSubscriptionManagerServiceFactory;


public class NotificationSubscriptionManagerServiceFactoryTest extends TestCase {

    public void testNewInstance()
            throws Exception {

        // Test
        NotificationSubscriptionManagerService result = NotificationSubscriptionManagerServiceFactory.newInstance();

        // Verify
        assertNotNull( result );
    }
}
