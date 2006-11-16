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

	private AuthClient authClient;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		this.authClient = new AuthClient(SAFE_ONLINE_LOCATION);
	}

	public void testAvailabilityViaEcho() throws Exception {
		// setup
		String message = "hello world";

		// operate
		String result = this.authClient.echo(message);

		// verify
		assertEquals(message, result);
	}

	public void testAuthenticateFcorneli() throws Exception {
		// setup
		String username = "fcorneli";
		String password = "secret";

		// operate
		boolean result = this.authClient.authenticate(username, password);

		// verify
		assertTrue(result);
	}

	public void testFoobarNotAuthenticated() throws Exception {
		// setup
		String username = "foobar";
		String password = "foobar";

		// operate
		boolean result = this.authClient.authenticate(username, password);

		// verify
		assertFalse(result);
	}
}
