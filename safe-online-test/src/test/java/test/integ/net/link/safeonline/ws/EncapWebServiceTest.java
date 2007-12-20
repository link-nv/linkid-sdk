package test.integ.net.link.safeonline.ws;

import static org.junit.Assert.assertNotNull;

import java.net.MalformedURLException;
import java.rmi.RemoteException;

import net.link.safeonline.sdk.ws.encap.activation.EncapActivationClient;
import net.link.safeonline.sdk.ws.encap.activation.EncapActivationClientImpl;

import org.junit.Test;

public class EncapWebServiceTest {

	private static final String ENCAP_LOCATION = "test.encap.no/mSec2";

	// localhost:8080/safe-online-encap-ws

	@Test
	public void testActivationWebService() throws MalformedURLException,
			RemoteException {
		String mobile = "95874644";
		String orgId = "encap";
		EncapActivationClient activationClient = new EncapActivationClientImpl(
				ENCAP_LOCATION);
		String activationCode = activationClient.activate(mobile, orgId, "");
		assertNotNull(activationCode);
	}

}
