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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.StringWriter;
import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.ws.Binding;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import net.link.safeonline.attrib.ws.SAMLAttributePortImpl;
import net.link.safeonline.attrib.ws.SAMLAttributeServiceFactory;
import net.link.safeonline.authentication.exception.AttributeNotFoundException;
import net.link.safeonline.authentication.service.ApplicationAuthenticationService;
import net.link.safeonline.authentication.service.AttributeService;
import net.link.safeonline.authentication.service.SamlAuthorityService;
import net.link.safeonline.config.model.ConfigurationManager;
import net.link.safeonline.pkix.model.PkiValidator;
import net.link.safeonline.sdk.ws.WSSecurityClientHandler;
import net.link.safeonline.test.util.DummyLoginModule;
import net.link.safeonline.test.util.JaasTestUtils;
import net.link.safeonline.test.util.JndiTestUtils;
import net.link.safeonline.test.util.PkiTestUtils;
import net.link.safeonline.test.util.WebServiceTestUtils;
import net.link.safeonline.ws.common.WebServiceConstants;
import net.link.safeonline.ws.util.ri.InjectionInstanceResolver;
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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SAMLAttributePortImplTest {

	private static final Log LOG = LogFactory
			.getLog(SAMLAttributePortImplTest.class);

	private WebServiceTestUtils webServiceTestUtils;

	private SAMLAttributePort clientPort;

	private JndiTestUtils jndiTestUtils;

	private AttributeService mockAttributeService;

	private PkiValidator mockPkiValidator;

	private ApplicationAuthenticationService mockAuthenticationService;

	private SamlAuthorityService mockSamlAuthorityService;

	private ConfigurationManager mockConfigurationManager;

	private Object[] mockObjects;

	private X509Certificate certificate;

	@SuppressWarnings("unchecked")
	@Before
	public void setUp() throws Exception {
		LOG.debug("setup");

		this.jndiTestUtils = new JndiTestUtils();
		this.jndiTestUtils.setUp();

		this.mockAttributeService = createMock(AttributeService.class);
		this.mockPkiValidator = createMock(PkiValidator.class);
		this.mockAuthenticationService = createMock(ApplicationAuthenticationService.class);
		this.mockSamlAuthorityService = createMock(SamlAuthorityService.class);
		this.mockConfigurationManager = createMock(ConfigurationManager.class);

		this.mockObjects = new Object[] { this.mockAttributeService,
				this.mockPkiValidator, this.mockAuthenticationService,
				this.mockSamlAuthorityService, this.mockConfigurationManager };

		this.jndiTestUtils.bindComponent(
				"SafeOnline/AttributeServiceBean/local",
				this.mockAttributeService);
		this.jndiTestUtils.bindComponent("SafeOnline/PkiValidatorBean/local",
				this.mockPkiValidator);
		this.jndiTestUtils.bindComponent(
				"SafeOnline/ApplicationAuthenticationServiceBean/local",
				this.mockAuthenticationService);
		this.jndiTestUtils.bindComponent(
				"SafeOnline/SamlAuthorityServiceBean/local",
				this.mockSamlAuthorityService);
		this.jndiTestUtils.bindComponent(
				"SafeOnline/ConfigurationManagerBean/local",
				this.mockConfigurationManager);

		expect(
				this.mockPkiValidator.validateCertificate((String) EasyMock
						.anyObject(), (X509Certificate) EasyMock.anyObject()))
				.andStubReturn(true);

		expect(
				this.mockConfigurationManager
						.getMaximumWsSecurityTimestampOffset()).andStubReturn(
				Long.MAX_VALUE);

		JaasTestUtils.initJaasLoginModule(DummyLoginModule.class);

		SAMLAttributePort wsPort = new SAMLAttributePortImpl();
		this.webServiceTestUtils = new WebServiceTestUtils();
		this.webServiceTestUtils.setUp(wsPort);
		/*
		 * Next is required, else the wsPort will get old mocks injected when
		 * running multiple tests.
		 */
		InjectionInstanceResolver.clearInstanceCache();
		SAMLAttributeService service = SAMLAttributeServiceFactory
				.newInstance();
		this.clientPort = service.getSAMLAttributePort();
		this.webServiceTestUtils.setEndpointAddress(this.clientPort);

		KeyPair keyPair = PkiTestUtils.generateKeyPair();
		this.certificate = PkiTestUtils.generateSelfSignedCertificate(keyPair,
				"CN=Test");

		BindingProvider bindingProvider = (BindingProvider) this.clientPort;
		Binding binding = bindingProvider.getBinding();
		List<Handler> handlerChain = binding.getHandlerChain();
		Handler<SOAPMessageContext> wsSecurityHandler = new WSSecurityClientHandler(
				this.certificate, keyPair.getPrivate());
		handlerChain.add(wsSecurityHandler);
		binding.setHandlerChain(handlerChain);
	}

	@After
	public void tearDown() throws Exception {
		LOG.debug("tearDown");
		this.webServiceTestUtils.tearDown();
		this.jndiTestUtils.tearDown();
	}

	@Test
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
				this.mockAttributeService.getConfirmedAttributeValue(
						testSubjectLogin, testAttributeName)).andReturn(
				testAttributeValue);
		expect(this.mockAuthenticationService.authenticate(this.certificate))
				.andReturn("test-application-name");

		// prepare
		replay(this.mockObjects);

		// operate
		ResponseType response = this.clientPort.attributeQuery(request);

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
		Object resultAttributeValueObject = resultAttributeValues.get(0);
		assertEquals(resultAttributeValueObject.getClass(), String.class);
		String resultAttributeValue = (String) resultAttributeValueObject;
		assertEquals(testAttributeValue, resultAttributeValue);

		JAXBContext context = JAXBContext.newInstance(ObjectFactory.class);
		Marshaller marshaller = context.createMarshaller();
		StringWriter stringWriter = new StringWriter();
		ObjectFactory samlpObjectFactory = new ObjectFactory();
		marshaller.marshal(samlpObjectFactory.createResponse(response),
				stringWriter);
		LOG.debug("response: " + stringWriter);
	}

	@Test
	public void testQueryMultivaluedAttribute() throws Exception {
		// setup
		oasis.names.tc.saml._2_0.assertion.ObjectFactory samlObjectFactory = new oasis.names.tc.saml._2_0.assertion.ObjectFactory();

		AttributeQueryType request = new AttributeQueryType();
		SubjectType subject = new SubjectType();
		NameIDType subjectName = new NameIDType();
		String testSubjectLogin = "test-subject-login-"
				+ UUID.randomUUID().toString();
		subjectName.setValue(testSubjectLogin);
		subject.getContent().add(samlObjectFactory.createNameID(subjectName));
		request.setSubject(subject);

		List<AttributeType> attributes = request.getAttribute();
		AttributeType attribute = new AttributeType();
		String testAttributeName = "test-attribute-name-"
				+ UUID.randomUUID().toString();
		attribute.setName(testAttributeName);
		attributes.add(attribute);

		String testAttributeValue1 = "test-attribute-value-1";
		String testAttributeValue2 = "test-attribute-value-2";
		String[] testAttributeValues = { testAttributeValue1,
				testAttributeValue2 };
		String testIssuerName = "test-issuer-name";

		// stubs
		expect(this.mockSamlAuthorityService.getIssuerName()).andStubReturn(
				testIssuerName);

		// expectations
		expect(
				this.mockAttributeService.getConfirmedAttributeValue(
						testSubjectLogin, testAttributeName)).andReturn(
				testAttributeValues);
		expect(this.mockAuthenticationService.authenticate(this.certificate))
				.andReturn("test-application-name");

		// prepare
		replay(this.mockObjects);

		// operate
		ResponseType response = this.clientPort.attributeQuery(request);

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
		assertEquals(Boolean.TRUE.toString(), resultAttribute
				.getOtherAttributes().get(
						WebServiceConstants.MULTIVALUED_ATTRIBUTE));
		List<Object> resultAttributeValues = resultAttribute
				.getAttributeValue();
		assertEquals(2, resultAttributeValues.size());
		Object resultAttributeValueObject = resultAttributeValues.get(0);
		assertEquals(String.class, resultAttributeValueObject.getClass());
		String resultAttributeValue = (String) resultAttributeValueObject;
		assertEquals(testAttributeValue1, resultAttributeValue);
		resultAttributeValueObject = resultAttributeValues.get(1);
		assertEquals(String.class, resultAttributeValueObject.getClass());
		resultAttributeValue = (String) resultAttributeValueObject;
		assertEquals(testAttributeValue2, resultAttributeValue);

		JAXBContext context = JAXBContext.newInstance(ObjectFactory.class);
		Marshaller marshaller = context.createMarshaller();
		StringWriter stringWriter = new StringWriter();
		ObjectFactory samlpObjectFactory = new ObjectFactory();
		marshaller.marshal(samlpObjectFactory.createResponse(response),
				stringWriter);
		LOG.debug("response: " + stringWriter);
	}

	@Test
	public void testQueryCompoundedAttribute() throws Exception {
		// setup
		oasis.names.tc.saml._2_0.assertion.ObjectFactory samlObjectFactory = new oasis.names.tc.saml._2_0.assertion.ObjectFactory();

		AttributeQueryType request = new AttributeQueryType();
		SubjectType subject = new SubjectType();
		NameIDType subjectName = new NameIDType();
		String testSubjectLogin = "test-subject-login-"
				+ UUID.randomUUID().toString();
		subjectName.setValue(testSubjectLogin);
		subject.getContent().add(samlObjectFactory.createNameID(subjectName));
		request.setSubject(subject);

		List<AttributeType> attributes = request.getAttribute();
		AttributeType attribute = new AttributeType();
		String testAttributeName = "test-compounded-attribute-name-"
				+ UUID.randomUUID().toString();
		attribute.setName(testAttributeName);
		attributes.add(attribute);

		Map<?, ?>[] testAttributeValues = new Map[2];
		Map<String, Object> compAttribute1 = new HashMap<String, Object>();
		testAttributeValues[0] = compAttribute1;
		compAttribute1.put("test-member1", "test-value11");
		compAttribute1.put("test-member2", "test-value21");
		Map<String, Object> compAttribute2 = new HashMap<String, Object>();
		testAttributeValues[1] = compAttribute2;
		compAttribute2.put("test-member1", "test-value12");
		compAttribute2.put("test-member2", "test-value22");

		String testIssuerName = "test-issuer-name";

		// stubs
		expect(this.mockSamlAuthorityService.getIssuerName()).andStubReturn(
				testIssuerName);

		// expectations
		expect(
				this.mockAttributeService.getConfirmedAttributeValue(
						testSubjectLogin, testAttributeName)).andReturn(
				testAttributeValues);
		expect(this.mockAuthenticationService.authenticate(this.certificate))
				.andReturn("test-application-name");

		// prepare
		replay(this.mockObjects);

		// operate
		ResponseType response = this.clientPort.attributeQuery(request);

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
		assertEquals(Boolean.TRUE.toString(), resultAttribute
				.getOtherAttributes().get(
						WebServiceConstants.MULTIVALUED_ATTRIBUTE));
		List<Object> resultAttributeValues = resultAttribute
				.getAttributeValue();
		assertEquals(2, resultAttributeValues.size());

		JAXBContext context = JAXBContext.newInstance(ObjectFactory.class);
		Marshaller marshaller = context.createMarshaller();
		StringWriter stringWriter = new StringWriter();
		ObjectFactory samlpObjectFactory = new ObjectFactory();
		marshaller.marshal(samlpObjectFactory.createResponse(response),
				stringWriter);
		LOG.debug("response: " + stringWriter);
	}

	@Test
	public void testAttributeQueryWithBooleanValue() throws Exception {
		// setup
		oasis.names.tc.saml._2_0.assertion.ObjectFactory samlObjectFactory = new oasis.names.tc.saml._2_0.assertion.ObjectFactory();

		AttributeQueryType request = new AttributeQueryType();
		SubjectType subject = new SubjectType();
		NameIDType subjectName = new NameIDType();
		String testSubjectLogin = "test-subject-login-"
				+ UUID.randomUUID().toString();
		subjectName.setValue(testSubjectLogin);
		subject.getContent().add(samlObjectFactory.createNameID(subjectName));
		request.setSubject(subject);

		List<AttributeType> attributes = request.getAttribute();
		AttributeType attribute = new AttributeType();
		String testAttributeName = "test-attribute-name-"
				+ UUID.randomUUID().toString();
		attribute.setName(testAttributeName);
		attributes.add(attribute);

		Boolean testAttributeValue = Boolean.TRUE;
		String testIssuerName = "test-issuer-name";

		// stubs
		expect(this.mockSamlAuthorityService.getIssuerName()).andStubReturn(
				testIssuerName);

		// expectations
		expect(
				this.mockAttributeService.getConfirmedAttributeValue(
						testSubjectLogin, testAttributeName)).andReturn(
				testAttributeValue);
		expect(this.mockAuthenticationService.authenticate(this.certificate))
				.andReturn("test-application-name");

		// prepare
		replay(this.mockObjects);

		// operate
		ResponseType response = this.clientPort.attributeQuery(request);

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
		Object resultAttributeValueObject = resultAttributeValues.get(0);
		assertEquals(resultAttributeValueObject.getClass(), Boolean.class);
		Boolean resultAttributeValue = (Boolean) resultAttributeValueObject;
		assertEquals(testAttributeValue, resultAttributeValue);
	}

	@Test
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
				this.mockAttributeService.getConfirmedAttributeValue(
						testSubjectLogin, testAttributeName)).andThrow(
				new AttributeNotFoundException());
		expect(this.mockAuthenticationService.authenticate(this.certificate))
				.andReturn("test-application-name");

		// prepare
		replay(this.mockObjects);

		// operate
		ResponseType response = this.clientPort.attributeQuery(request);

		// verify
		verify(this.mockObjects);
		assertNotNull(response);
		StatusType resultStatus = response.getStatus();
		String resultStatusMessage = resultStatus.getStatusMessage();
		LOG.debug("result status message: " + resultStatusMessage);
		assertEquals("urn:oasis:names:tc:SAML:2.0:status:Requester",
				resultStatus.getStatusCode().getValue());
	}
}
