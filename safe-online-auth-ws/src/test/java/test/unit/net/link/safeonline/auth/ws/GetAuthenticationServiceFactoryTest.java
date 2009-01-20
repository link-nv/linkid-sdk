package test.unit.net.link.safeonline.auth.ws;

import junit.framework.TestCase;
import net.lin_k.safe_online.auth.GetAuthenticationService;
import net.link.safeonline.auth.ws.GetAuthenticationServiceFactory;

public class GetAuthenticationServiceFactoryTest extends TestCase {

    public void testNewInstance() throws Exception {

        // operate
        GetAuthenticationService result = GetAuthenticationServiceFactory
                .newInstance();

        // verify
        assertNotNull(result);
    }
}
