/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.webapp.filter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.link.safeonline.test.util.ServletTestManager;
import net.link.safeonline.webapp.filter.TimeoutFilter;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TimeoutFilterTest {

	private static final Log LOG = LogFactory.getLog(TimeoutFilterTest.class);

	private ServletTestManager servletTestManager;

	@Before
	public void setUp() throws Exception {
		this.servletTestManager = new ServletTestManager();
		Map<String, String> filterInitParameters = new HashMap<String, String>();
		filterInitParameters.put("TimeoutPath", "timeout");
		filterInitParameters.put("LoginSessionAttribute", "username");
		Map<String, Object> initialSessionAttributes = new HashMap<String, Object>();
		initialSessionAttributes.put("username", "alice");
		this.servletTestManager.setUp(TestServlet.class, TimeoutFilter.class,
				filterInitParameters, initialSessionAttributes);
	}

	@After
	public void tearDown() throws Exception {
		this.servletTestManager.tearDown();
	}

	public static class TestServlet extends HttpServlet {

		private static final long serialVersionUID = 1L;

		private static final Log LOG = LogFactory.getLog(TestServlet.class);

		@Override
		protected void doGet(HttpServletRequest request,
				HttpServletResponse response) throws ServletException,
				IOException {
			LOG.debug("writing to print writer");

			HttpSession session = request.getSession();
			String username = (String) session.getAttribute("username");
			LOG.debug("username: " + username);

			PrintWriter out = response.getWriter();
			out.println("hello world: " + username);
		}
	}

	@Test
	public void testGet() throws Exception {
		// setup
		HttpClient httpClient = new HttpClient();
		GetMethod getMethod = new GetMethod(this.servletTestManager
				.getServletLocation());
		int statusCode;
		String body;

		// operate
		LOG.debug("operate: create new session");
		statusCode = httpClient.executeMethod(getMethod);

		// verify
		assertEquals(HttpServletResponse.SC_OK, statusCode);
		logHeaders(getMethod);
		body = getMethod.getResponseBodyAsString();
		LOG.debug("body: " + body);
		assertTrue(body.startsWith("hello world: alice"));
		// session cookie
		assertNotNull(getMethod.getResponseHeader("Set-Cookie"));

		// operate
		LOG.debug("operate: continue on existing session");
		statusCode = httpClient.executeMethod(getMethod);
		logHeaders(getMethod);
		// login cookie
		body = getMethod.getResponseBodyAsString();
		LOG.debug("body: " + body);
		assertTrue(body.startsWith("hello world: alice"));
		assertNotNull(getMethod.getResponseHeader("Set-Cookie"));
	}

	private void logHeaders(GetMethod getMethod) {
		Header[] headers = getMethod.getResponseHeaders();
		for (Header header : headers) {
			LOG
					.debug("header: " + header.getName() + " = "
							+ header.getValue());
		}
	}
}
