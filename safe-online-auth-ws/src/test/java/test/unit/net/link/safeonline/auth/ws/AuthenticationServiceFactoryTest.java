package test.unit.net.link.safeonline.auth.ws;

import junit.framework.TestCase;
import net.lin_k.safe_online.auth.WSAuthenticationService;
import net.link.safeonline.auth.ws.soap.WSAuthenticationServiceFactory;


public class AuthenticationServiceFactoryTest extends TestCase {

    public void testNewInstance()
            throws Exception {

        // Test
        WSAuthenticationService result = WSAuthenticationServiceFactory.newInstance();

        // Verify
        assertNotNull( result );
    }
}
