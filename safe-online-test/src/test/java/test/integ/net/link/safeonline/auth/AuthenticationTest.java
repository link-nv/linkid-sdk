package test.integ.net.link.safeonline.auth;

import net.link.safeonline.sdk.auth.AuthClient;
import junit.framework.TestCase;

/**
 * Integration test for the SafeOnline authentication web service.
 * 
 * @author fcorneli
 * 
 */
public class AuthenticationTest extends TestCase {

	private static final String SAFE_ONLINE_LOCATION = "localhost:8080";

	public void testAvailabilityViaEcho() throws Exception {
		// setup
		AuthClient authClient = new AuthClient(SAFE_ONLINE_LOCATION);

		String message = "hello world";

		// operate
		String result = authClient.echo(message);

		// verify
		assertEquals(message, result);
	}
}
