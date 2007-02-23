/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.sdk.attrib;

import java.io.InputStream;
import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xml.security.utils.Constants;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import net.link.safeonline.sdk.attrib.WSSecurityClientHandler;
import net.link.safeonline.test.util.DomTestUtils;
import net.link.safeonline.test.util.PkiTestUtils;
import junit.framework.TestCase;

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

		TestSOAPMessageContext soapMessageContext = new TestSOAPMessageContext(
				true);
		soapMessageContext.setMessage(message);

		// operate
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

		Node resultNode = XPathAPI
				.selectSingleNode(
						resultSoapPart,
						"/soap:Envelope/soap:Header/wsse:Security[@soap:mustUnderstand = '1']",
						nsElement);
		assertNotNull(resultNode);
	}

	private static class TestSOAPMessageContext implements SOAPMessageContext {

		private SOAPMessage message;

		private final boolean outbound;

		public TestSOAPMessageContext(boolean outbound) {
			this.outbound = outbound;
		}

		public Object[] getHeaders(QName arg0, JAXBContext arg1, boolean arg2) {
			return null;
		}

		public SOAPMessage getMessage() {
			return this.message;
		}

		public Set<String> getRoles() {
			return null;
		}

		public void setMessage(SOAPMessage message) {
			this.message = message;
		}

		public Scope getScope(String arg0) {
			return null;
		}

		public void setScope(String arg0, Scope arg1) {
		}

		public void clear() {
		}

		public boolean containsKey(Object key) {
			return false;
		}

		public boolean containsValue(Object value) {
			return false;
		}

		public Set<java.util.Map.Entry<String, Object>> entrySet() {
			return null;
		}

		public Object get(Object key) {
			if (MessageContext.MESSAGE_OUTBOUND_PROPERTY.equals(key)) {
				return this.outbound;
			}
			return null;
		}

		public boolean isEmpty() {
			return false;
		}

		public Set<String> keySet() {
			return null;
		}

		public Object put(String key, Object value) {
			return null;
		}

		public void putAll(Map<? extends String, ? extends Object> t) {
		}

		public Object remove(Object key) {
			return null;
		}

		public int size() {
			return 0;
		}

		public Collection<Object> values() {
			return null;
		}
	}
}
