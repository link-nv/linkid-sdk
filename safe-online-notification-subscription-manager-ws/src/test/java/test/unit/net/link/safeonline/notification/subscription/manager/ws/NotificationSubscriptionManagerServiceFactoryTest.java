/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.notification.subscription.manager.ws;

import junit.framework.TestCase;
import net.lin_k.safe_online.notification.subscription.manager.NotificationSubscriptionManagerService;
import net.link.safeonline.notification.subscription.manager.ws.NotificationSubscriptionManagerServiceFactory;


public class NotificationSubscriptionManagerServiceFactoryTest extends TestCase {

    public void testNewInstance() throws Exception {

        // operate
        NotificationSubscriptionManagerService result = NotificationSubscriptionManagerServiceFactory.newInstance();

        // verify
        assertNotNull(result);
    }
}
