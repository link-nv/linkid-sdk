/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.net.link.safeonline.saml2.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileOutputStream;

import javax.xml.XMLConstants;

import net.link.safeonline.saml2.util.AuthnResponseFactory;
import net.link.safeonline.saml2.util.SafeOnlineAuthnContextClass;
import net.link.safeonline.test.util.DomTestUtils;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xpath.XPathAPI;
import org.junit.Test;
import org.opensaml.saml2.core.Response;
import org.opensaml.xml.Configuration;
import org.opensaml.xml.io.Marshaller;
import org.opensaml.xml.io.MarshallerFactory;
import org.opensaml.xml.io.MarshallingException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class AuthnResponseFactoryTest {

	private static final Log LOG = LogFactory
			.getLog(AuthnResponseFactoryTest.class);

	@Test
	public void createAuthnResponse() throws Exception {
		// setup
		String inResponseTo = "id-in-response-to-test-id";
		String issuerName = "test-issuer-name";
		String subjectName = "test-subject-name";
		int validity = 60 * 10;
		String applicationName = "test-application-name";
		String target = "https://sp.test.com";

		// operate
		Response response = AuthnResponseFactory.createAuthResponse(
				inResponseTo, applicationName, issuerName, subjectName,
				SafeOnlineAuthnContextClass.PASSWORD_PROTECTED_TRANSPORT,
				validity, target);

		// verify
		assertNotNull(response);

		MarshallerFactory marshallerFactory = Configuration
				.getMarshallerFactory();
		Marshaller marshaller = marshallerFactory.getMarshaller(response);
		Element responseElement;
		try {
			responseElement = marshaller.marshall(response);
		} catch (MarshallingException e) {
			throw new RuntimeException("opensaml2 marshalling error: "
					+ e.getMessage(), e);
		}
		String responseStr = DomTestUtils.domToString(responseElement);
		LOG.debug("response: " + responseStr);
		File tmpFile = File.createTempFile("saml-response-", ".xml");
		IOUtils.write(responseStr, new FileOutputStream(tmpFile));
		LOG.debug("tmp file: " + tmpFile.getAbsolutePath());

		Node inResponseToNode = XPathAPI.selectSingleNode(responseElement,
				"/samlp:Response/@InResponseTo");
		assertNotNull(inResponseToNode);
		assertEquals(inResponseTo, inResponseToNode.getTextContent());

		Document document = responseElement.getOwnerDocument();
		Element nsElement = document.createElement("nsElement");
		nsElement.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI,
				"xmlns:samlp", "urn:oasis:names:tc:SAML:2.0:protocol");
		nsElement.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI,
				"xmlns:saml", "urn:oasis:names:tc:SAML:2.0:assertion");

		Node issuerNode = XPathAPI.selectSingleNode(responseElement,
				"/samlp:Response/saml:Issuer", nsElement);
		assertNotNull(issuerNode);
		assertEquals(issuerName, issuerNode.getTextContent());

		Node audienceNode = XPathAPI.selectSingleNode(responseElement,
				"//saml:Audience", nsElement);
		assertNotNull(audienceNode);
		assertEquals(applicationName, audienceNode.getTextContent());

		Node recipientNode = XPathAPI
				.selectSingleNode(
						responseElement,
						"/samlp:Response/saml:Assertion/saml:Subject/saml:SubjectConfirmation/saml:SubjectConfirmationData/@Recipient",
						nsElement);
		assertNotNull(recipientNode);
		LOG.debug("recipient: " + recipientNode.getTextContent());
		assertEquals(target, recipientNode.getTextContent());
	}
}
