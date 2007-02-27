/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.attrib.ws;

import java.io.InputStream;
import java.security.cert.X509Certificate;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import junit.framework.TestCase;
import net.link.safeonline.attrib.ws.ApplicationCertificateLoginHandler;
import net.link.safeonline.attrib.ws.WSSecurityServerHandler;
import net.link.safeonline.test.util.TestSOAPMessageContext;

public class WSSecurityServerHandlerTest extends TestCase {

	private WSSecurityServerHandler testedInstance;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		this.testedInstance = new WSSecurityServerHandler();
	}

	public void testHandleMessageAddsCertificateToContext() throws Exception {
		// setup
		MessageFactory messageFactory = MessageFactory
				.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL);
		InputStream testSoapMessageInputStream = WSSecurityServerHandlerTest.class
				.getResourceAsStream("/test-ws-security-message.xml");

		SOAPMessage message = messageFactory.createMessage(null,
				testSoapMessageInputStream);

		SOAPMessageContext soapMessageContext = new TestSOAPMessageContext(
				message, false);

		// operate
		this.testedInstance.handleMessage(soapMessageContext);

		// verify
		X509Certificate resultCertificate = (X509Certificate) soapMessageContext
				.get(ApplicationCertificateLoginHandler.CERTIFICATE_PROPERTY);
		assertNotNull(resultCertificate);
	}
}
