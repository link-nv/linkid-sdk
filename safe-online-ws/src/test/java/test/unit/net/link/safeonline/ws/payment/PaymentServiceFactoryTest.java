package test.unit.net.link.safeonline.ws.payment;

import junit.framework.TestCase;
import net.lin_k.safe_online.payment.PaymentService;
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
