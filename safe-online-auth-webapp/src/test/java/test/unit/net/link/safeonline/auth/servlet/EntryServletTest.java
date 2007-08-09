/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.auth.servlet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import net.link.safeonline.auth.servlet.EntryServlet;
import net.link.safeonline.test.util.ServletTestManager;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class EntryServletTest {

	private static final Log LOG = LogFactory.getLog(EntryServletTest.class);

	private ServletTestManager servletTestManager;

	@Before
	public void setUp() throws Exception {
		this.servletTestManager = new ServletTestManager();
		this.servletTestManager.setUp(EntryServlet.class);
	}

	@After
	public void tearDown() throws Exception {
		this.servletTestManager.tearDown();
	}

	@Test
	public void unsupportedAuthenticationProtocol() throws Exception {
		// setup
		HttpClient httpClient = new HttpClient();
		GetMethod getMethod = new GetMethod(this.servletTestManager
				.getServletLocation());
		getMethod.setFollowRedirects(false);

		// operate
		int statusCode = httpClient.executeMethod(getMethod);

		// verify
		LOG.debug("status code: " + statusCode);
		assertEquals(HttpStatus.SC_MOVED_TEMPORARILY, statusCode);
		String location = getMethod.getResponseHeader("Location").getValue();
		LOG.debug("location: " + location);
		assertTrue(location.endsWith("/unsupported-protocol.seam"));
	}

	@Test
	public void simpleAuthenticationProtocol() throws Exception {
		// setup
		HttpClient httpClient = new HttpClient();
		GetMethod getMethod = new GetMethod(this.servletTestManager
				.getServletLocation());
		getMethod.setFollowRedirects(false);
		String applicationId = "test-application-id";
		String target = "http://test.target";
		getMethod.setQueryString(new NameValuePair[] {
				new NameValuePair("application", applicationId),
				new NameValuePair("target", target) });

		// operate
		int statusCode = httpClient.executeMethod(getMethod);

		// verify
		LOG.debug("status code: " + statusCode);
		assertEquals(HttpStatus.SC_MOVED_TEMPORARILY, statusCode);
		String location = getMethod.getResponseHeader("Location").getValue();
		LOG.debug("location: " + location);
		assertTrue(location.endsWith("/main.seam"));
		String resultApplicationId = (String) this.servletTestManager
				.getSessionAttribute("applicationId");
		assertEquals(applicationId, resultApplicationId);
		String resultTarget = (String) this.servletTestManager
				.getSessionAttribute("target");
		assertEquals(target, resultTarget);
	}
}
