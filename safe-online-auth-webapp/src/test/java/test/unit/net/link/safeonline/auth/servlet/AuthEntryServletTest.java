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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.audit.SecurityAuditLogger;
import net.link.safeonline.auth.protocol.saml2.Saml2PostProtocolHandler;
import net.link.safeonline.auth.servlet.AuthnEntryServlet;
import net.link.safeonline.authentication.service.ApplicationAuthenticationService;
import net.link.safeonline.authentication.service.DevicePolicyService;
import net.link.safeonline.authentication.service.SamlAuthorityService;
import net.link.safeonline.entity.DeviceClassEntity;
import net.link.safeonline.entity.DeviceEntity;
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

public class AuthEntryServletTest {

	private static final Log LOG = LogFactory
			.getLog(AuthEntryServletTest.class);

	private ServletTestManager entryServletTestManager;

	private String firstTimeUrl = "first-time";

	private String startUrl = "start";

	private String unsupportedProtocolUrl = "unsupported-protocol";

	private String protocolErrorUrl = "protocol-error";

	private String protocol = "http";

	private JndiTestUtils jndiTestUtils;

	private ApplicationAuthenticationService mockApplicationAuthenticationService;

	private PkiValidator mockPkiValidator;

	private DevicePolicyService mockDevicePolicyService;

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

		SamlAuthorityService mockSamlAuthorityService = createMock(SamlAuthorityService.class);
		expect(mockSamlAuthorityService.getIssuerName()).andStubReturn(
				"test-issuer-name");
		expect(mockSamlAuthorityService.getAuthnAssertionValidity())
				.andStubReturn(10 * 60);
		this.jndiTestUtils.bindComponent(
				"SafeOnline/SamlAuthorityServiceBean/local",
				mockSamlAuthorityService);

		this.mockDevicePolicyService = createMock(DevicePolicyService.class);
		this.jndiTestUtils.bindComponent(
				"SafeOnline/DevicePolicyServiceBean/local",
				this.mockDevicePolicyService);

		JmxTestUtils jmxTestUtils = new JmxTestUtils();
		jmxTestUtils.setUp(IdentityServiceClient.IDENTITY_SERVICE);

		this.entryServletTestManager = new ServletTestManager();
		Map<String, String> initParams = new HashMap<String, String>();
		initParams.put("StartUrl", this.startUrl);
		initParams.put("FirstTimeUrl", this.firstTimeUrl);
		initParams.put("UnsupportedProtocolUrl", this.unsupportedProtocolUrl);
		initParams.put("ProtocolErrorUrl", this.protocolErrorUrl);
		initParams.put("Protocol", this.protocol);
		this.entryServletTestManager.setUp(AuthnEntryServlet.class, initParams);

		this.mockObjects = new Object[] {
				this.mockApplicationAuthenticationService,
				this.mockPkiValidator, mockSamlAuthorityService,
				this.mockDevicePolicyService };
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
	public void saml2AuthenticationProtocol() throws Exception {
		// setup
		HttpClient httpClient = new HttpClient();
		String servletLocation = this.entryServletTestManager
				.getServletLocation();
		PostMethod postMethod = new PostMethod(servletLocation);

		KeyPair applicationKeyPair = PkiTestUtils.generateKeyPair();
		X509Certificate applicationCert = PkiTestUtils
				.generateSelfSignedCertificate(applicationKeyPair,
						"CN=TestApplication");
		String applicationName = "test-application-id";
		String assertionConsumerService = "http://test.assertion.consumer.service";
		String samlAuthnRequest = AuthnRequestFactory.createAuthnRequest(
				applicationName, applicationName, applicationKeyPair,
				assertionConsumerService, servletLocation, null, null);
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
		assertTrue(location.endsWith(this.firstTimeUrl));
		String resultApplicationId = (String) this.entryServletTestManager
				.getSessionAttribute("applicationId");
		assertEquals(applicationName, resultApplicationId);
		String target = (String) this.entryServletTestManager
				.getSessionAttribute("target");
		assertEquals(assertionConsumerService, target);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void saml2RequestedAuthnContextSetsRequiredDevices()
			throws Exception {

		// setup
		HttpClient httpClient = new HttpClient();
		String servletLocation = this.entryServletTestManager
				.getServletLocation();
		PostMethod postMethod = new PostMethod(servletLocation);

		KeyPair applicationKeyPair = PkiTestUtils.generateKeyPair();
		X509Certificate applicationCert = PkiTestUtils
				.generateSelfSignedCertificate(applicationKeyPair,
						"CN=TestApplication");
		String applicationName = "test-application-id";
		String assertionConsumerService = "http://test.assertion.consumer.service";
		Set<String> devices = new HashSet<String>();
		devices.add(SafeOnlineConstants.PASSWORD_DEVICE_AUTH_CONTEXT_CLASS);
		String samlAuthnRequest = AuthnRequestFactory.createAuthnRequest(
				applicationName, applicationName, applicationKeyPair,
				assertionConsumerService, servletLocation, null, devices);
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

		List<DeviceEntity> authnDevices = new LinkedList<DeviceEntity>();
		DeviceEntity passwordDevice = new DeviceEntity(
				SafeOnlineConstants.USERNAME_PASSWORD_DEVICE_ID,
				new DeviceClassEntity(
						SafeOnlineConstants.PASSWORD_DEVICE_CLASS,
						SafeOnlineConstants.PASSWORD_DEVICE_AUTH_CONTEXT_CLASS),
				null, null, null, null, null, null);
		authnDevices.add(passwordDevice);
		expect(
				this.mockDevicePolicyService
						.listDevices(SafeOnlineConstants.PASSWORD_DEVICE_AUTH_CONTEXT_CLASS))
				.andReturn(authnDevices);

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
		assertTrue(location.endsWith(this.firstTimeUrl));
		String resultApplicationId = (String) this.entryServletTestManager
				.getSessionAttribute("applicationId");
		assertEquals(applicationName, resultApplicationId);
		String target = (String) this.entryServletTestManager
				.getSessionAttribute("target");
		assertEquals(assertionConsumerService, target);
		Set<DeviceEntity> resultRequiredDevices = (Set<DeviceEntity>) this.entryServletTestManager
				.getSessionAttribute("requiredDevices");
		assertNotNull(resultRequiredDevices);
		for (DeviceEntity resultRequiredDevice : resultRequiredDevices)
			LOG
					.debug("resultRequiredDevice: "
							+ resultRequiredDevice.getName());
		assertTrue(resultRequiredDevices.contains(passwordDevice));
	}

	@Test
	public void saml2AuthenticationProtocolWrongSignatureKey() throws Exception {
		// setup
		HttpClient httpClient = new HttpClient();
		String servletLocation = this.entryServletTestManager
				.getServletLocation();
		PostMethod postMethod = new PostMethod(servletLocation);

		KeyPair applicationKeyPair = PkiTestUtils.generateKeyPair();
		String applicationName = "test-application-id";
		String samlAuthnRequest = AuthnRequestFactory.createAuthnRequest(
				applicationName, applicationName, applicationKeyPair, null,
				servletLocation, null, null);
		String encodedSamlAuthnRequest = Base64.encode(samlAuthnRequest
				.getBytes());

		NameValuePair[] data = { new NameValuePair("SAMLRequest",
				encodedSamlAuthnRequest) };
		postMethod.setRequestBody(data);

		KeyPair foobarKeyPair = PkiTestUtils.generateKeyPair();
		X509Certificate foobarCert = PkiTestUtils
				.generateSelfSignedCertificate(foobarKeyPair,
						"CN=TestApplication");

		SecurityAuditLogger mockSecurityAuditLogger = createMock(SecurityAuditLogger.class);
		this.jndiTestUtils.bindComponent(
				"SafeOnline/SecurityAuditLoggerBean/local",
				mockSecurityAuditLogger);

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
		String servletLocation = this.entryServletTestManager
				.getServletLocation();
		PostMethod postMethod = new PostMethod(servletLocation);

		KeyPair applicationKeyPair = PkiTestUtils.generateKeyPair();
		X509Certificate applicationCert = PkiTestUtils
				.generateSelfSignedCertificate(applicationKeyPair,
						"CN=TestApplication");
		String applicationName = "test-application-id";
		String samlAuthnRequest = AuthnRequestFactory.createAuthnRequest(
				applicationName, applicationName, applicationKeyPair, null,
				servletLocation, null, null);
		String encodedSamlAuthnRequest = Base64.encode(samlAuthnRequest
				.getBytes());

		NameValuePair[] data = { new NameValuePair("SAMLRequest",
				encodedSamlAuthnRequest) };
		postMethod.setRequestBody(data);

		SecurityAuditLogger mockSecurityAuditLogger = createMock(SecurityAuditLogger.class);
		this.jndiTestUtils.bindComponent(
				"SafeOnline/SecurityAuditLoggerBean/local",
				mockSecurityAuditLogger);

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
