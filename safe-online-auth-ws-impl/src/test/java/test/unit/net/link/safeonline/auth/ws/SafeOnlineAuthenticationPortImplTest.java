package test.unit.net.link.safeonline.auth.ws;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;

import junit.framework.TestCase;
import net.lin_k.safe_online.auth._1_0.types.AuthenticateRequestType;
import net.lin_k.safe_online.auth._1_0.types.AuthenticateResultType;
import net.link.safeonline.auth.ws.SafeOnlineAuthenticationPortImpl;
import net.link.safeonline.authentication.service.AuthenticationService;

public class SafeOnlineAuthenticationPortImplTest extends TestCase {

	private SafeOnlineAuthenticationPortImpl testedInstance;

	private AuthenticationService mockAuthenticationService;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		System.setProperty(Context.INITIAL_CONTEXT_FACTORY,
				"org.shiftone.ooc.InitialContextFactoryImpl");

		this.testedInstance = new SafeOnlineAuthenticationPortImpl();

		InitialContext initialContext = new InitialContext();
		NamingEnumeration<NameClassPair> list = initialContext.list("");
		Context safeOnlineContext;
		if (list.hasMore()) {
			safeOnlineContext = (Context) initialContext.lookup("SafeOnline");
		} else {
			safeOnlineContext = initialContext.createSubcontext("SafeOnline");
		}
		list = safeOnlineContext.list("");
		Context authenticationServiceBeanContext;
		if (list.hasMore()) {
			authenticationServiceBeanContext = (Context) safeOnlineContext
					.lookup("AuthenticationServiceBean");
		} else {
			authenticationServiceBeanContext = safeOnlineContext
					.createSubcontext("AuthenticationServiceBean");
		}

		this.mockAuthenticationService = createMock(AuthenticationService.class);
		authenticationServiceBeanContext.rebind("local",
				this.mockAuthenticationService);
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
		String applicationName = "test-application";
		String username = "test-username";
		String password = "test-password";

		// expectations
		expect(
				this.mockAuthenticationService.authenticate(applicationName,
						username, password)).andStubReturn(true);

		// prepare
		replay(this.mockAuthenticationService);

		// operate
		AuthenticateRequestType request = new AuthenticateRequestType();
		request.setApplication(applicationName);
		request.setUsername(username);
		request.setPassword(password);
		AuthenticateResultType result = this.testedInstance
				.authenticate(request);

		// verify
		verify(this.mockAuthenticationService);
		assertNotNull(result);
		assertTrue(result.isAuthenticated());
	}
}
