package test.unit.net.link.safeonline.session.ws;

import junit.framework.TestCase;
import net.lin_k.safe_online.session.SessionTrackingService;
import net.link.safeonline.session.ws.SessionTrackingServiceFactory;


public class SessionTrackingServiceFactoryTest extends TestCase {

    public void testNewInstance()
            throws Exception {

        // Test
        SessionTrackingService result = SessionTrackingServiceFactory.newInstance();

        // Verify
        assertNotNull( result );
    }
}
