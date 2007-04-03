/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.attrib.ws;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.io.StringWriter;
import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.ws.Binding;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.Handler;

import junit.framework.TestCase;
import net.link.safeonline.attrib.ws.SAMLAttributePortImpl;
import net.link.safeonline.attrib.ws.SAMLAttributeServiceFactory;
import net.link.safeonline.authentication.exception.AttributeNotFoundException;
import net.link.safeonline.authentication.service.AttributeService;
import net.link.safeonline.authentication.service.AuthenticationService;
import net.link.safeonline.authentication.service.SamlAuthorityService;
import net.link.safeonline.model.PkiValidator;
import net.link.safeonline.sdk.attrib.WSSecurityClientHandler;
import net.link.safeonline.test.util.JaasTestUtils;
import net.link.safeonline.test.util.JndiTestUtils;
import net.link.safeonline.test.util.PkiTestUtils;
import net.link.safeonline.test.util.WebServiceTestUtils;
import oasis.names.tc.saml._2_0.assertion.AssertionType;
import oasis.names.tc.saml._2_0.assertion.AttributeStatementType;
import oasis.names.tc.saml._2_0.assertion.AttributeType;
import oasis.names.tc.saml._2_0.assertion.NameIDType;
import oasis.names.tc.saml._2_0.assertion.StatementAbstractType;
import oasis.names.tc.saml._2_0.assertion.SubjectType;
import oasis.names.tc.saml._2_0.protocol.AttributeQueryType;
import oasis.names.tc.saml._2_0.protocol.ObjectFactory;
import oasis.names.tc.saml._2_0.protocol.ResponseType;
import oasis.names.tc.saml._2_0.protocol.SAMLAttributePort;
import oasis.names.tc.saml._2_0.protocol.SAMLAttributeService;
import oasis.names.tc.saml._2_0.protocol.StatusType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.easymock.EasyMock;

public class SAMLAttributePortImplTest extends TestCase {

	private static final Log LOG = LogFactory
			.getLog(SAMLAttributePortImplTest.class);

	private WebServiceTestUtils webServiceTestUtils;

	private SAMLAttributePort clientPort;

	private JndiTestUtils jndiTestUtils;

	private AttributeService mockAttributeService;

	private PkiValidator mockPkiValidator;

	private AuthenticationService mockAuthenticationService;

	private SamlAuthorityService mockSamlAuthorityService;

	private Object[] mockObjects;

	private X509Certificate certificate;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		this.jndiTestUtils = new JndiTestUtils();
		this.jndiTestUtils.setUp();

		this.mockAttributeService = createMock(AttributeService.class);
		this.mockPkiValidator = createMock(PkiValidator.class);
		this.mockAuthenticationService = createMock(AuthenticationService.class);
		this.mockSamlAuthorityService = createMock(SamlAuthorityService.class);

		this.mockObjects = new Object[] { this.mockAttributeService,
				this.mockPkiValidator, this.mockAuthenticationService,
				this.mockSamlAuthorityService };

		this.jndiTestUtils.bindComponent(
				"SafeOnline/AttributeServiceBean/local",
				this.mockAttributeService);
		this.jndiTestUtils.bindComponent("SafeOnline/PkiValidatorBean/local",
				this.mockPkiValidator);
		this.jndiTestUtils.bindComponent(
				"SafeOnline/AuthenticationServiceBean/local",
				this.mockAuthenticationService);
		this.jndiTestUtils.bindComponent(
				"SafeOnline/SamlAuthorityServiceBean/local",
				this.mockSamlAuthorityService);

		expect(
				this.mockPkiValidator.validateCertificate((String) EasyMock
						.anyObject(), (X509Certificate) EasyMock.anyObject()))
				.andStubReturn(true);

		JaasTestUtils.initJaasLoginModule(TestLoginModule.class);

		SAMLAttributePort wsPort = new SAMLAttributePortImpl();
		this.webServiceTestUtils = new WebServiceTestUtils();
		this.webServiceTestUtils.setUp(wsPort);
		SAMLAttributeService service = SAMLAttributeServiceFactory
				.newInstance();
		this.clientPort = service.getSAMLAttributePort();
		this.webServiceTestUtils.setEndpointAddress(clientPort);

		KeyPair keyPair = PkiTestUtils.generateKeyPair();
		this.certificate = PkiTestUtils.generateSelfSignedCertificate(keyPair,
				"CN=Test");

		BindingProvider bindingProvider = (BindingProvider) clientPort;
		Binding binding = bindingProvider.getBinding();
		List<Handler> handlerChain = binding.getHandlerChain();
		Handler wsSecurityHandler = new WSSecurityClientHandler(certificate,
				keyPair.getPrivate());
		handlerChain.add(wsSecurityHandler);
		binding.setHandlerChain(handlerChain);
	}

	@Override
	protected void tearDown() throws Exception {
		this.webServiceTestUtils.tearDown();
		this.jndiTestUtils.tearDown();

		super.tearDown();
	}

	public void testAttributeQuery() throws Exception {
		// setup
		oasis.names.tc.saml._2_0.assertion.ObjectFactory samlObjectFactory = new oasis.names.tc.saml._2_0.assertion.ObjectFactory();

		AttributeQueryType request = new AttributeQueryType();
		SubjectType subject = new SubjectType();
		NameIDType subjectName = new NameIDType();
		String testSubjectLogin = "test-subject-login";
		subjectName.setValue(testSubjectLogin);
		subject.getContent().add(samlObjectFactory.createNameID(subjectName));
		request.setSubject(subject);

		List<AttributeType> attributes = request.getAttribute();
		AttributeType attribute = new AttributeType();
		String testAttributeName = "test-attribute-name";
		attribute.setName(testAttributeName);
		attributes.add(attribute);

		String testAttributeValue = "test-attribute-value";
		String testIssuerName = "test-issuer-name";

		// stubs
		expect(this.mockSamlAuthorityService.getIssuerName()).andStubReturn(
				testIssuerName);

		// expectations
		expect(
				this.mockAttributeService.getAttribute(testSubjectLogin,
						testAttributeName)).andReturn(testAttributeValue);
		expect(this.mockAuthenticationService.authenticate(this.certificate))
				.andReturn("test-application-name");

		// prepare
		replay(this.mockObjects);

		// operate
		ResponseType response = clientPort.attributeQuery(request);

		// verify
		verify(this.mockObjects);
		assertNotNull(response);

		List<Object> resultAssertions = response
				.getAssertionOrEncryptedAssertion();
		assertEquals(1, resultAssertions.size());
		LOG.debug("assertion class: "
				+ resultAssertions.get(0).getClass().getName());
		AssertionType resultAssertion = (AssertionType) resultAssertions.get(0);
		SubjectType resultSubject = resultAssertion.getSubject();
		List<JAXBElement<?>> resultSubjectContent = resultSubject.getContent();
		assertEquals(1, resultSubjectContent.size());
		LOG.debug("subject content type: "
				+ resultSubjectContent.get(0).getValue().getClass().getName());
		NameIDType resultSubjectName = (NameIDType) resultSubjectContent.get(0)
				.getValue();
		assertEquals(testSubjectLogin, resultSubjectName.getValue());

		List<StatementAbstractType> resultStatements = resultAssertion
				.getStatementOrAuthnStatementOrAuthzDecisionStatement();
		assertEquals(1, resultStatements.size());
		AttributeStatementType resultAttributeStatement = (AttributeStatementType) resultStatements
				.get(0);
		List<Object> resultAttributes = resultAttributeStatement
				.getAttributeOrEncryptedAttribute();
		assertEquals(1, resultAttributes.size());
		LOG.debug("result attribute type: "
				+ resultAttributes.get(0).getClass().getName());
		AttributeType resultAttribute = (AttributeType) resultAttributes.get(0);
		assertEquals(testAttributeName, resultAttribute.getName());
		List<Object> resultAttributeValues = resultAttribute
				.getAttributeValue();
		assertEquals(1, resultAttributeValues.size());
		String resultAttributeValue = (String) resultAttributeValues.get(0);
		assertEquals(testAttributeValue, resultAttributeValue);

		JAXBContext context = JAXBContext.newInstance(ObjectFactory.class);
		Marshaller marshaller = context.createMarshaller();
		StringWriter stringWriter = new StringWriter();
		ObjectFactory samlpObjectFactory = new ObjectFactory();
		marshaller.marshal(samlpObjectFactory.createResponse(response),
				stringWriter);
		LOG.debug("response: " + stringWriter);
	}

	public void testAttributeQueryForNonExistingAttribute() throws Exception {
		// setup
		oasis.names.tc.saml._2_0.assertion.ObjectFactory samlObjectFactory = new oasis.names.tc.saml._2_0.assertion.ObjectFactory();

		AttributeQueryType request = new AttributeQueryType();
		SubjectType subject = new SubjectType();
		NameIDType subjectName = new NameIDType();
		String testSubjectLogin = "test-subject-login";
		subjectName.setValue(testSubjectLogin);
		subject.getContent().add(samlObjectFactory.createNameID(subjectName));
		request.setSubject(subject);

		List<AttributeType> attributes = request.getAttribute();
		AttributeType attribute = new AttributeType();
		String testAttributeName = "test-attribute-name";
		attribute.setName(testAttributeName);
		attributes.add(attribute);

		// expectations
		expect(
				this.mockAttributeService.getAttribute(testSubjectLogin,
						testAttributeName)).andThrow(
				new AttributeNotFoundException());
		expect(this.mockAuthenticationService.authenticate(this.certificate))
				.andReturn("test-application-name");

		// prepare
		replay(this.mockObjects);

		// operate
		ResponseType response = clientPort.attributeQuery(request);

		// verify
		verify(this.mockObjects);
		assertNotNull(response);
		StatusType resultStatus = response.getStatus();
		String resultStatusMessage = resultStatus.getStatusMessage();
		LOG.debug("result status message: " + resultStatusMessage);
		assertEquals("urn:oasis:names:tc:SAML:2.0:status:Requester",
				resultStatus.getStatusCode().getValue());
	}

	public static class TestLoginModule implements LoginModule {

		public boolean abort() throws LoginException {
			return true;
		}

		public boolean commit() throws LoginException {
			return true;
		}

		public void initialize(Subject subject,
				CallbackHandler callbackHandler, Map<String, ?> sharedState,
				Map<String, ?> options) {
		}

		public boolean login() throws LoginException {
			return true;
		}

		public boolean logout() throws LoginException {
			return true;
		}
	};
}
