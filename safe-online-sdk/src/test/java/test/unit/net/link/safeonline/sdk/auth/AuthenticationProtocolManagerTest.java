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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.link.safeonline.sdk.auth.AuthenticationProtocol;
import net.link.safeonline.sdk.auth.AuthenticationProtocolHandler;
import net.link.safeonline.sdk.auth.AuthenticationProtocolManager;
import net.link.safeonline.test.util.ServletTestManager;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

public class AuthenticationProtocolManagerTest {

	static final Log LOG = LogFactory
			.getLog(AuthenticationProtocolManagerTest.class);

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
						"http://authn.service", "app-name", null, null, null,
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
						null, this.mockHttpServletRequest);

		// verify
		verify(this.mockObjects);
		assertNotNull(saml2AuthenticationProtocolHandler);
	}

	@Test
	public void testInitiateAuthenticationWithoutLandingPage() throws Exception {
		ServletTestManager servletTestManager = new ServletTestManager();
		AuthenticationProtocolHandler mockProtocolHandler = createMock(AuthenticationProtocolHandler.class);
		servletTestManager.setUp(TestServlet.class);
		servletTestManager.setSessionAttribute(
				AuthenticationProtocolManager.PROTOCOL_HANDLER_ATTRIBUTE,
				mockProtocolHandler);
		try {
			String location = servletTestManager.getServletLocation();
			mockProtocolHandler.initiateAuthentication(
					(HttpServletRequest) EasyMock.anyObject(),
					(HttpServletResponse) EasyMock.anyObject(), EasyMock
							.eq(location));
			replay(mockProtocolHandler);
			LOG.debug("servlet location: " + location);
			HttpClient httpClient = new HttpClient();
			GetMethod getMethod = new GetMethod(location);
			int statusCode = httpClient.executeMethod(getMethod);
			LOG.debug("status code: " + statusCode);
			verify(mockProtocolHandler);
			assertEquals(HttpStatus.SC_OK, statusCode);
			assertNull(servletTestManager
					.getSessionAttribute(AuthenticationProtocolManager.TARGET_ATTRIBUTE));
		} finally {
			servletTestManager.tearDown();
		}
	}

	@Test
	public void testInitiateAuthenticationWithLandingPage() throws Exception {
		ServletTestManager servletTestManager = new ServletTestManager();
		AuthenticationProtocolHandler mockProtocolHandler = createMock(AuthenticationProtocolHandler.class);
		Map<String, String> initParams = new HashMap<String, String>();
		String landingPage = "login";
		initParams.put(AuthenticationProtocolManager.LANDING_PAGE_INIT_PARAM,
				"login");
		servletTestManager.setUp(TestServlet.class, initParams);
		servletTestManager.setSessionAttribute(
				AuthenticationProtocolManager.PROTOCOL_HANDLER_ATTRIBUTE,
				mockProtocolHandler);
		try {
			String location = servletTestManager.getServletLocation();
			mockProtocolHandler.initiateAuthentication(
					(HttpServletRequest) EasyMock.anyObject(),
					(HttpServletResponse) EasyMock.anyObject(), EasyMock
							.eq(landingPage));
			replay(mockProtocolHandler);
			LOG.debug("servlet location: " + location);
			HttpClient httpClient = new HttpClient();
			GetMethod getMethod = new GetMethod(location);
			int statusCode = httpClient.executeMethod(getMethod);
			LOG.debug("status code: " + statusCode);
			verify(mockProtocolHandler);
			assertEquals(HttpStatus.SC_OK, statusCode);
			assertEquals(
					location,
					servletTestManager
							.getSessionAttribute(AuthenticationProtocolManager.TARGET_ATTRIBUTE));
		} finally {
			servletTestManager.tearDown();
		}
	}

	public static class TestServlet extends HttpServlet {

		private static final long serialVersionUID = 1L;

		private static final Log SERVLET_LOG = LogFactory
				.getLog(TestServlet.class);

		@Override
		public void init(ServletConfig config) throws ServletException {
			super.init(config);
			SERVLET_LOG
					.debug("init: landing page init param: "
							+ config
									.getInitParameter(AuthenticationProtocolManager.LANDING_PAGE_INIT_PARAM));
		}

		@Override
		protected void doGet(HttpServletRequest request,
				HttpServletResponse response) throws ServletException,
				IOException {
			HttpSession session = request.getSession();
			ServletContext servletContext = session.getServletContext();
			SERVLET_LOG
					.debug("doGet: landing page init param: "
							+ servletContext
									.getInitParameter(AuthenticationProtocolManager.LANDING_PAGE_INIT_PARAM));
			AuthenticationProtocolManager.initiateAuthentication(request,
					response);
		}
	}
}
