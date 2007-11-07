package test.spike.net.link.safeonline;

import static org.junit.Assert.assertTrue;

import java.net.MalformedURLException;
import java.rmi.RemoteException;

import javax.xml.namespace.QName;
import javax.xml.rpc.ServiceException;

import net.link.safeonline.sdk.ws.encap.activation.EncapActivationClient;
import net.link.safeonline.sdk.ws.encap.activation.EncapActivationClientImpl;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

public class AxisTest {

	private final static Log LOG = LogFactory.getLog(AxisTest.class);

	@Test
	public void testAxisVersion() {
		String endpoint = "http://localhost:8080/safe-online-encap-ws/services/Version";
		Service service = new Service();
		Call call;
		try {
			call = (Call) service.createCall();
		} catch (ServiceException e) {
			LOG.debug("failed to create call: " + endpoint);
			return;
		}
		call.setTargetEndpointAddress(endpoint);
		call.setOperationName(new QName(endpoint, "getVersion"));
		String version;
		try {
			version = (String) call.invoke(new Object[] {});
			LOG.debug("axis version: " + version);
		} catch (RemoteException e) {
			LOG.debug("error invoking version call");
		}
	}

	@Test
	public void testActivationWebservice() throws MalformedURLException,
			RemoteException {
		String location = "test.encap.no/pp-encap-r1";
		// String location = "localhost:8080/safe-online-encap-ws";
		EncapActivationClient activationClient = new EncapActivationClientImpl(
				location);
		boolean result = activationClient.activate("32494575697", "enCap", "");
		assertTrue(result);
	}
}
