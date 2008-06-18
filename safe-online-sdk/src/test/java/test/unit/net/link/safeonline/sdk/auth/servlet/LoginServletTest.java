/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.sdk.auth.servlet;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.InputStream;
import java.util.Collections;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.link.safeonline.sdk.auth.AuthenticationProtocolHandler;
import net.link.safeonline.sdk.auth.AuthenticationProtocolManager;
import net.link.safeonline.sdk.auth.filter.LoginManager;
import net.link.safeonline.sdk.auth.servlet.LoginServlet;
import net.link.safeonline.test.util.DomTestUtils;
import net.link.safeonline.test.util.ServletTestManager;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xpath.XPathAPI;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.tidy.Tidy;

public class LoginServletTest {

	private ServletTestManager servletTestManager;

	private static final Log LOG = LogFactory.getLog(LoginServletTest.class);

	private String protocol = "http";

	@Before
	public void setUp() throws Exception {
		this.servletTestManager = new ServletTestManager();
		Map<String, String> servletInitParams = Collections.singletonMap(
				"Protocol", this.protocol);
		this.servletTestManager.setUp(LoginServlet.class, servletInitParams);
	}

	@After
	public void tearDown() throws Exception {
		this.servletTestManager.tearDown();
	}

	@Test
	public void testNoProtocolHandler() throws Exception {
		// setup
		String location = this.servletTestManager.getServletLocation();
		LOG.debug("servlet location: " + location);

		// operate
		HttpClient httpClient = new HttpClient();
		GetMethod getMethod = new GetMethod(location);
		int statusCode = httpClient.executeMethod(getMethod);

		// verify
		LOG.debug("status code: " + statusCode);
		assertEquals(HttpServletResponse.SC_BAD_REQUEST, statusCode);
		InputStream resultStream = getMethod.getResponseBodyAsStream();

		Tidy tidy = new Tidy();
		tidy.setQuiet(true);
		tidy.setShowWarnings(false);
		Document resultDocument = tidy.parseDOM(resultStream, null);
		LOG.debug("result document: "
				+ DomTestUtils.domToString(resultDocument));
		Node h1Node = XPathAPI.selectSingleNode(resultDocument, "//h1/text()");
		assertNotNull(h1Node);
		assertEquals("Error(s)", h1Node.getNodeValue());
	}

	@Test
	public void testHandlerCannotFinalize() throws Exception {
		// setup
		String location = this.servletTestManager.getServletLocation();
		LOG.debug("servlet location: " + location);
		AuthenticationProtocolHandler mockAuthenticationProtocolHandler = createMock(AuthenticationProtocolHandler.class);
		this.servletTestManager.setSessionAttribute(
				AuthenticationProtocolManager.PROTOCOL_HANDLER_ATTRIBUTE,
				mockAuthenticationProtocolHandler);

		// expectations
		expect(
				mockAuthenticationProtocolHandler.finalizeAuthentication(
						(HttpServletRequest) EasyMock.anyObject(),
						(HttpServletResponse) EasyMock.anyObject())).andReturn(
				null);

		// prepare
		replay(mockAuthenticationProtocolHandler);

		// operate
		HttpClient httpClient = new HttpClient();
		GetMethod getMethod = new GetMethod(location);
		int statusCode = httpClient.executeMethod(getMethod);

		// verify
		verify(mockAuthenticationProtocolHandler);
		LOG.debug("status code: " + statusCode);
		assertEquals(HttpServletResponse.SC_BAD_REQUEST, statusCode);
		String responseBody = getMethod.getResponseBodyAsString();
		LOG.debug("response body: " + responseBody);
	}

	@Test
	public void testLogin() throws Exception {
		// setup
		String location = this.servletTestManager.getServletLocation();
		LOG.debug("servlet location: " + location);
		AuthenticationProtocolHandler mockAuthenticationProtocolHandler = createMock(AuthenticationProtocolHandler.class);
		this.servletTestManager.setSessionAttribute(
				AuthenticationProtocolManager.PROTOCOL_HANDLER_ATTRIBUTE,
				mockAuthenticationProtocolHandler);
		String target = "http://test.target";
		this.servletTestManager.setSessionAttribute(
				AuthenticationProtocolManager.TARGET_ATTRIBUTE, target);
		String username = "test-user-name";

		// expectations
		expect(
				mockAuthenticationProtocolHandler.finalizeAuthentication(
						(HttpServletRequest) EasyMock.anyObject(),
						(HttpServletResponse) EasyMock.anyObject())).andReturn(
				username);

		// prepare
		replay(mockAuthenticationProtocolHandler);

		// operate
		HttpClient httpClient = new HttpClient();
		GetMethod getMethod = new GetMethod(location);
		getMethod.setFollowRedirects(false);
		int statusCode = httpClient.executeMethod(getMethod);

		// verify
		verify(mockAuthenticationProtocolHandler);
		LOG.debug("status code: " + statusCode);
		assertEquals(HttpServletResponse.SC_MOVED_TEMPORARILY, statusCode);
		String responseBody = getMethod.getResponseBodyAsString();
		LOG.debug("response body: " + responseBody);
		String resultUsername = (String) this.servletTestManager
				.getSessionAttribute(LoginManager.USERNAME_SESSION_ATTRIBUTE);
		assertEquals(username, resultUsername);
		String resultTarget = getMethod.getResponseHeader("Location")
				.getValue();
		assertEquals(target, resultTarget);
	}
}
