package test.unit.net.link.safeonline.auth.ws;

import junit.framework.TestCase;
import net.lin_k.safe_online.auth.GetWSAuthenticationService;
import net.link.safeonline.auth.ws.GetWSAuthenticationServiceFactory;


public class GetAuthenticationServiceFactoryTest extends TestCase {

    public void testNewInstance()
            throws Exception {

        // Test
        GetWSAuthenticationService result = GetWSAuthenticationServiceFactory.newInstance();

        // Verify
        assertNotNull( result );
    }
}
