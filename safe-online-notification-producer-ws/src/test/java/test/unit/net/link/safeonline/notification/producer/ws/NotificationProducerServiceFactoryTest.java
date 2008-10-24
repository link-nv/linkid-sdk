/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.notification.producer.ws;

import junit.framework.TestCase;
import net.lin_k.safe_online.notification.producer.NotificationProducerService;
import net.link.safeonline.notification.producer.ws.NotificationProducerServiceFactory;

public class NotificationProducerServiceFactoryTest extends TestCase {

    public void testNewInstance() throws Exception {

        // operate
        NotificationProducerService result = NotificationProducerServiceFactory
                .newInstance();

        // verify
        assertNotNull(result);
    }
}
