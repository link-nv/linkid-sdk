package test.unit.net.link.safeonline.ws.auth;

import junit.framework.TestCase;
import net.lin_k.safe_online.auth.WSAuthenticationService;
import net.link.safeonline.ws.auth.WSAuthenticationServiceFactory;


public class AuthenticationServiceFactoryTest extends TestCase {

    public void testNewInstance()
            throws Exception {

        // Test
        WSAuthenticationService result = WSAuthenticationServiceFactory.newInstance();

        // Verify
        assertNotNull( result );
    }
}
