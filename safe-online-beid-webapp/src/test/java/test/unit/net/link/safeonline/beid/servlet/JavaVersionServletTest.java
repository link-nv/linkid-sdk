/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.beid.servlet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import javax.servlet.http.HttpServletResponse;

import net.link.safeonline.beid.servlet.JavaVersionServlet;
import net.link.safeonline.test.util.ServletTestManager;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class JavaVersionServletTest {

	private static final Log LOG = LogFactory
			.getLog(JavaVersionServletTest.class);

	private ServletTestManager servletTestManager;

	private String location;

	@Before
	public void setUp() throws Exception {
		this.servletTestManager = new ServletTestManager();
		this.servletTestManager.setUp(JavaVersionServlet.class);
		this.location = this.servletTestManager.getServletLocation();
	}

	@After
	public void tearDown() throws Exception {
		this.servletTestManager.tearDown();
	}

	@Test
	public void testLinux() throws Exception {
		// setup
		HttpClient httpClient = new HttpClient();
		PostMethod postMethod = new PostMethod(this.location);
		postMethod.addParameter("platform", "Linux i686");

		// operate
		int statusCode = httpClient.executeMethod(postMethod);
		LOG.debug("status code: " + statusCode);
		assertEquals(HttpServletResponse.SC_MOVED_TEMPORARILY, statusCode);
		assertEquals(JavaVersionServlet.PLATFORM.LINUX, this.servletTestManager
				.getSessionAttribute("platform"));
		String resultLocation = postMethod.getResponseHeader("Location")
				.getValue();
		LOG.debug("result location: " + resultLocation);
		assertEquals(this.location + "beid-applet.seam", resultLocation);
	}

	@Test
	public void testWindows() throws Exception {
		// setup
		HttpClient httpClient = new HttpClient();
		PostMethod postMethod = new PostMethod(this.location);
		postMethod.addParameter("platform", "Win32");

		// operate
		int statusCode = httpClient.executeMethod(postMethod);
		LOG.debug("status code: " + statusCode);
		assertEquals(HttpServletResponse.SC_MOVED_TEMPORARILY, statusCode);
		assertEquals(JavaVersionServlet.PLATFORM.WINDOWS,
				this.servletTestManager.getSessionAttribute("platform"));
		String resultLocation = postMethod.getResponseHeader("Location")
				.getValue();
		LOG.debug("result location: " + resultLocation);
		assertEquals(this.location + "beid-applet.seam", resultLocation);
	}

	@Test
	public void testMacIntel() throws Exception {
		// setup
		HttpClient httpClient = new HttpClient();
		PostMethod postMethod = new PostMethod(this.location);
		postMethod.addParameter("platform", "MacIntel");

		// operate
		int statusCode = httpClient.executeMethod(postMethod);
		LOG.debug("status code: " + statusCode);
		assertEquals(HttpServletResponse.SC_MOVED_TEMPORARILY, statusCode);
		assertEquals(JavaVersionServlet.PLATFORM.MAC, this.servletTestManager
				.getSessionAttribute("platform"));
		String resultLocation = postMethod.getResponseHeader("Location")
				.getValue();
		LOG.debug("result location: " + resultLocation);
		assertEquals(this.location + "beid-applet.seam", resultLocation);
	}

	@Test
	public void testUnsupportedPlatform() throws Exception {
		// setup
		HttpClient httpClient = new HttpClient();
		PostMethod postMethod = new PostMethod(this.location);
		postMethod.addParameter("platform", "GameBoy");

		// operate
		int statusCode = httpClient.executeMethod(postMethod);
		LOG.debug("status code: " + statusCode);
		assertEquals(HttpServletResponse.SC_MOVED_TEMPORARILY, statusCode);
		assertNull(this.servletTestManager.getSessionAttribute("platform"));
		String resultLocation = postMethod.getResponseHeader("Location")
				.getValue();
		LOG.debug("result location: " + resultLocation);
		assertEquals(this.location + "unsupported-platform.seam",
				resultLocation);
	}
}
