package test.unit.net.link.safeonline.auth.ws;

import junit.framework.TestCase;
import net.lin_k.safe_online.auth.AuthenticationService;
import net.link.safeonline.auth.ws.AuthenticationServiceFactory;

public class AuthenticationServiceFactoryTest extends TestCase {

    public void testNewInstance() throws Exception {

        // operate
        AuthenticationService result = AuthenticationServiceFactory
                .newInstance();

        // verify
        assertNotNull(result);
    }
}
