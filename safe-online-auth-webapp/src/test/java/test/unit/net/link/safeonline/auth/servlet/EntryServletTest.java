/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.auth.servlet;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.auth.protocol.SimpleProtocolHandler;
import net.link.safeonline.auth.protocol.saml2.Saml2PostProtocolHandler;
import net.link.safeonline.auth.servlet.EntryServlet;
import net.link.safeonline.authentication.service.ApplicationAuthenticationService;
import net.link.safeonline.pkix.model.PkiValidator;
import net.link.safeonline.sdk.auth.saml2.AuthnRequestFactory;
import net.link.safeonline.test.util.JmxTestUtils;
import net.link.safeonline.test.util.JndiTestUtils;
import net.link.safeonline.test.util.PkiTestUtils;
import net.link.safeonline.test.util.ServletTestManager;
import net.link.safeonline.util.ee.IdentityServiceClient;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xml.security.utils.Base64;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class EntryServletTest {

	private static final Log LOG = LogFactory.getLog(EntryServletTest.class);

	private ServletTestManager entryServletTestManager;

	private String startUrl = "start";

	private String unsupportedProtocolUrl = "unsupported-protocol";

	private String protocolErrorUrl = "protocol-error";

	private JndiTestUtils jndiTestUtils;

	private ApplicationAuthenticationService mockApplicationAuthenticationService;

	private PkiValidator mockPkiValidator;

	private Object[] mockObjects;

	@Before
	public void setUp() throws Exception {
		this.jndiTestUtils = new JndiTestUtils();
		this.jndiTestUtils.setUp();
		this.mockApplicationAuthenticationService = createMock(ApplicationAuthenticationService.class);
		this.jndiTestUtils.bindComponent(
				"SafeOnline/ApplicationAuthenticationServiceBean/local",
				this.mockApplicationAuthenticationService);
		this.mockPkiValidator = createMock(PkiValidator.class);
		this.jndiTestUtils.bindComponent("SafeOnline/PkiValidatorBean/local",
				this.mockPkiValidator);

		JmxTestUtils jmxTestUtils = new JmxTestUtils();
		jmxTestUtils.setUp(IdentityServiceClient.IDENTITY_SERVICE);

		this.entryServletTestManager = new ServletTestManager();
		Map<String, String> initParams = new HashMap<String, String>();
		initParams.put("StartUrl", this.startUrl);
		initParams.put("UnsupportedProtocolUrl", this.unsupportedProtocolUrl);
		initParams.put("ProtocolErrorUrl", this.protocolErrorUrl);
		this.entryServletTestManager.setUp(EntryServlet.class, initParams);

		this.mockObjects = new Object[] {
				this.mockApplicationAuthenticationService,
				this.mockPkiValidator };
	}

	@After
	public void tearDown() throws Exception {
		this.entryServletTestManager.tearDown();
		this.jndiTestUtils.tearDown();
	}

	@Test
	public void unsupportedAuthenticationProtocol() throws Exception {
		// setup
		HttpClient httpClient = new HttpClient();
		GetMethod getMethod = new GetMethod(this.entryServletTestManager
				.getServletLocation());
		/*
		 * Here we simulate a user that directly visits the authentication web
		 * application.
		 */
		getMethod.setFollowRedirects(false);

		// operate
		int statusCode = httpClient.executeMethod(getMethod);

		// verify
		LOG.debug("status code: " + statusCode);
		assertEquals(HttpStatus.SC_MOVED_TEMPORARILY, statusCode);
		String location = getMethod.getResponseHeader("Location").getValue();
		LOG.debug("location: " + location);
		assertTrue(location.endsWith(this.unsupportedProtocolUrl));
	}

	@Test
	public void simpleAuthenticationProtocol() throws Exception {
		// setup
		HttpClient httpClient = new HttpClient();
		GetMethod getMethod = new GetMethod(this.entryServletTestManager
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
		assertTrue(location.endsWith(this.startUrl));
		String resultApplicationId = (String) this.entryServletTestManager
				.getSessionAttribute("applicationId");
		assertEquals(applicationId, resultApplicationId);
		String resultTarget = (String) this.entryServletTestManager
				.getSessionAttribute("target");
		assertEquals(target, resultTarget);
	}

	@Test
	public void simpleAuthenticationProtocolHasMissingTarget() throws Exception {
		// setup
		HttpClient httpClient = new HttpClient();
		GetMethod getMethod = new GetMethod(this.entryServletTestManager
				.getServletLocation());
		getMethod.setFollowRedirects(false);
		String applicationId = "test-application-id";
		getMethod.setQueryString(new NameValuePair[] { new NameValuePair(
				"application", applicationId) });

		// operate
		int statusCode = httpClient.executeMethod(getMethod);

		// verify
		LOG.debug("status code: " + statusCode);
		assertEquals(HttpStatus.SC_MOVED_TEMPORARILY, statusCode);
		String location = getMethod.getResponseHeader("Location").getValue();
		LOG.debug("location: " + location);
		assertTrue(location.endsWith(this.protocolErrorUrl));

		String resultProtocolErrorMessage = (String) this.entryServletTestManager
				.getSessionAttribute("protocolErrorMessage");
		assertNotNull(resultProtocolErrorMessage);
		assertTrue(resultProtocolErrorMessage.indexOf("target") != -1);
		String resultProtocolName = (String) this.entryServletTestManager
				.getSessionAttribute("protocolName");
		assertNotNull(resultProtocolName);
		assertEquals(SimpleProtocolHandler.NAME, resultProtocolName);
	}

	@Test
	public void saml2AuthenticationProtocol() throws Exception {
		// setup
		HttpClient httpClient = new HttpClient();
		PostMethod postMethod = new PostMethod(this.entryServletTestManager
				.getServletLocation());

		KeyPair applicationKeyPair = PkiTestUtils.generateKeyPair();
		X509Certificate applicationCert = PkiTestUtils
				.generateSelfSignedCertificate(applicationKeyPair,
						"CN=TestApplication");
		String applicationName = "test-application-id";
		String assertionConsumerService = "http://test.assertion.consumer.service";
		String samlAuthnRequest = AuthnRequestFactory.createAuthnRequest(
				applicationName, applicationKeyPair, assertionConsumerService);
		String encodedSamlAuthnRequest = Base64.encode(samlAuthnRequest
				.getBytes());

		NameValuePair[] data = { new NameValuePair("SAMLRequest",
				encodedSamlAuthnRequest) };
		postMethod.setRequestBody(data);

		// expectations
		expect(
				this.mockApplicationAuthenticationService
						.getCertificate(applicationName)).andReturn(
				applicationCert);
		expect(
				this.mockPkiValidator
						.validateCertificate(
								SafeOnlineConstants.SAFE_ONLINE_APPLICATIONS_TRUST_DOMAIN,
								applicationCert)).andReturn(true);

		// prepare
		replay(this.mockObjects);

		// operate
		int statusCode = httpClient.executeMethod(postMethod);

		// verify
		verify(this.mockObjects);
		LOG.debug("status code: " + statusCode);
		LOG.debug("result body: " + postMethod.getResponseBodyAsString());
		assertEquals(HttpStatus.SC_MOVED_TEMPORARILY, statusCode);
		String location = postMethod.getResponseHeader("Location").getValue();
		LOG.debug("location: " + location);
		assertTrue(location.endsWith(this.startUrl));
		String resultApplicationId = (String) this.entryServletTestManager
				.getSessionAttribute("applicationId");
		assertEquals(applicationName, resultApplicationId);
		String target = (String) this.entryServletTestManager
				.getSessionAttribute("target");
		assertEquals(assertionConsumerService, target);
	}

	@Test
	public void saml2AuthenticationProtocolWrongSignatureKey() throws Exception {
		// setup
		HttpClient httpClient = new HttpClient();
		PostMethod postMethod = new PostMethod(this.entryServletTestManager
				.getServletLocation());

		KeyPair applicationKeyPair = PkiTestUtils.generateKeyPair();
		String applicationName = "test-application-id";
		String samlAuthnRequest = AuthnRequestFactory.createAuthnRequest(
				applicationName, applicationKeyPair, null);
		String encodedSamlAuthnRequest = Base64.encode(samlAuthnRequest
				.getBytes());

		NameValuePair[] data = { new NameValuePair("SAMLRequest",
				encodedSamlAuthnRequest) };
		postMethod.setRequestBody(data);

		KeyPair foobarKeyPair = PkiTestUtils.generateKeyPair();
		X509Certificate foobarCert = PkiTestUtils
				.generateSelfSignedCertificate(foobarKeyPair,
						"CN=TestApplication");

		// expectations
		expect(
				this.mockApplicationAuthenticationService
						.getCertificate(applicationName)).andReturn(foobarCert);
		expect(
				this.mockPkiValidator
						.validateCertificate(
								SafeOnlineConstants.SAFE_ONLINE_APPLICATIONS_TRUST_DOMAIN,
								foobarCert)).andReturn(true);

		// prepare
		replay(this.mockObjects);

		// operate
		int statusCode = httpClient.executeMethod(postMethod);

		// verify
		verify(this.mockObjects);
		LOG.debug("status code: " + statusCode);
		LOG.debug("result body: " + postMethod.getResponseBodyAsString());
		assertEquals(HttpStatus.SC_MOVED_TEMPORARILY, statusCode);
		String location = postMethod.getResponseHeader("Location").getValue();
		LOG.debug("location: " + location);
		assertTrue(location.endsWith(this.protocolErrorUrl));
		String resultProtocolErrorMessage = (String) this.entryServletTestManager
				.getSessionAttribute("protocolErrorMessage");
		assertNotNull(resultProtocolErrorMessage);
		LOG.debug("result protocol error message: "
				+ resultProtocolErrorMessage);
		assertTrue(resultProtocolErrorMessage
				.indexOf("signature validation error") != -1);
		String resultProtocolName = (String) this.entryServletTestManager
				.getSessionAttribute("protocolName");
		assertNotNull(resultProtocolName);
		assertEquals(Saml2PostProtocolHandler.NAME, resultProtocolName);
	}

	@Test
	public void saml2AuthenticationProtocolNotTrustedApplication()
			throws Exception {
		// setup
		HttpClient httpClient = new HttpClient();
		PostMethod postMethod = new PostMethod(this.entryServletTestManager
				.getServletLocation());

		KeyPair applicationKeyPair = PkiTestUtils.generateKeyPair();
		X509Certificate applicationCert = PkiTestUtils
				.generateSelfSignedCertificate(applicationKeyPair,
						"CN=TestApplication");
		String applicationName = "test-application-id";
		String samlAuthnRequest = AuthnRequestFactory.createAuthnRequest(
				applicationName, applicationKeyPair, null);
		String encodedSamlAuthnRequest = Base64.encode(samlAuthnRequest
				.getBytes());

		NameValuePair[] data = { new NameValuePair("SAMLRequest",
				encodedSamlAuthnRequest) };
		postMethod.setRequestBody(data);

		// expectations
		expect(
				this.mockApplicationAuthenticationService
						.getCertificate(applicationName)).andReturn(
				applicationCert);
		expect(
				this.mockPkiValidator
						.validateCertificate(
								SafeOnlineConstants.SAFE_ONLINE_APPLICATIONS_TRUST_DOMAIN,
								applicationCert)).andReturn(false);

		// prepare
		replay(this.mockObjects);

		// operate
		int statusCode = httpClient.executeMethod(postMethod);

		// verify
		verify(this.mockObjects);
		LOG.debug("status code: " + statusCode);
		LOG.debug("result body: " + postMethod.getResponseBodyAsString());
		assertEquals(HttpStatus.SC_MOVED_TEMPORARILY, statusCode);
		String location = postMethod.getResponseHeader("Location").getValue();
		LOG.debug("location: " + location);
		assertTrue(location.endsWith(this.protocolErrorUrl));
		String resultProtocolErrorMessage = (String) this.entryServletTestManager
				.getSessionAttribute("protocolErrorMessage");
		assertNotNull(resultProtocolErrorMessage);
		LOG.debug("result protocol error message: "
				+ resultProtocolErrorMessage);
		assertTrue(resultProtocolErrorMessage.indexOf("certificate") != -1);
		String resultProtocolName = (String) this.entryServletTestManager
				.getSessionAttribute("protocolName");
		assertNotNull(resultProtocolName);
		assertEquals(Saml2PostProtocolHandler.NAME, resultProtocolName);
	}
}
