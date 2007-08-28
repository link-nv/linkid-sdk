/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.sdk.auth;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertNotNull;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import net.link.safeonline.sdk.auth.AuthenticationProtocol;
import net.link.safeonline.sdk.auth.AuthenticationProtocolHandler;
import net.link.safeonline.sdk.auth.AuthenticationProtocolManager;

import org.junit.Before;
import org.junit.Test;

public class AuthenticationProtocolManagerTest {

	private HttpServletRequest mockHttpServletRequest;

	private HttpSession mockHttpSession;

	private Object[] mockObjects;

	@Before
	public void setUp() throws Exception {
		this.mockHttpServletRequest = createMock(HttpServletRequest.class);
		this.mockHttpSession = createMock(HttpSession.class);

		this.mockObjects = new Object[] { this.mockHttpServletRequest,
				this.mockHttpSession };

		// stubs
		expect(this.mockHttpServletRequest.getSession()).andStubReturn(
				this.mockHttpSession);
	}

	@Test
	public void simpleProtocolHandler() throws Exception {
		// expectations
		expect(
				this.mockHttpSession
						.getAttribute(AuthenticationProtocolManager.PROTOCOL_HANDLER_ATTRIBUTE))
				.andReturn(null);
		this.mockHttpSession.setAttribute(
				eq(AuthenticationProtocolManager.PROTOCOL_HANDLER_ATTRIBUTE),
				anyObject());

		// prepare
		replay(this.mockObjects);

		// operate
		AuthenticationProtocolHandler simpleAuthenticationProtocolHandler = AuthenticationProtocolManager
				.createAuthenticationProtocolHandler(
						AuthenticationProtocol.SIMPLE_PLAIN_URL,
						"http://authn.service", "app-name", null, null,
						this.mockHttpServletRequest);

		// verify
		verify(this.mockObjects);
		assertNotNull(simpleAuthenticationProtocolHandler);
	}

	@Test
	public void saml2ProtocolHandler() throws Exception {
		// expectations
		expect(
				this.mockHttpSession
						.getAttribute(AuthenticationProtocolManager.PROTOCOL_HANDLER_ATTRIBUTE))
				.andReturn(null);
		this.mockHttpSession.setAttribute(
				eq(AuthenticationProtocolManager.PROTOCOL_HANDLER_ATTRIBUTE),
				anyObject());

		// prepare
		replay(this.mockObjects);

		// operate
		AuthenticationProtocolHandler saml2AuthenticationProtocolHandler = AuthenticationProtocolManager
				.createAuthenticationProtocolHandler(
						AuthenticationProtocol.SAML2_BROWSER_POST,
						"http://authn.service", "application-id", null, null,
						this.mockHttpServletRequest);

		// verify
		verify(this.mockObjects);
		assertNotNull(saml2AuthenticationProtocolHandler);
	}
}
