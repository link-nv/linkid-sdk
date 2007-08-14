/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.sdk.auth.saml2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.security.KeyPair;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.link.safeonline.sdk.auth.AuthenticationProtocol;
import net.link.safeonline.sdk.auth.AuthenticationProtocolHandler;
import net.link.safeonline.sdk.auth.AuthenticationProtocolManager;
import net.link.safeonline.sdk.auth.saml2.Saml2BrowserPostAuthenticationProtocolHandler;
import net.link.safeonline.test.util.PkiTestUtils;
import net.link.safeonline.test.util.ServletTestManager;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class Saml2BrowserPostAuthenticationProtocolHandlerTest {

	private static final Log LOG = LogFactory
			.getLog(Saml2BrowserPostAuthenticationProtocolHandlerTest.class);

	private ServletTestManager servletTestManager;

	@Before
	public void setUp() throws Exception {
		this.servletTestManager = new ServletTestManager();
		this.servletTestManager.setUp(TestServlet.class);
	}

	@After
	public void tearDown() throws Exception {
		this.servletTestManager.tearDown();
	}

	public static class TestServlet extends HttpServlet {

		private static final long serialVersionUID = 1L;

		@Override
		protected void doGet(HttpServletRequest request,
				HttpServletResponse response) throws ServletException,
				IOException {
			KeyPair keyPair;
			try {
				keyPair = PkiTestUtils.generateKeyPair();
			} catch (Exception e) {
				throw new ServletException("could not generate RSA key pair");
			}
			AuthenticationProtocolHandler authenticationProtocolHandler = AuthenticationProtocolManager
					.getAuthenticationProtocolHandler(
							AuthenticationProtocol.SAML2_BROWSER_POST,
							"http://test.authn.service", "test-application",
							keyPair, null);
			authenticationProtocolHandler.initiateAuthentication(request,
					response, "http://target");
		}
	}

	@Test
	public void doGet() throws Exception {
		// setup
		LOG.debug("test doGet");
		HttpClient httpClient = new HttpClient();
		GetMethod getMethod = new GetMethod(this.servletTestManager
				.getServletLocation());

		// operate
		int resultStatusCode = httpClient.executeMethod(getMethod);

		// verify
		LOG.debug("result status code: " + resultStatusCode);
		assertEquals(HttpStatus.SC_OK, resultStatusCode);
		String response = getMethod.getResponseBodyAsString();
		LOG.debug("response body: " + response);

		File tmpFile = File.createTempFile("saml-post-request-", ".html");
		IOUtils.write(response, new FileOutputStream(tmpFile));
	}

	@Test
	public void defaultVelocityMacroResource() throws Exception {
		// operate
		URL result = Saml2BrowserPostAuthenticationProtocolHandler.class
				.getResource(Saml2BrowserPostAuthenticationProtocolHandler.SAML2_POST_BINDING_VM_RESOURCE);

		// verify
		assertNotNull(result);
	}
}
