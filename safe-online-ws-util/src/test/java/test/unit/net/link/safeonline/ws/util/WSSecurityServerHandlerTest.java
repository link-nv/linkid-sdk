/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.ws.util;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.InputStream;
import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.util.Set;
import java.util.UUID;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.transform.dom.DOMSource;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import net.link.safeonline.config.model.ConfigurationManager;
import net.link.safeonline.test.util.DomTestUtils;
import net.link.safeonline.test.util.JndiTestUtils;
import net.link.safeonline.test.util.PkiTestUtils;
import net.link.safeonline.test.util.TestSOAPMessageContext;
import net.link.safeonline.ws.util.ServerCrypto;
import net.link.safeonline.ws.util.WSSecurityServerHandler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.security.WSSecurityEngine;
import org.apache.ws.security.WSSecurityEngineResult;
import org.apache.ws.security.components.crypto.Crypto;
import org.apache.xml.security.Init;
import org.apache.xml.security.algorithms.MessageDigestAlgorithm;
import org.apache.xml.security.c14n.Canonicalizer;
import org.apache.xml.security.signature.XMLSignature;
import org.apache.xml.security.transforms.Transforms;
import org.apache.xml.security.utils.Constants;
import org.apache.xpath.XPathAPI;
import org.bouncycastle.util.encoders.Base64;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Instant;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class WSSecurityServerHandlerTest {

	private static final Log LOG = LogFactory
			.getLog(WSSecurityServerHandlerTest.class);

	private WSSecurityServerHandler testedInstance;

	private JndiTestUtils jndiTestUtils;

	private ConfigurationManager mockConfigurationManager;

	private Object[] mockObjects;

	@Before
	public void setUp() throws Exception {
		this.mockConfigurationManager = createMock(ConfigurationManager.class);
		this.jndiTestUtils = new JndiTestUtils();
		this.jndiTestUtils.setUp();
		this.jndiTestUtils.bindComponent(
				"SafeOnline/ConfigurationManagerBean/local",
				this.mockConfigurationManager);
		this.testedInstance = new WSSecurityServerHandler();
		this.testedInstance.postConstructCallback();

		this.mockObjects = new Object[] { this.mockConfigurationManager };
	}

	@After
	public void tearDown() throws Exception {
		this.jndiTestUtils.tearDown();
	}

	@Test
	public void testHandleMessageAddsCertificateToContext() throws Exception {
		// setup
		MessageFactory messageFactory = MessageFactory
				.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL);
		InputStream testSoapMessageInputStream = WSSecurityServerHandlerTest.class
				.getResourceAsStream("/test-ws-security-message.xml");
		assertNotNull(testSoapMessageInputStream);

		SOAPMessage message = messageFactory.createMessage(null,
				testSoapMessageInputStream);

		SOAPMessageContext soapMessageContext = new TestSOAPMessageContext(
				message, false);

		// stubs
		expect(
				this.mockConfigurationManager
						.getMaximumWsSecurityTimestampOffset()).andStubReturn(
				Long.MAX_VALUE);

		// prepare
		replay(this.mockObjects);

		// operate
		this.testedInstance.handleMessage(soapMessageContext);

		// verify
		verify(this.mockObjects);
		X509Certificate resultCertificate = WSSecurityServerHandler
				.getCertificate(soapMessageContext);
		assertNotNull(resultCertificate);
		assertTrue(WSSecurityServerHandler.isSignedElement("id-21414356",
				soapMessageContext));
		assertFalse(WSSecurityServerHandler.isSignedElement("id-foobar",
				soapMessageContext));
	}

	@Test
	public void testOutboundMessageHasTimestamp() throws Exception {
		// setup
		MessageFactory messageFactory = MessageFactory
				.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL);
		InputStream testSoapMessageInputStream = WSSecurityServerHandlerTest.class
				.getResourceAsStream("/test-soap-message.xml");
		assertNotNull(testSoapMessageInputStream);

		SOAPMessage message = messageFactory.createMessage(null,
				testSoapMessageInputStream);

		SOAPMessageContext soapMessageContext = new TestSOAPMessageContext(
				message, true);

		// prepare
		replay(this.mockObjects);

		// operate
		this.testedInstance.handleMessage(soapMessageContext);

		// verify
		SOAPMessage resultMessage = soapMessageContext.getMessage();
		SOAPPart resultSoapPart = resultMessage.getSOAPPart();
		LOG.debug("result SOAP part: "
				+ DomTestUtils.domToString(resultSoapPart));
		verify(this.mockObjects);
		Element nsElement = resultSoapPart.createElement("nsElement");
		nsElement.setAttributeNS(Constants.NamespaceSpecNS, "xmlns:soap",
				"http://schemas.xmlsoap.org/soap/envelope/");
		nsElement
				.setAttributeNS(
						Constants.NamespaceSpecNS,
						"xmlns:wsse",
						"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd");
		nsElement
				.setAttributeNS(
						Constants.NamespaceSpecNS,
						"xmlns:wsu",
						"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd");
		assertNotNull(
				"missing WS-Security timestamp",
				XPathAPI
						.selectSingleNode(
								resultSoapPart,
								"/soap:Envelope/soap:Header/wsse:Security/wsu:Timestamp/wsu:Created",
								nsElement));
	}

	@Test
	public void testHandleMessageInvalidSoapBody() throws Exception {
		// setup
		MessageFactory messageFactory = MessageFactory
				.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL);
		InputStream testSoapMessageInputStream = WSSecurityServerHandlerTest.class
				.getResourceAsStream("/test-ws-security-invalid-message.xml");
		assertNotNull(testSoapMessageInputStream);

		SOAPMessage message = messageFactory.createMessage(null,
				testSoapMessageInputStream);

		SOAPMessageContext soapMessageContext = new TestSOAPMessageContext(
				message, false);

		// prepare
		replay(this.mockObjects);

		// operate & verify
		try {
			this.testedInstance.handleMessage(soapMessageContext);
			fail();
		} catch (RuntimeException e) {
			// expected
			verify(this.mockObjects);
		}
	}

	static {
		Init.init();
	}

	@Test
	public void signatureCheckingFailsWhenBodyNotSigned() throws Exception {
		// setup
		KeyPair keyPair = PkiTestUtils.generateKeyPair();
		X509Certificate certificate = PkiTestUtils
				.generateSelfSignedCertificate(keyPair, "CN=Test");

		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
				.newInstance();
		documentBuilderFactory.setNamespaceAware(true);
		DocumentBuilder documentBuilder = documentBuilderFactory
				.newDocumentBuilder();
		Document document = documentBuilder.newDocument();

		Element envelopeElement = document.createElementNS(
				"http://schemas.xmlsoap.org/soap/envelope/", "soap:Envelope");
		envelopeElement.setAttributeNS(Constants.NamespaceSpecNS, "xmlns:soap",
				"http://schemas.xmlsoap.org/soap/envelope/");
		document.appendChild(envelopeElement);

		Element headerElement = document.createElementNS(
				"http://schemas.xmlsoap.org/soap/envelope/", "soap:Header");
		envelopeElement.appendChild(headerElement);

		Element securityElement = document
				.createElementNS(
						"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd",
						"wsse:Security");
		securityElement
				.setAttributeNS(
						Constants.NamespaceSpecNS,
						"xmlns:wsse",
						"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd");
		securityElement.setAttributeNS("wsse", "mustUnderstand", "1");
		headerElement.appendChild(securityElement);
		Element binarySecurityTokenElement = document
				.createElementNS(
						"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd",
						"BinarySecurityToken");
		securityElement.appendChild(binarySecurityTokenElement);
		String certId = "id-" + UUID.randomUUID().toString();
		binarySecurityTokenElement
				.setAttributeNS(
						"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd",
						"wsu:Id", certId);
		binarySecurityTokenElement
				.setAttributeNS(
						Constants.NamespaceSpecNS,
						"xmlns:wsu",
						"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd");
		binarySecurityTokenElement
				.setAttributeNS(
						"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd",
						"ValueType",
						"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-x509-token-profile-1.0#X509v3");
		binarySecurityTokenElement
				.setAttributeNS(
						"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd",
						"EncodingType",
						"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-soap-message-security-1.0#Base64Binary");
		binarySecurityTokenElement.setTextContent(new String(Base64
				.encode(certificate.getEncoded())));

		Element timestampElement = document
				.createElementNS(
						"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd",
						"wsu:Timestamp");
		timestampElement
				.setAttributeNS(
						Constants.NamespaceSpecNS,
						"xmlns:wsu",
						"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd");
		String timestampId = "id-" + UUID.randomUUID().toString();
		timestampElement
				.setAttributeNS(
						"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd",
						"wsu:Id", timestampId);
		securityElement.appendChild(timestampElement);
		Element createdElement = document
				.createElementNS(
						"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd",
						"wsu:Created");
		timestampElement.appendChild(createdElement);
		timestampElement.setTextContent(new DateTime().toString());

		Element bodyElement = document.createElementNS(
				"http://schemas.xmlsoap.org/soap/envelope/", "soap:Body");
		String bodyId = "id-" + UUID.randomUUID().toString();
		bodyElement
				.setAttributeNS(
						"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd",
						"wsu:Id", bodyId);
		envelopeElement.appendChild(bodyElement);

		Element sampleElement = document.createElementNS("tns", "tns:test");
		bodyElement.appendChild(sampleElement);
		sampleElement.setAttributeNS(Constants.NamespaceSpecNS, "xmlns:test",
				"urn:test");

		XMLSignature signature = new XMLSignature(document, null,
				XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA512,
				Canonicalizer.ALGO_ID_C14N_EXCL_WITH_COMMENTS);
		securityElement.appendChild(signature.getElement());
		{
			Transforms transforms = new Transforms(document);
			transforms
					.addTransform(Transforms.TRANSFORM_C14N_EXCL_WITH_COMMENTS);
			signature.addDocument("#" + timestampId, transforms,
					MessageDigestAlgorithm.ALGO_ID_DIGEST_SHA512);
		}

		Element securityTokenReferenceElement = document
				.createElementNS(
						"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd",
						"wsse:SecurityTokenReference");
		signature.getKeyInfo().addUnknownElement(securityTokenReferenceElement);
		Element referenceElement = document
				.createElementNS(
						"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd",
						"wsse:Reference");
		securityTokenReferenceElement.appendChild(referenceElement);
		referenceElement
				.setAttributeNS(
						"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd",
						"URI", "#" + certId);
		referenceElement
				.setAttributeNS(
						"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd",
						"ValueType",
						"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-x509-token-profile-1.0#X509v3");

		signature.sign(keyPair.getPrivate());

		LOG.debug("document: " + DomTestUtils.domToString(document));

		// stubs
		expect(
				this.mockConfigurationManager
						.getMaximumWsSecurityTimestampOffset()).andStubReturn(
				Long.MAX_VALUE);

		// prepare
		replay(this.mockObjects);

		// operate
		MessageFactory messageFactory = MessageFactory
				.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL);
		SOAPMessage message = messageFactory.createMessage();
		DOMSource domSource = new DOMSource(document);
		SOAPPart soapPart = message.getSOAPPart();
		soapPart.setContent(domSource);

		message.getSOAPHeader();

		SOAPMessageContext soapMessageContext = new TestSOAPMessageContext(
				message, false);

		// operate & verify
		try {
			this.testedInstance.handleMessage(soapMessageContext);
			fail();
		} catch (RuntimeException e) {
			// expected
			LOG.debug("expected exception: " + e.getMessage());
			assertEquals("SOAP Body was not signed", e.getMessage());
			verify(this.mockObjects);
		}
	}

	@Test
	public void signatureCheckingFailsWhenTimestampNotSigned() throws Exception {
		// setup
		KeyPair keyPair = PkiTestUtils.generateKeyPair();
		X509Certificate certificate = PkiTestUtils
				.generateSelfSignedCertificate(keyPair, "CN=Test");

		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
				.newInstance();
		documentBuilderFactory.setNamespaceAware(true);
		DocumentBuilder documentBuilder = documentBuilderFactory
				.newDocumentBuilder();
		Document document = documentBuilder.newDocument();

		Element envelopeElement = document.createElementNS(
				"http://schemas.xmlsoap.org/soap/envelope/", "soap:Envelope");
		envelopeElement.setAttributeNS(Constants.NamespaceSpecNS, "xmlns:soap",
				"http://schemas.xmlsoap.org/soap/envelope/");
		document.appendChild(envelopeElement);

		Element headerElement = document.createElementNS(
				"http://schemas.xmlsoap.org/soap/envelope/", "soap:Header");
		envelopeElement.appendChild(headerElement);

		Element securityElement = document
				.createElementNS(
						"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd",
						"wsse:Security");
		securityElement
				.setAttributeNS(
						Constants.NamespaceSpecNS,
						"xmlns:wsse",
						"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd");
		securityElement.setAttributeNS("wsse", "mustUnderstand", "1");
		headerElement.appendChild(securityElement);
		Element binarySecurityTokenElement = document
				.createElementNS(
						"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd",
						"BinarySecurityToken");
		securityElement.appendChild(binarySecurityTokenElement);
		String certId = "id-" + UUID.randomUUID().toString();
		binarySecurityTokenElement
				.setAttributeNS(
						"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd",
						"wsu:Id", certId);
		binarySecurityTokenElement
				.setAttributeNS(
						Constants.NamespaceSpecNS,
						"xmlns:wsu",
						"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd");
		binarySecurityTokenElement
				.setAttributeNS(
						"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd",
						"ValueType",
						"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-x509-token-profile-1.0#X509v3");
		binarySecurityTokenElement
				.setAttributeNS(
						"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd",
						"EncodingType",
						"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-soap-message-security-1.0#Base64Binary");
		binarySecurityTokenElement.setTextContent(new String(Base64
				.encode(certificate.getEncoded())));

		Element timestampElement = document
				.createElementNS(
						"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd",
						"wsu:Timestamp");
		timestampElement
				.setAttributeNS(
						Constants.NamespaceSpecNS,
						"xmlns:wsu",
						"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd");
		String timestampId = "id-" + UUID.randomUUID().toString();
		timestampElement
				.setAttributeNS(
						"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd",
						"wsu:Id", timestampId);
		securityElement.appendChild(timestampElement);
		Element createdElement = document
				.createElementNS(
						"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd",
						"wsu:Created");
		timestampElement.appendChild(createdElement);
		timestampElement.setTextContent(new DateTime().toString());

		Element bodyElement = document.createElementNS(
				"http://schemas.xmlsoap.org/soap/envelope/", "soap:Body");
		String bodyId = "id-" + UUID.randomUUID().toString();
		bodyElement
				.setAttributeNS(
						"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd",
						"wsu:Id", bodyId);
		envelopeElement.appendChild(bodyElement);

		Element sampleElement = document.createElementNS("tns", "tns:test");
		bodyElement.appendChild(sampleElement);
		sampleElement.setAttributeNS(Constants.NamespaceSpecNS, "xmlns:test",
				"urn:test");

		XMLSignature signature = new XMLSignature(document, null,
				XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA512,
				Canonicalizer.ALGO_ID_C14N_EXCL_WITH_COMMENTS);
		securityElement.appendChild(signature.getElement());
		{
			Transforms transforms = new Transforms(document);
			transforms
					.addTransform(Transforms.TRANSFORM_C14N_EXCL_WITH_COMMENTS);
			signature.addDocument("#" + bodyId, transforms,
					MessageDigestAlgorithm.ALGO_ID_DIGEST_SHA512);
		}

		Element securityTokenReferenceElement = document
				.createElementNS(
						"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd",
						"wsse:SecurityTokenReference");
		signature.getKeyInfo().addUnknownElement(securityTokenReferenceElement);
		Element referenceElement = document
				.createElementNS(
						"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd",
						"wsse:Reference");
		securityTokenReferenceElement.appendChild(referenceElement);
		referenceElement
				.setAttributeNS(
						"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd",
						"URI", "#" + certId);
		referenceElement
				.setAttributeNS(
						"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd",
						"ValueType",
						"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-x509-token-profile-1.0#X509v3");

		signature.sign(keyPair.getPrivate());

		LOG.debug("document: " + DomTestUtils.domToString(document));

		// stubs
		expect(
				this.mockConfigurationManager
						.getMaximumWsSecurityTimestampOffset()).andStubReturn(
				Long.MAX_VALUE);

		// prepare
		replay(this.mockObjects);

		// operate
		MessageFactory messageFactory = MessageFactory
				.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL);
		SOAPMessage message = messageFactory.createMessage();
		DOMSource domSource = new DOMSource(document);
		SOAPPart soapPart = message.getSOAPPart();
		soapPart.setContent(domSource);

		message.getSOAPHeader();

		SOAPMessageContext soapMessageContext = new TestSOAPMessageContext(
				message, false);

		// operate & verify
		try {
			this.testedInstance.handleMessage(soapMessageContext);
			fail();
		} catch (RuntimeException e) {
			LOG.debug("expected exception: " + e.getMessage());
			assertEquals("Timestamp not signed", e.getMessage());
			// expected
			verify(this.mockObjects);
		}
	}

	@SuppressWarnings("unchecked")
	@Test
	public void wss4j() throws Exception {
		// setup
		KeyPair keyPair = PkiTestUtils.generateKeyPair();
		X509Certificate certificate = PkiTestUtils
				.generateSelfSignedCertificate(keyPair, "CN=Test");

		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
				.newInstance();
		documentBuilderFactory.setNamespaceAware(true);
		DocumentBuilder documentBuilder = documentBuilderFactory
				.newDocumentBuilder();
		Document document = documentBuilder.newDocument();

		Element envelopeElement = document.createElementNS(
				"http://schemas.xmlsoap.org/soap/envelope/", "soap:Envelope");
		envelopeElement.setAttributeNS(Constants.NamespaceSpecNS, "xmlns:soap",
				"http://schemas.xmlsoap.org/soap/envelope/");
		document.appendChild(envelopeElement);

		Element headerElement = document.createElementNS(
				"http://schemas.xmlsoap.org/soap/envelope/", "soap:Header");
		envelopeElement.appendChild(headerElement);

		Element securityElement = document
				.createElementNS(
						"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd",
						"wsse:Security");
		securityElement
				.setAttributeNS(
						Constants.NamespaceSpecNS,
						"xmlns:wsse",
						"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd");
		securityElement.setAttributeNS("wsse", "mustUnderstand", "1");
		headerElement.appendChild(securityElement);
		Element binarySecurityTokenElement = document
				.createElementNS(
						"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd",
						"BinarySecurityToken");
		securityElement.appendChild(binarySecurityTokenElement);
		String certId = "id-" + UUID.randomUUID().toString();
		binarySecurityTokenElement
				.setAttributeNS(
						"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd",
						"wsu:Id", certId);
		binarySecurityTokenElement
				.setAttributeNS(
						Constants.NamespaceSpecNS,
						"xmlns:wsu",
						"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd");
		binarySecurityTokenElement
				.setAttributeNS(
						"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd",
						"ValueType",
						"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-x509-token-profile-1.0#X509v3");
		binarySecurityTokenElement
				.setAttributeNS(
						"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd",
						"EncodingType",
						"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-soap-message-security-1.0#Base64Binary");
		binarySecurityTokenElement.setTextContent(new String(Base64
				.encode(certificate.getEncoded())));

		Element timestampElement = document
				.createElementNS(
						"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd",
						"wsu:Timestamp");
		timestampElement
				.setAttributeNS(
						Constants.NamespaceSpecNS,
						"xmlns:wsu",
						"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd");
		String timestampId = "id-" + UUID.randomUUID().toString();
		timestampElement
				.setAttributeNS(
						"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd",
						"wsu:Id", timestampId);
		securityElement.appendChild(timestampElement);
		Element createdElement = document
				.createElementNS(
						"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd",
						"wsu:Created");
		timestampElement.appendChild(createdElement);
		timestampElement.setTextContent(new DateTime().toString());

		Element bodyElement = document.createElementNS(
				"http://schemas.xmlsoap.org/soap/envelope/", "soap:Body");
		String bodyId = "id-body-" + UUID.randomUUID().toString();
		bodyElement.setAttributeNS(null, "Id", bodyId);
		envelopeElement.appendChild(bodyElement);

		Element sampleElement = document.createElementNS("tns", "tns:test");
		bodyElement.appendChild(sampleElement);
		sampleElement.setAttributeNS(Constants.NamespaceSpecNS, "xmlns:test",
				"urn:test");

		XMLSignature signature = new XMLSignature(document, null,
				XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA512,
				Canonicalizer.ALGO_ID_C14N_EXCL_WITH_COMMENTS);
		securityElement.appendChild(signature.getElement());
		{
			Transforms transforms = new Transforms(document);
			transforms
					.addTransform(Transforms.TRANSFORM_C14N_EXCL_WITH_COMMENTS);
			signature.addDocument("#" + bodyId, transforms,
					MessageDigestAlgorithm.ALGO_ID_DIGEST_SHA512);
		}
		{
			Transforms transforms = new Transforms(document);
			transforms
					.addTransform(Transforms.TRANSFORM_C14N_EXCL_WITH_COMMENTS);
			signature.addDocument("#" + timestampId, transforms,
					MessageDigestAlgorithm.ALGO_ID_DIGEST_SHA512);
		}

		Element securityTokenReferenceElement = document
				.createElementNS(
						"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd",
						"wsse:SecurityTokenReference");
		signature.getKeyInfo().addUnknownElement(securityTokenReferenceElement);
		Element referenceElement = document
				.createElementNS(
						"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd",
						"wsse:Reference");
		securityTokenReferenceElement.appendChild(referenceElement);
		referenceElement
				.setAttributeNS(
						"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd",
						"URI", "#" + certId);
		referenceElement
				.setAttributeNS(
						"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd",
						"ValueType",
						"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-x509-token-profile-1.0#X509v3");

		signature.sign(keyPair.getPrivate());

		LOG.debug("document: " + DomTestUtils.domToString(document));

		// operate
		MessageFactory messageFactory = MessageFactory
				.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL);
		SOAPMessage message = messageFactory.createMessage();
		DOMSource domSource = new DOMSource(document);
		SOAPPart soapPart = message.getSOAPPart();
		soapPart.setContent(domSource);
		soapPart = message.getSOAPPart();
		WSSecurityEngine securityEngine = WSSecurityEngine.getInstance();
		Crypto crypto = new ServerCrypto();
		Vector<WSSecurityEngineResult> wsSecurityEngineResults;
		wsSecurityEngineResults = securityEngine.processSecurityHeader(
				soapPart, null, null, crypto);

		assertNotNull(wsSecurityEngineResults);
		for (WSSecurityEngineResult result : wsSecurityEngineResults) {
			Set<String> signedElements = (Set<String>) result
					.get(WSSecurityEngineResult.TAG_SIGNED_ELEMENT_IDS);
			if (null != signedElements) {
				LOG.debug("signed elements: " + signedElements);
				assertTrue(signedElements.contains(bodyId));
				assertTrue(signedElements.contains(timestampId));
			}
		}
	}

	@Test
	public void maxMillis() throws Exception {
		DateTime dateTime = new DateTime("2007-02-26T15:06:11.824Z");
		LOG.debug("date time: " + dateTime);
		Instant instant = dateTime.toInstant();
		LOG.debug("instant: " + instant);
		DateTime now = new DateTime();
		LOG.debug("now: " + now);
		Instant nowInstant = now.toInstant();
		LOG.debug("now instant: " + nowInstant);
		long diff = Math.abs(nowInstant.getMillis() - instant.getMillis());
		LOG.debug("diff: " + diff);
		LOG.debug("Max: " + Long.MAX_VALUE);
		LOG.debug("diff > MAX?: " + (diff > Long.MAX_VALUE));
		Duration duration = new Duration(instant, nowInstant);
		LOG.debug("duration: " + duration);
	}
}
