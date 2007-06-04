/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.data.ws;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.ws.Binding;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.soap.SOAPFaultException;

import junit.framework.TestCase;
import liberty.dst._2006_08.ref.safe_online.DataService;
import liberty.dst._2006_08.ref.safe_online.DataServicePort;
import liberty.dst._2006_08.ref.safe_online.ObjectFactory;
import liberty.dst._2006_08.ref.safe_online.QueryItemType;
import liberty.dst._2006_08.ref.safe_online.QueryResponseType;
import liberty.dst._2006_08.ref.safe_online.QueryType;
import liberty.dst._2006_08.ref.safe_online.SelectType;
import liberty.util._2006_08.StatusType;
import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.service.ApplicationAuthenticationService;
import net.link.safeonline.authentication.service.AttributeProviderService;
import net.link.safeonline.data.ws.DataServiceConstants;
import net.link.safeonline.data.ws.DataServiceFactory;
import net.link.safeonline.data.ws.DataServicePortImpl;
import net.link.safeonline.data.ws.SecondLevelStatusCode;
import net.link.safeonline.data.ws.TopLevelStatusCode;
import net.link.safeonline.entity.AttributeEntity;
import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.model.PkiValidator;
import net.link.safeonline.sdk.ws.WSSecurityClientHandler;
import net.link.safeonline.sdk.ws.data.TargetIdentityClientHandler;
import net.link.safeonline.test.util.DomTestUtils;
import net.link.safeonline.test.util.DummyLoginModule;
import net.link.safeonline.test.util.JaasTestUtils;
import net.link.safeonline.test.util.JndiTestUtils;
import net.link.safeonline.test.util.PkiTestUtils;
import net.link.safeonline.test.util.WebServiceTestUtils;
import net.link.safeonline.ws.util.LoggingHandler;
import net.link.safeonline.ws.util.ri.InjectionInstanceResolver;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class DataServicePortImplTest extends TestCase {

	private static final Log LOG = LogFactory
			.getLog(DataServicePortImplTest.class);

	private WebServiceTestUtils webServiceTestUtils;

	private JndiTestUtils jndiTestUtils;

	private DataServicePort dataServicePort;

	private AttributeProviderService mockAttributeProviderService;

	private ApplicationAuthenticationService mockAuthenticationService;

	private PkiValidator mockPkiValidator;

	private Object[] mockObjects;

	private X509Certificate certificate;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		this.jndiTestUtils = new JndiTestUtils();
		this.jndiTestUtils.setUp();

		this.mockAttributeProviderService = createMock(AttributeProviderService.class);
		this.jndiTestUtils.bindComponent(
				"SafeOnline/AttributeProviderServiceBean/local",
				this.mockAttributeProviderService);

		this.mockAuthenticationService = createMock(ApplicationAuthenticationService.class);
		this.jndiTestUtils.bindComponent(
				"SafeOnline/ApplicationAuthenticationServiceBean/local",
				this.mockAuthenticationService);

		this.mockPkiValidator = createMock(PkiValidator.class);
		this.jndiTestUtils.bindComponent("SafeOnline/PkiValidatorBean/local",
				this.mockPkiValidator);

		this.mockObjects = new Object[] { this.mockAttributeProviderService,
				this.mockAuthenticationService, this.mockPkiValidator };

		this.webServiceTestUtils = new WebServiceTestUtils();

		DataServicePortImpl wsPort = new DataServicePortImpl();
		this.webServiceTestUtils.setUp(wsPort);
		InjectionInstanceResolver.clearInstanceCache();

		DataService dataService = DataServiceFactory.newInstance();
		this.dataServicePort = dataService.getDataServicePort();
		this.webServiceTestUtils.setEndpointAddress(this.dataServicePort);

		KeyPair keyPair = PkiTestUtils.generateKeyPair();
		this.certificate = PkiTestUtils.generateSelfSignedCertificate(keyPair,
				"CN=Test");

		BindingProvider bindingProvider = (BindingProvider) this.dataServicePort;
		Binding binding = bindingProvider.getBinding();
		List<Handler> handlerChain = binding.getHandlerChain();
		Handler wsSecurityHandler = new WSSecurityClientHandler(certificate,
				keyPair.getPrivate());
		handlerChain.add(wsSecurityHandler);
		binding.setHandlerChain(handlerChain);

		JaasTestUtils.initJaasLoginModule(DummyLoginModule.class);
	}

	@Override
	protected void tearDown() throws Exception {

		this.webServiceTestUtils.tearDown();

		this.jndiTestUtils.tearDown();

		super.tearDown();
	}

	public void testInvalidCertificate() throws Exception {
		// setup
		QueryType query = new QueryType();

		// expectations
		expect(
				this.mockPkiValidator
						.validateCertificate(
								SafeOnlineConstants.SAFE_ONLINE_APPLICATIONS_TRUST_DOMAIN,
								this.certificate)).andReturn(false);

		// prepare
		replay(this.mockObjects);

		// operate
		try {
			this.dataServicePort.query(query);
			fail();
		} catch (SOAPFaultException e) {
			// expected
		}

		// verify
		verify(this.mockObjects);
	}

	public void testUnknownApplication() throws Exception {
		// setup
		QueryType query = new QueryType();

		// expectations
		expect(
				this.mockPkiValidator
						.validateCertificate(
								SafeOnlineConstants.SAFE_ONLINE_APPLICATIONS_TRUST_DOMAIN,
								this.certificate)).andReturn(true);

		expect(this.mockAuthenticationService.authenticate(this.certificate))
				.andThrow(new ApplicationNotFoundException());

		// prepare
		replay(this.mockObjects);

		// operate
		try {
			this.dataServicePort.query(query);
			fail();
		} catch (SOAPFaultException e) {
			// expected
		}

		// verify
		verify(this.mockObjects);
	}

	public void testUnsupportedObjectType() throws Exception {
		// setup
		String applicationName = "application-" + UUID.randomUUID().toString();
		QueryType query = new QueryType();
		QueryItemType queryItem = new QueryItemType();
		query.getQueryItem().add(queryItem);
		queryItem.setObjectType("foo-bar-object-type");

		// expectations
		expect(
				this.mockPkiValidator
						.validateCertificate(
								SafeOnlineConstants.SAFE_ONLINE_APPLICATIONS_TRUST_DOMAIN,
								this.certificate)).andReturn(true);

		expect(this.mockAuthenticationService.authenticate(this.certificate))
				.andReturn(applicationName);

		// prepare
		replay(this.mockObjects);

		// operate
		QueryResponseType result = this.dataServicePort.query(query);

		// verify
		verify(this.mockObjects);
		StatusType status = result.getStatus();
		assertEquals(TopLevelStatusCode.FAILED, TopLevelStatusCode
				.fromCode(status.getCode()));
		assertEquals(SecondLevelStatusCode.UNSUPPORTED_OBJECT_TYPE,
				SecondLevelStatusCode.fromCode(status.getStatus().get(0)
						.getCode()));
	}

	public void testMissingSelect() throws Exception {
		// setup
		String applicationName = "application-" + UUID.randomUUID().toString();
		QueryType query = new QueryType();
		QueryItemType queryItem = new QueryItemType();
		query.getQueryItem().add(queryItem);
		queryItem
				.setObjectType(DataServiceConstants.STRING_ATTRIBUTE_OBJECT_TYPE);

		// expectations
		expect(
				this.mockPkiValidator
						.validateCertificate(
								SafeOnlineConstants.SAFE_ONLINE_APPLICATIONS_TRUST_DOMAIN,
								this.certificate)).andReturn(true);

		expect(this.mockAuthenticationService.authenticate(this.certificate))
				.andReturn(applicationName);

		// prepare
		replay(this.mockObjects);

		// operate
		QueryResponseType result = this.dataServicePort.query(query);

		// verify
		verify(this.mockObjects);
		StatusType status = result.getStatus();
		assertEquals(TopLevelStatusCode.FAILED, TopLevelStatusCode
				.fromCode(status.getCode()));
		assertEquals(SecondLevelStatusCode.MISSING_SELECT,
				SecondLevelStatusCode.fromCode(status.getStatus().get(0)
						.getCode()));
	}

	public void testMissingTargetIdentity() throws Exception {
		// setup
		String applicationName = "application-" + UUID.randomUUID().toString();
		QueryType query = new QueryType();
		QueryItemType queryItem = new QueryItemType();
		query.getQueryItem().add(queryItem);
		queryItem
				.setObjectType(DataServiceConstants.STRING_ATTRIBUTE_OBJECT_TYPE);
		SelectType select = new SelectType();
		select.setValue("select-value");
		queryItem.setSelect(select);

		// expectations
		expect(
				this.mockPkiValidator
						.validateCertificate(
								SafeOnlineConstants.SAFE_ONLINE_APPLICATIONS_TRUST_DOMAIN,
								this.certificate)).andReturn(true);

		expect(this.mockAuthenticationService.authenticate(this.certificate))
				.andReturn(applicationName);

		// prepare
		replay(this.mockObjects);

		// operate
		QueryResponseType result = this.dataServicePort.query(query);

		// verify
		verify(this.mockObjects);
		StatusType status = result.getStatus();
		assertEquals(TopLevelStatusCode.FAILED, TopLevelStatusCode
				.fromCode(status.getCode()));
		assertEquals(SecondLevelStatusCode.MISSING_CREDENTIALS,
				SecondLevelStatusCode.fromCode(status.getStatus().get(0)
						.getCode()));
	}

	public void testTargetIdentityNotSigned() throws Exception {
		// setup
		String applicationName = "application-" + UUID.randomUUID().toString();
		QueryType query = new QueryType();
		QueryItemType queryItem = new QueryItemType();
		query.getQueryItem().add(queryItem);
		queryItem
				.setObjectType(DataServiceConstants.STRING_ATTRIBUTE_OBJECT_TYPE);
		SelectType select = new SelectType();
		select.setValue("select-value");
		queryItem.setSelect(select);

		BindingProvider bindingProvider = (BindingProvider) this.dataServicePort;
		Binding binding = bindingProvider.getBinding();
		List<Handler> handlerChain = binding.getHandlerChain();
		TargetIdentityClientHandler targetIdentityClientHandler = new TargetIdentityClientHandler();
		String targetIdentity = "test-target-identity";
		targetIdentityClientHandler.setTargetIdentity(targetIdentity);
		handlerChain.add(targetIdentityClientHandler);
		LoggingHandler loggingHandler = new LoggingHandler();
		handlerChain.add(loggingHandler);
		binding.setHandlerChain(handlerChain);

		// expectations
		expect(
				this.mockPkiValidator
						.validateCertificate(
								SafeOnlineConstants.SAFE_ONLINE_APPLICATIONS_TRUST_DOMAIN,
								this.certificate)).andReturn(true);

		expect(this.mockAuthenticationService.authenticate(this.certificate))
				.andReturn(applicationName);

		// prepare
		replay(this.mockObjects);

		// operate & verify
		try {
			this.dataServicePort.query(query);
			fail();
		} catch (SOAPFaultException e) {
			// expected
		}

		// verify
		verify(this.mockObjects);
	}

	public void testQueryMultivaluedAttribute() throws Exception {
		// setup
		String applicationName = "application-" + UUID.randomUUID().toString();
		QueryType query = new QueryType();
		QueryItemType queryItem = new QueryItemType();
		query.getQueryItem().add(queryItem);
		queryItem
				.setObjectType(DataServiceConstants.STRING_ATTRIBUTE_OBJECT_TYPE);
		SelectType select = new SelectType();
		String attributeName = "test-attribute-name";
		select.setValue(attributeName);
		queryItem.setSelect(select);

		BindingProvider bindingProvider = (BindingProvider) this.dataServicePort;
		Binding binding = bindingProvider.getBinding();
		List<Handler> handlerChain = binding.getHandlerChain();
		TargetIdentityClientHandler targetIdentityClientHandler = new TargetIdentityClientHandler();
		String targetIdentity = "test-target-identity";
		targetIdentityClientHandler.setTargetIdentity(targetIdentity);
		handlerChain.add(0, targetIdentityClientHandler);
		LoggingHandler loggingHandler = new LoggingHandler();
		handlerChain.add(loggingHandler);
		binding.setHandlerChain(handlerChain);

		for (Handler handler : handlerChain) {
			LOG.debug("handler in chain: " + handler.getClass().getName());
		}

		// expectations
		expect(
				this.mockPkiValidator
						.validateCertificate(
								SafeOnlineConstants.SAFE_ONLINE_APPLICATIONS_TRUST_DOMAIN,
								this.certificate)).andReturn(true);

		expect(this.mockAuthenticationService.authenticate(this.certificate))
				.andReturn(applicationName);

		AttributeTypeEntity attributeType = new AttributeTypeEntity();
		attributeType.setName(attributeName);
		attributeType.setType(SafeOnlineConstants.STRING_TYPE);
		SubjectEntity subject = new SubjectEntity();
		List<AttributeEntity> attributes = new LinkedList<AttributeEntity>();
		AttributeEntity attribute1 = new AttributeEntity(attributeType,
				subject, 0);
		String attributeValue1 = "value1";
		attribute1.setStringValue(attributeValue1);
		attributes.add(attribute1);
		AttributeEntity attribute2 = new AttributeEntity(attributeType,
				subject, 1);
		String attributeValue2 = "value2";
		attribute2.setStringValue(attributeValue2);
		attributes.add(attribute2);

		expect(
				this.mockAttributeProviderService.getAttributes(targetIdentity,
						attributeName)).andReturn(attributes);

		// prepare
		replay(this.mockObjects);

		// operate
		QueryResponseType response = this.dataServicePort.query(query);

		// verify
		verify(this.mockObjects);

		// marshall the result to a DOM
		JAXBContext context = JAXBContext.newInstance(ObjectFactory.class);
		ObjectFactory objectFactory = new ObjectFactory();
		Marshaller marshaller = context.createMarshaller();
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
				.newInstance();
		DocumentBuilder documentBuilder = documentBuilderFactory
				.newDocumentBuilder();
		Document document = documentBuilder.newDocument();
		marshaller.marshal(objectFactory.createQueryResponse(response),
				document);
		LOG.debug("result document: " + DomTestUtils.domToString(document));

		// verify the content of the DOM result
		Element nsElement = document.createElement("nsElement");
		nsElement.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI,
				"xmlns:dstref", "urn:liberty:dst:2006-08:ref:safe-online");
		nsElement.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI,
				"xmlns:lu", "urn:liberty:util:2006-08");
		nsElement.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI,
				"xmlns:saml", "urn:oasis:names:tc:SAML:2.0:assertion");
		Node resultNode = XPathAPI
				.selectSingleNode(
						document,
						"/dstref:QueryResponse/dstref:Data/saml:Attribute/saml:AttributeValue[1]",
						nsElement);
		assertNotNull(resultNode);
		assertEquals(attributeValue1, resultNode.getTextContent());
		resultNode = XPathAPI
				.selectSingleNode(
						document,
						"/dstref:QueryResponse/dstref:Data/saml:Attribute/saml:AttributeValue[2]",
						nsElement);
		assertNotNull(resultNode);
		assertEquals(attributeValue2, resultNode.getTextContent());
	}
}
