package test.integ.net.link.safeonline.ws;

import static org.junit.Assert.assertTrue;
import net.link.safeonline.sdk.ws.encap.authentication.EncapAuthenticationClient;
import net.link.safeonline.sdk.ws.encap.authentication.EncapAuthenticationClientImpl;

import org.junit.Test;


public class EncapWebServiceTest {

    // private static final String ENCAP_LOCATION = "test.encap.no/mSec2";
    // private static final String ENCAP_LOCATION =
    // localhost:8080/safe-online-encap-ws
    // private static final String ENCAP_LOCATION = "81.246.63.169:9090/mSec2";
    private static final String ENCAP_LOCATION = "test.encap.no/mobileotp";

    @SuppressWarnings("unused")
    private static final String TELENOR_MOBILE = "95874644";


    @Test
    public void testAuthentication()
            throws Exception {

        String mobile = "4795875994";
        String orgId = "test1";
        EncapAuthenticationClient authClient = new EncapAuthenticationClientImpl(ENCAP_LOCATION);
        String challenge = authClient.challenge(mobile, orgId);
        Thread.sleep(1000 * 30);
        assertTrue(authClient.verifyOTP(challenge, "123456"));

    }
}
