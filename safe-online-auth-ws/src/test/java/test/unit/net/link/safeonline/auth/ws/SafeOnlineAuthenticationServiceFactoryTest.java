package test.unit.net.link.safeonline.auth.ws;

import junit.framework.TestCase;
import net.lin_k.safe_online.auth._1.SafeOnlineAuthenticationService;
import net.link.safeonline.auth.ws.SafeOnlineAuthenticationServiceFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SafeOnlineAuthenticationServiceFactoryTest extends TestCase {

	private static final Log LOG = LogFactory
			.getLog(SafeOnlineAuthenticationServiceFactoryTest.class);

	public void testNewInstance() throws Exception {
		// operate
		SafeOnlineAuthenticationService result = SafeOnlineAuthenticationServiceFactory
				.newInstance();

		// verify
		assertNotNull(result);
		LOG.debug("result service name: " + result.getServiceName());
	}
}
