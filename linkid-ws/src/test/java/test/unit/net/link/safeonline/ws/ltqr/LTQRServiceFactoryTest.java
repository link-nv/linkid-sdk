package test.unit.net.link.safeonline.ws.ltqr;

import junit.framework.TestCase;
import net.lin_k.safe_online.ltqr.LTQRService;
import net.link.safeonline.ws.ltqr.LTQRServiceFactory;


public class LTQRServiceFactoryTest extends TestCase {

    public void testNewInstance()
            throws Exception {

        // Test
        LTQRService result = LTQRServiceFactory.newInstance();

        // Verify
        assertNotNull( result );
    }
}
