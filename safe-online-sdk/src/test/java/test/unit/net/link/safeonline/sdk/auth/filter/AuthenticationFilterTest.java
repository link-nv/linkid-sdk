/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.sdk.auth.filter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.link.safeonline.sdk.auth.filter.AuthenticationFilter;
import net.link.safeonline.test.util.PkiTestUtils;
import net.link.safeonline.test.util.ServletTestManager;
import net.link.safeonline.test.util.TestClassLoader;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class AuthenticationFilterTest {

	private static final Log LOG = LogFactory
			.getLog(AuthenticationFilterTest.class);

	private ServletTestManager servletTestManager;

	private ClassLoader originalContextClassLoader;

	private TestClassLoader testClassLoader;

	@Before
	public void setUp() throws Exception {
		this.originalContextClassLoader = Thread.currentThread()
				.getContextClassLoader();
		this.testClassLoader = new TestClassLoader();
		Thread.currentThread().setContextClassLoader(this.testClassLoader);
		this.servletTestManager = new ServletTestManager();
	}

	@After
	public void tearDown() throws Exception {
		this.servletTestManager.tearDown();
		Thread.currentThread().setContextClassLoader(
				this.originalContextClassLoader);
	}

	public static class TestServlet extends HttpServlet {

		private static final long serialVersionUID = 1L;

		@Override
		protected void doGet(HttpServletRequest request,
				HttpServletResponse response) throws ServletException,
				IOException {
			throw new ServletException("should never get called");
		}
	}

	@Test
	public void performSimpleAuthnRequest() throws Exception {
		// setup
		Map<String, String> filterInitParameters = new HashMap<String, String>();
		filterInitParameters.put("SafeOnlineAuthenticationServiceUrl",
				"http://authn.service");
		filterInitParameters.put("ApplicationName", "application-id");
		Map<String, String> initialSessionAttributes = new HashMap<String, String>();
		this.servletTestManager.setUp(TestServlet.class,
				AuthenticationFilter.class, filterInitParameters,
				initialSessionAttributes);

		GetMethod getMethod = new GetMethod(this.servletTestManager
				.getServletLocation());
		getMethod.setFollowRedirects(false);
		HttpClient httpClient = new HttpClient();

		// operate
		int statusCode = httpClient.executeMethod(getMethod);

		// verify
		LOG.debug("status code: " + statusCode);
		assertEquals(HttpStatus.SC_MOVED_TEMPORARILY, statusCode);
		String location = getMethod.getResponseHeader("Location").getValue();
		assertNotNull(location);
		LOG.debug("result Location: " + location);
		assertTrue(location.startsWith("http://authn.service"));
	}

	@Test
	public void performSaml2AuthnRequest() throws Exception {
		// setup
		KeyPair keyPair = PkiTestUtils.generateKeyPair();
		X509Certificate cert = PkiTestUtils.generateSelfSignedCertificate(
				keyPair, "CN=TestApplication");
		File tmpP12File = File.createTempFile("application-", ".p12");
		tmpP12File.deleteOnExit();
		PkiTestUtils.persistKey(tmpP12File, keyPair.getPrivate(), cert,
				"secret", "secret");

		String p12ResourceName = "p12-resource-name.p12";
		this.testClassLoader.addResource(p12ResourceName, tmpP12File.toURL());

		Map<String, String> filterInitParameters = new HashMap<String, String>();
		filterInitParameters.put("SafeOnlineAuthenticationServiceUrl",
				"http://authn.service");
		filterInitParameters.put("ApplicationName", "application-id");
		filterInitParameters
				.put("AuthenticationProtocol", "SAML2_BROWSER_POST");
		filterInitParameters.put("KeyStoreResource", p12ResourceName);
		filterInitParameters.put("KeyStorePassword", "secret");
		Map<String, String> initialSessionAttributes = new HashMap<String, String>();
		this.servletTestManager.setUp(TestServlet.class,
				AuthenticationFilter.class, filterInitParameters,
				initialSessionAttributes);

		GetMethod getMethod = new GetMethod(this.servletTestManager
				.getServletLocation());
		HttpClient httpClient = new HttpClient();

		// operate
		int statusCode = httpClient.executeMethod(getMethod);

		// verify
		LOG.debug("status code: " + statusCode);
		assertEquals(HttpStatus.SC_OK, statusCode);
		String response = getMethod.getResponseBodyAsString();
		LOG.debug("response body: " + response);
	}
}
