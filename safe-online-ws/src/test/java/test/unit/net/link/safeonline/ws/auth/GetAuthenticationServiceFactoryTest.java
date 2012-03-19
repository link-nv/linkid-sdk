package test.unit.net.link.safeonline.ws.auth;

import junit.framework.TestCase;
import net.lin_k.safe_online.auth.GetWSAuthenticationService;
import net.link.safeonline.ws.auth.GetWSAuthenticationServiceFactory;


public class GetAuthenticationServiceFactoryTest extends TestCase {

    public void testNewInstance()
            throws Exception {

        // Test
        GetWSAuthenticationService result = GetWSAuthenticationServiceFactory.newInstance();

        // Verify
        assertNotNull( result );
    }
}
