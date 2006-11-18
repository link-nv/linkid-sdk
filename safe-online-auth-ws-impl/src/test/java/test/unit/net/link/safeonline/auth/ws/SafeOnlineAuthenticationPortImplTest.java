package test.unit.net.link.safeonline.auth.ws;

import junit.framework.TestCase;
import net.lin_k.safe_online.auth._1_0.types.AuthenticateRequestType;
import net.lin_k.safe_online.auth._1_0.types.AuthenticateResultType;
import net.link.safeonline.auth.ws.SafeOnlineAuthenticationPortImpl;

public class SafeOnlineAuthenticationPortImplTest extends TestCase {

	private SafeOnlineAuthenticationPortImpl testedInstance;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		this.testedInstance = new SafeOnlineAuthenticationPortImpl();
	}

	public void testEcho() throws Exception {
		// setup
		String msg = "hello world";

		// operate
		String result = this.testedInstance.echo(msg);

		// verify
		assertEquals(msg, result);
	}

	public void testAuthenticate() throws Exception {
		// setup
		String username = "fcorneli";
		String password = "secret";

		// operate & verify
		operateAndVerify(true, username, password);
	}

	public void testFoobarNotAuthenticated() throws Exception {
		// setup
		String username = "foobar";
		String password = "foobar";

		// operate & verify
		operateAndVerify(false, username, password);
	}

	public void testWrongPasswordNotAuthenticated() throws Exception {
		// setup
		String username = "fcorneli";
		String password = "foobar";

		// operate & verify
		operateAndVerify(false, username, password);
	}

	public void testNullPasswordDoesNotAuthenticate() throws Exception {
		// setup
		String username = "fcorneli";
		String password = null;

		// operate & verify
		operateAndVerify(false, username, password);
	}

	public void testNullUsernameDoesNotAuthenticate() throws Exception {
		// setup
		String username = null;
		String password = "foobar";

		// operate & verify
		operateAndVerify(false, username, password);
	}

	private void operateAndVerify(boolean authResult, String username,
			String password) {
		// operate
		AuthenticateRequestType request = createAuthenticateRequestType(
				username, password);
		AuthenticateResultType result = this.testedInstance
				.authenticate(request);

		// verify
		assertNotNull(result);
		assertEquals(authResult, result.isAuthenticated());
	}

	private AuthenticateRequestType createAuthenticateRequestType(
			String username, String password) {
		AuthenticateRequestType request = new AuthenticateRequestType();
		request.setUsername(username);
		request.setPassword(password);
		return request;
	}
}
