package test.integ.net.link.safeonline.ws;

import static org.junit.Assert.assertNotNull;
import net.link.safeonline.sdk.ws.encap.activation.EncapActivationClient;
import net.link.safeonline.sdk.ws.encap.activation.EncapActivationClientImpl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;


public class EncapWebServiceTest {

    private static final Log    LOG            = LogFactory.getLog(EncapWebServiceTest.class);

    // private static final String ENCAP_LOCATION = "test.encap.no/mSec2";
    // private static final String ENCAP_LOCATION =
    // localhost:8080/safe-online-encap-ws

    private static final String ENCAP_LOCATION = "81.246.63.169:9090/mSec2";

    @SuppressWarnings("unused")
    private static final String TELENOR_MOBILE = "95874644";


    @Test
    public void testActivationWebService() throws Exception {

        String mobile = "+32494575697";
        String orgId = "test1";
        EncapActivationClient activationClient = new EncapActivationClientImpl(ENCAP_LOCATION);
        String activationCode = activationClient.activate(mobile, orgId, "");
        LOG.debug("received activation code: " + activationCode);
        assertNotNull(activationCode);
    }
}
