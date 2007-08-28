/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.webapp.filter;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.io.IOException;
import java.security.Principal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;
import net.link.safeonline.service.AuthorizationService;
import net.link.safeonline.test.util.JndiTestUtils;
import net.link.safeonline.test.util.ServletTestManager;
import net.link.safeonline.webapp.filter.ServletLoginFilter;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ServletLoginFilterTest extends TestCase {

	private static final Log LOG = LogFactory
			.getLog(ServletLoginFilterTest.class);

	private ServletTestManager servletTestManager;

	private JndiTestUtils jndiTestUtils;

	private String username;

	private AuthorizationService mockAuthorizationService;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		this.jndiTestUtils = new JndiTestUtils();
		this.jndiTestUtils.setUp();
		this.mockAuthorizationService = createMock(AuthorizationService.class);
		this.jndiTestUtils.bindComponent(
				"SafeOnline/AuthorizationServiceBean/local",
				this.mockAuthorizationService);

		this.username = "username-" + getName();
		Map<String, Object> initialSessionAttributes = new HashMap<String, Object>();
		initialSessionAttributes.put("username", this.username);

		this.servletTestManager = new ServletTestManager();
		this.servletTestManager.setUp(ServletLoginFilterTestServlet.class,
				ServletLoginFilter.class, null, initialSessionAttributes);

		ServletLoginFilterTestServlet.reset();
	}

	@Override
	protected void tearDown() throws Exception {
		this.servletTestManager.tearDown();
		this.jndiTestUtils.tearDown();

		super.tearDown();
	}

	public void testServletContainerLogin() throws Exception {
		// setup
		String testExpectedRole = "test-role-" + getName();
		ServletLoginFilterTestServlet.addExpectedRole(testExpectedRole);

		HttpClient httpClient = new HttpClient();
		GetMethod getMethod = new GetMethod(this.servletTestManager
				.getServletLocation());

		// stubs
		expect(this.mockAuthorizationService.getRoles(this.username))
				.andStubReturn(Collections.singleton(testExpectedRole));
		replay(this.mockAuthorizationService);

		// operate
		int statusCode = httpClient.executeMethod(getMethod);

		// verify
		verify(this.mockAuthorizationService);
		assertEquals(HttpStatus.SC_OK, statusCode);
		assertTrue(ServletLoginFilterTestServlet.isInvoked());
		LOG.debug("last user principal: "
				+ ServletLoginFilterTestServlet.getLastUserPrincipal());
		assertNotNull(ServletLoginFilterTestServlet.getLastUserPrincipal());
		assertEquals(this.username, ServletLoginFilterTestServlet
				.getLastUserPrincipal().getName());
		assertTrue(ServletLoginFilterTestServlet
				.isExpectedRolePresent(testExpectedRole));
		assertFalse(ServletLoginFilterTestServlet
				.isExpectedRolePresent(testExpectedRole + "-not-expected"));
	}

	public static class ServletLoginFilterTestServlet extends HttpServlet {

		private static final long serialVersionUID = 1L;

		private static final Log LOG = LogFactory
				.getLog(ServletLoginFilterTestServlet.class);

		private static boolean invoked;

		private static Principal lastUserPrincipal;

		private static Map<String, Boolean> expectedRoles;

		public static void reset() {
			ServletLoginFilterTestServlet.invoked = false;
			ServletLoginFilterTestServlet.lastUserPrincipal = null;
			ServletLoginFilterTestServlet.expectedRoles = new HashMap<String, Boolean>();
		}

		public static void addExpectedRole(String expectedRole) {
			ServletLoginFilterTestServlet.expectedRoles
					.put(expectedRole, false);
		}

		public static boolean isExpectedRolePresent(String expectedRole) {
			Boolean expectedRolePresent = ServletLoginFilterTestServlet.expectedRoles
					.get(expectedRole);
			if (null == expectedRolePresent) {
				return false;
			}
			return expectedRolePresent;
		}

		public static boolean isInvoked() {
			return ServletLoginFilterTestServlet.invoked;
		}

		public static Principal getLastUserPrincipal() {
			return ServletLoginFilterTestServlet.lastUserPrincipal;
		}

		@Override
		protected void doGet(HttpServletRequest request,
				HttpServletResponse response) throws ServletException,
				IOException {
			LOG.debug("doGet");
			ServletLoginFilterTestServlet.invoked = true;
			Principal principal = request.getUserPrincipal();
			LOG.debug("user principal: " + principal);
			ServletLoginFilterTestServlet.lastUserPrincipal = principal;

			for (String expectedRole : ServletLoginFilterTestServlet.expectedRoles
					.keySet()) {
				boolean rolePresent = request.isUserInRole(expectedRole);
				ServletLoginFilterTestServlet.expectedRoles.put(expectedRole,
						rolePresent);
			}
		}
	}
}
