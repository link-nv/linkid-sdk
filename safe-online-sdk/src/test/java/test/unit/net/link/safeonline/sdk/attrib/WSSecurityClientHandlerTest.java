/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.sdk.attrib;

import java.io.File;
import java.io.InputStream;
import java.security.KeyPair;
import java.security.cert.X509Certificate;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import junit.framework.TestCase;
import net.link.safeonline.sdk.ws.WSSecurityClientHandler;
import net.link.safeonline.test.util.DomTestUtils;
import net.link.safeonline.test.util.PkiTestUtils;
import net.link.safeonline.test.util.TestSOAPMessageContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xml.security.utils.Constants;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class WSSecurityClientHandlerTest extends TestCase {

	private static final Log LOG = LogFactory
			.getLog(WSSecurityClientHandlerTest.class);

	private WSSecurityClientHandler testedInstance;

	protected void setUp() throws Exception {
		super.setUp();

		KeyPair keyPair = PkiTestUtils.generateKeyPair();
		X509Certificate certificate = PkiTestUtils
				.generateSelfSignedCertificate(keyPair, "CN=Test");

		this.testedInstance = new WSSecurityClientHandler(certificate, keyPair
				.getPrivate());
	}

	public void testHandleMessageAddsWsSecuritySoapHeader() throws Exception {
		// setup
		MessageFactory messageFactory = MessageFactory
				.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL);
		InputStream testSoapMessageInputStream = WSSecurityClientHandlerTest.class
				.getResourceAsStream("/test-soap-message.xml");

		SOAPMessage message = messageFactory.createMessage(null,
				testSoapMessageInputStream);

		SOAPMessageContext soapMessageContext = new TestSOAPMessageContext(
				message, true);

		// operate
		WSSecurityClientHandler.addToBeSignedId("test-id", soapMessageContext);
		this.testedInstance.handleMessage(soapMessageContext);

		// verify
		SOAPMessage resultMessage = soapMessageContext.getMessage();
		SOAPPart resultSoapPart = resultMessage.getSOAPPart();
		LOG.debug("result SOAP part: "
				+ DomTestUtils.domToString(resultSoapPart));

		Element nsElement = resultSoapPart.createElement("nsElement");
		nsElement.setAttributeNS(Constants.NamespaceSpecNS, "xmlns:soap",
				"http://schemas.xmlsoap.org/soap/envelope/");
		nsElement
				.setAttributeNS(
						Constants.NamespaceSpecNS,
						"xmlns:wsse",
						"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd");
		nsElement.setAttributeNS(Constants.NamespaceSpecNS, "xmlns:ds",
				"http://www.w3.org/2000/09/xmldsig#");

		Node resultNode = XPathAPI
				.selectSingleNode(
						resultSoapPart,
						"/soap:Envelope/soap:Header/wsse:Security[@soap:mustUnderstand = '1']",
						nsElement);
		assertNotNull(resultNode);

		resultNode = XPathAPI
				.selectSingleNode(
						resultSoapPart,
						"/soap:Envelope/soap:Header/wsse:Security/ds:Signature/ds:SignedInfo/ds:Reference[@URI='#test-id']",
						nsElement);
		assertNotNull(resultNode);

		File tmpFile = File.createTempFile("ws-security-message-", ".xml");
		DomTestUtils.saveDocument(resultSoapPart, tmpFile);
	}
}
