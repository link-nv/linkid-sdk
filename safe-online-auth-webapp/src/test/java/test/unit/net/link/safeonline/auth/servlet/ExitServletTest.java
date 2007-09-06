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

import java.io.File;
import java.io.FileOutputStream;
import java.security.KeyPair;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.xml.XMLConstants;

import net.link.safeonline.auth.Device;
import net.link.safeonline.auth.protocol.ProtocolHandlerManager;
import net.link.safeonline.auth.protocol.saml2.Saml2PostProtocolHandler;
import net.link.safeonline.auth.servlet.AuthenticationServiceManager;
import net.link.safeonline.auth.servlet.ExitServlet;
import net.link.safeonline.authentication.service.AuthenticationService;
import net.link.safeonline.authentication.service.SamlAuthorityService;
import net.link.safeonline.entity.helpdesk.HelpdeskEventEntity;
import net.link.safeonline.helpdesk.HelpdeskManager;
import net.link.safeonline.helpdesk.bean.HelpdeskBean;
import net.link.safeonline.test.util.DomTestUtils;
import net.link.safeonline.test.util.JmxTestUtils;
import net.link.safeonline.test.util.JndiTestUtils;
import net.link.safeonline.test.util.MBeanActionHandler;
import net.link.safeonline.test.util.PkiTestUtils;
import net.link.safeonline.test.util.ServletTestManager;
import net.link.safeonline.util.ee.IdentityServiceClient;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xpath.XPathAPI;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class ExitServletTest {

	private static final Log LOG = LogFactory.getLog(ExitServletTest.class);

	private String protocolErrorUrl = "protocol-error";

	private ServletTestManager exitServletTestManager;

	private String username = "test-user-name";

	private String target = "http://test.target";

	private String device = "beid";

	private String inResponseTo = "test-in-response-to";

	private String applicationId = "test-application-id";

	private JmxTestUtils jmxTestUtils;

	private JndiTestUtils jndiTestUtils;

	private Object[] mockObjects;

	private AuthenticationService mockAuthenticationService;

	@Before
	public void setUp() throws Exception {
		this.jmxTestUtils = new JmxTestUtils();
		this.jmxTestUtils.setUp(IdentityServiceClient.IDENTITY_SERVICE);

		final KeyPair keyPair = PkiTestUtils.generateKeyPair();
		this.jmxTestUtils.registerActionHandler("getPrivateKey",
				new MBeanActionHandler() {
					public Object invoke(Object[] arguments) {
						LOG.debug("returning private key");
						return keyPair.getPrivate();
					}
				});
		this.jmxTestUtils.registerActionHandler("getPublicKey",
				new MBeanActionHandler() {
					public Object invoke(Object[] arguments) {
						LOG.debug("returning public key");
						return keyPair.getPublic();
					}
				});

		this.jndiTestUtils = new JndiTestUtils();
		SamlAuthorityService mockSamlAuthorityService = createMock(SamlAuthorityService.class);
		expect(mockSamlAuthorityService.getIssuerName()).andStubReturn(
				"test-issuer-name");
		expect(mockSamlAuthorityService.getAuthnAssertionValidity())
				.andStubReturn(60 * 10);
		this.mockAuthenticationService = createMock(AuthenticationService.class);
		HelpdeskManager mockHelpdeskManager = createMock(HelpdeskManager.class);
		List<HelpdeskEventEntity> helpdeskContext = new Vector<HelpdeskEventEntity>();
		expect(mockHelpdeskManager.persist(helpdeskContext)).andStubReturn(
				new Long(1));
		this.mockObjects = new Object[] { mockSamlAuthorityService,
				this.mockAuthenticationService, mockHelpdeskManager };
		this.jndiTestUtils.setUp();
		this.jndiTestUtils.bindComponent(
				"SafeOnline/SamlAuthorityServiceBean/local",
				mockSamlAuthorityService);
		this.jndiTestUtils.bindComponent(
				"SafeOnline/HelpdeskManagerBean/local", mockHelpdeskManager);

		this.exitServletTestManager = new ServletTestManager();
		Map<String, String> servletInitParams = new HashMap<String, String>();
		servletInitParams.put("ProtocolErrorUrl", this.protocolErrorUrl);
		Map<String, Object> initialSessionAttributes = new HashMap<String, Object>();

		initialSessionAttributes.put(
				ProtocolHandlerManager.PROTOCOL_HANDLER_ID_ATTRIBUTE,
				Saml2PostProtocolHandler.class.getName());
		initialSessionAttributes.put("username", this.username);
		initialSessionAttributes.put("target", this.target);
		initialSessionAttributes.put("applicationId", this.applicationId);
		initialSessionAttributes.put(
				AuthenticationServiceManager.AUTH_SERVICE_ATTRIBUTE,
				this.mockAuthenticationService);
		initialSessionAttributes
				.put(Device.AUTHN_DEVICE_ATTRIBUTE, this.device);
		initialSessionAttributes.put(
				Saml2PostProtocolHandler.IN_RESPONSE_TO_ATTRIBUTE,
				this.inResponseTo);

		helpdeskContext.add(new HelpdeskEventEntity());
		initialSessionAttributes.put(HelpdeskBean.HELPDESK_CONTEXT,
				helpdeskContext);

		this.exitServletTestManager.setUp(ExitServlet.class, servletInitParams,
				null, null, initialSessionAttributes);
	}

	@After
	public void tearDown() throws Exception {
		this.exitServletTestManager.tearDown();
		this.jndiTestUtils.tearDown();
		// this.jmxTestUtils.tearDown();
	}

	@Test
	public void saml2Response() throws Exception {
		// setup
		HttpClient httpClient = new HttpClient();
		GetMethod getMethod = new GetMethod(this.exitServletTestManager
				.getServletLocation());
		getMethod.setFollowRedirects(false);

		// expectations
		this.mockAuthenticationService.commitAuthentication(this.applicationId);

		// prepare
		replay(this.mockObjects);

		// operate
		int statusCode = httpClient.executeMethod(getMethod);

		// verify
		verify(this.mockObjects);
		assertEquals(HttpStatus.SC_OK, statusCode);
		String responseBody = getMethod.getResponseBodyAsString();
		LOG.debug("response body: " + responseBody);

		Document responseDocument = DomTestUtils.parseDocument(responseBody);
		LOG.debug("document element name: "
				+ responseDocument.getDocumentElement().getNodeName());
		Node valueNode = XPathAPI.selectSingleNode(responseDocument,
				"/:html/:body/:form/:div/:input[@name='SAMLResponse']/@value");
		assertNotNull(valueNode);
		String samlResponseValue = valueNode.getTextContent();
		LOG.debug("SAMLResponse value: " + samlResponseValue);
		String samlResponse = new String(Base64.decodeBase64(samlResponseValue
				.getBytes()));
		LOG.debug("SAML Response: " + samlResponse);
		File tmpFile = File.createTempFile("saml-response-", ".xml");
		LOG.debug("tmp filename: " + tmpFile.getAbsolutePath());
		IOUtils.write(samlResponse, new FileOutputStream(tmpFile));
		Document samlResponseDocument = DomTestUtils
				.parseDocument(samlResponse);
		Element nsElement = samlResponseDocument.createElement("nsElement");
		nsElement.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI,
				"xmlns:saml", "urn:oasis:names:tc:SAML:2.0:assertion");
		nsElement.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI,
				"xmlns:samlp", "urn:oasis:names:tc:SAML:2.0:protocol");
		nsElement.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI,
				"xmlns:ds", "http://www.w3.org/2000/09/xmldsig#");
		assertNotNull(XPathAPI.selectSingleNode(samlResponseDocument,
				"/samlp:Response/ds:Signature", nsElement));
		assertNotNull(XPathAPI.selectSingleNode(samlResponseDocument,
				"/samlp:Response/saml:Assertion/saml:Subject/saml:NameID",
				nsElement));
	}
}
