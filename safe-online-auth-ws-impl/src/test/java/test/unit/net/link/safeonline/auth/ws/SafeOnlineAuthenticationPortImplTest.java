/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.auth.ws;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import javax.xml.ws.BindingProvider;

import junit.framework.TestCase;
import net.lin_k.safe_online.auth._1.SafeOnlineAuthenticationPort;
import net.lin_k.safe_online.auth._1.SafeOnlineAuthenticationService;
import net.lin_k.safe_online.auth._1_0.types.AuthenticateRequestType;
import net.lin_k.safe_online.auth._1_0.types.AuthenticateResultType;
import net.link.safeonline.auth.ws.SafeOnlineAuthenticationPortImpl;
import net.link.safeonline.auth.ws.SafeOnlineAuthenticationServiceFactory;
import net.link.safeonline.authentication.service.AuthenticationService;
import net.link.safeonline.test.util.JndiTestUtils;
import net.link.safeonline.test.util.WebServiceTestUtils;

public class SafeOnlineAuthenticationPortImplTest extends TestCase {

	private SafeOnlineAuthenticationPortImpl testedInstance;

	private AuthenticationService mockAuthenticationService;

	private JndiTestUtils jndiTestUtils;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		this.testedInstance = new SafeOnlineAuthenticationPortImpl();

		this.mockAuthenticationService = createMock(AuthenticationService.class);
		this.jndiTestUtils = new JndiTestUtils();
		this.jndiTestUtils.setUp();
		this.jndiTestUtils.bindComponent(
				"SafeOnline/AuthenticationServiceBean/local",
				this.mockAuthenticationService);

		this.testedInstance.postConstructCallback();
	}

	@Override
	protected void tearDown() throws Exception {
		this.jndiTestUtils.tearDown();
		super.tearDown();
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

	public void testEndpointViaJAXWSRI() throws Exception {
		// setup
		SafeOnlineAuthenticationPort wsPort = new SafeOnlineAuthenticationPortImpl();

		WebServiceTestUtils webServiceTestUtils = new WebServiceTestUtils();
		webServiceTestUtils.setUp(wsPort);

		// operate
		SafeOnlineAuthenticationService service = SafeOnlineAuthenticationServiceFactory
				.newInstance();
		SafeOnlineAuthenticationPort clientPort = service
				.getSafeOnlineAuthenticationPort();
		BindingProvider bindingProvider = (BindingProvider) clientPort;
		String endpointAddress = webServiceTestUtils.getEndpointAddress();
		bindingProvider.getRequestContext().put(
				BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endpointAddress);

		String result = clientPort.echo("hello world");

		// verify
		assertEquals("hello world", result);

		// cleanup
		webServiceTestUtils.tearDown();
	}
}
