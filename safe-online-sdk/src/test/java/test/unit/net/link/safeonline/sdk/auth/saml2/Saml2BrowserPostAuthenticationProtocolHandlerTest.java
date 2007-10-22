/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.sdk.auth.saml2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.lang.reflect.Field;
import java.net.URL;
import java.security.KeyPair;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.link.safeonline.sdk.auth.AuthenticationProtocol;
import net.link.safeonline.sdk.auth.AuthenticationProtocolHandler;
import net.link.safeonline.sdk.auth.AuthenticationProtocolManager;
import net.link.safeonline.sdk.auth.saml2.Challenge;
import net.link.safeonline.sdk.auth.saml2.Saml2BrowserPostAuthenticationProtocolHandler;
import net.link.safeonline.test.util.PkiTestUtils;
import net.link.safeonline.test.util.ServletTestManager;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class Saml2BrowserPostAuthenticationProtocolHandlerTest {

	private static final Log LOG = LogFactory
			.getLog(Saml2BrowserPostAuthenticationProtocolHandlerTest.class);

	private ServletTestManager requestServletTestManager;

	private ServletTestManager responseServletTestManager;

	@Before
	public void setUp() throws Exception {
		this.requestServletTestManager = new ServletTestManager();
		this.requestServletTestManager.setUp(SamlRequestTestServlet.class);

		this.responseServletTestManager = new ServletTestManager();
		this.responseServletTestManager.setUp(SamlResponseTestServlet.class);
	}

	@After
	public void tearDown() throws Exception {
		this.requestServletTestManager.tearDown();
		this.responseServletTestManager.tearDown();
	}

	public static class SamlRequestTestServlet extends HttpServlet {

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
					.createAuthenticationProtocolHandler(
							AuthenticationProtocol.SAML2_BROWSER_POST,
							"http://test.authn.service", "test-application",
							keyPair, null, request);
			authenticationProtocolHandler.initiateAuthentication(request,
					response, "http://target");
		}
	}

	public static class SamlResponseTestServlet extends HttpServlet {

		private static final long serialVersionUID = 1L;

		private static final Log srtLOG = LogFactory
				.getLog(SamlResponseTestServlet.class);

		@SuppressWarnings("unchecked")
		@Override
		protected void doPost(HttpServletRequest request,
				HttpServletResponse response) throws ServletException,
				IOException {
			srtLOG.debug("doGet");
			AuthenticationProtocolHandler authenticationProtocolHandler = AuthenticationProtocolManager
					.createAuthenticationProtocolHandler(
							AuthenticationProtocol.SAML2_BROWSER_POST,
							"http://test.authn.service", "test-application",
							null, null, request);
			Saml2BrowserPostAuthenticationProtocolHandler saml2Handler = (Saml2BrowserPostAuthenticationProtocolHandler) authenticationProtocolHandler;
			try {
				Field challengeField = Saml2BrowserPostAuthenticationProtocolHandler.class
						.getDeclaredField("challenge");
				challengeField.setAccessible(true);
				Challenge<String> challenge = (Challenge<String>) challengeField
						.get(saml2Handler);
				challenge.setValue("test-in-response-to");
			} catch (Exception e) {
				throw new ServletException("reflection error: "
						+ e.getMessage(), e);
			}
			String username = authenticationProtocolHandler
					.finalizeAuthentication(request, response);
			if (null != username) {
				HttpSession session = request.getSession();
				session.setAttribute("username", username);
			}
			Writer out = response.getWriter();
			out.write("username: " + username);
			out.flush();
		}
	}

	@Test
	public void doGet() throws Exception {
		// setup
		LOG.debug("test doGet");
		HttpClient httpClient = new HttpClient();
		GetMethod getMethod = new GetMethod(this.requestServletTestManager
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

	@Test
	public void testResponseHandling() throws Exception {
		// setup
		HttpClient httpClient = new HttpClient();
		PostMethod postMethod = new PostMethod(this.responseServletTestManager
				.getServletLocation());

		InputStream xmlInputStream = Saml2BrowserPostAuthenticationProtocolHandlerTest.class
				.getResourceAsStream("/test-saml-response.xml");
		String xmlInputString = IOUtils.toString(xmlInputStream);
		DateTime now = new DateTime();
		xmlInputString = replaceAll("replaceWithCurrentTime", now.toString(),
				xmlInputString);
		xmlInputString = replaceAll("replaceWithCurrentPlusValidityTime", now
				.plusMinutes(10).toString(), xmlInputString);
		ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
		IOUtils.copy(IOUtils.toInputStream(xmlInputString), byteOutputStream);
		byte[] samlResponse = byteOutputStream.toByteArray();
		byte[] encodedSamlResponse = Base64.encodeBase64(samlResponse);
		NameValuePair[] data = { new NameValuePair("SAMLResponse", new String(
				encodedSamlResponse)) };
		postMethod.setRequestBody(data);

		// operate
		int statusCode = httpClient.executeMethod(postMethod);

		// verify
		LOG.debug("status code: " + statusCode);
		assertEquals(HttpStatus.SC_OK, statusCode);
		String responseBody = postMethod.getResponseBodyAsString();
		LOG.debug("response body: \"" + responseBody + "\"");
		String username = (String) this.responseServletTestManager
				.getSessionAttribute("username");
		LOG.debug("authenticated username: " + username);
		assertNotNull(username);
	}

	private static String replaceAll(String oldStr, String newStr,
			String inString) {
		int start;
		String resultString = inString;
		while (true) {
			start = resultString.indexOf(oldStr);
			if (start == -1)
				break;
			StringBuffer sb = new StringBuffer();
			sb.append(resultString.substring(0, start));
			sb.append(newStr);
			sb.append(resultString.substring(start + oldStr.length()));
			resultString = sb.toString();
		}
		return resultString;
	}
}
