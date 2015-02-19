/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.ws.payment;

import junit.framework.TestCase;
import net.lin_k.safe_online.payment._3.PaymentService;
import net.link.safeonline.ws.payment.PaymentServiceFactory;


public class PaymentServiceFactoryTest extends TestCase {

    public void testNewInstance()
            throws Exception {

        // Test
        PaymentService result = PaymentServiceFactory.newInstance();

        // Verify
        assertNotNull( result );
    }
}
