/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.auth.ws;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Endpoint;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpServer;

import junit.framework.TestCase;
import net.lin_k.safe_online.auth._1.SafeOnlineAuthenticationPort;
import net.lin_k.safe_online.auth._1.SafeOnlineAuthenticationService;
import net.lin_k.safe_online.auth._1_0.types.AuthenticateRequestType;
import net.lin_k.safe_online.auth._1_0.types.AuthenticateResultType;
import net.link.safeonline.auth.ws.SafeOnlineAuthenticationPortImpl;
import net.link.safeonline.auth.ws.SafeOnlineAuthenticationServiceFactory;
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
		this.testedInstance.postConstructCallback();
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

	public void __testEndpointViaJAXWSRI() throws Exception {
		// setup
		SafeOnlineAuthenticationPort wsPort = new SafeOnlineAuthenticationPortImpl();
		Endpoint endpoint = Endpoint.create(wsPort);

		HttpServer httpServer = HttpServer.create();
		int port = getFreePort();
		httpServer.bind(new InetSocketAddress(port), 5);
		ExecutorService executorService = Executors.newFixedThreadPool(5);
		httpServer.setExecutor(executorService);
		httpServer.start();

		HttpContext httpContext = httpServer.createContext("/test");
		endpoint.publish(httpContext);

		// operate
		SafeOnlineAuthenticationService service = SafeOnlineAuthenticationServiceFactory
				.newInstance();
		SafeOnlineAuthenticationPort clientPort = service
				.getSafeOnlineAuthenticationPort();
		BindingProvider bindingProvider = (BindingProvider) clientPort;
		bindingProvider.getRequestContext().put(
				BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
				"http://localhost:" + port + "/test");

		String result = clientPort.echo("hello world");

		// verify
		assertEquals("hello world", result);

		// cleanup
		endpoint.stop();
		httpServer.stop(1);
		executorService.shutdown();
	}

	private static int getFreePort() throws Exception {
		ServerSocket serverSocket = new ServerSocket(0);
		int port = serverSocket.getLocalPort();
		serverSocket.close();
		return port;
	}
}
