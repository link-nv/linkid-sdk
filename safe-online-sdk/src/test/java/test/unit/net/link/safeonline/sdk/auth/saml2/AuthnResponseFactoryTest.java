/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.sdk.auth.saml2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.security.KeyPair;

import javax.xml.XMLConstants;

import net.link.safeonline.sdk.auth.saml2.AuthnResponseFactory;
import net.link.safeonline.test.util.DomTestUtils;
import net.link.safeonline.test.util.PkiTestUtils;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xpath.XPathAPI;
import org.junit.Test;
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
		String samlName = "urn:oasis:names:tc:SAML:2.0:ac:classes:PasswordProtectedTransport";
		int validity = 60 * 10;
		String applicationName = "test-application-name";
		String target = "https://sp.test.com";
		KeyPair signerKeyPair = PkiTestUtils.generateKeyPair();

		// operate
		long begin = System.currentTimeMillis();
		String result = AuthnResponseFactory.createAuthResponse(inResponseTo,
				applicationName, issuerName, subjectName, samlName,
				signerKeyPair, validity, target);
		long end = System.currentTimeMillis();

		// verify
		assertNotNull(result);
		LOG.debug("duration: " + (end - begin) + " ms");
		LOG.debug("result message: " + result);
		File tmpFile = File.createTempFile("saml-response-", ".xml");
		FileOutputStream tmpOutput = new FileOutputStream(tmpFile);
		IOUtils.write(result, tmpOutput);
		IOUtils.closeQuietly(tmpOutput);

		Document resultDocument = DomTestUtils.parseDocument(result);

		Node inResponseToNode = XPathAPI.selectSingleNode(resultDocument,
				"/samlp:Response/@InResponseTo");
		assertNotNull(inResponseToNode);
		assertEquals(inResponseTo, inResponseToNode.getTextContent());

		// Document document = responseElement.getOwnerDocument();
		Element nsElement = resultDocument.createElement("nsElement");
		nsElement.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI,
				"xmlns:samlp", "urn:oasis:names:tc:SAML:2.0:protocol");
		nsElement.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI,
				"xmlns:saml", "urn:oasis:names:tc:SAML:2.0:assertion");

		Node issuerNode = XPathAPI.selectSingleNode(resultDocument,
				"/samlp:Response/saml:Issuer", nsElement);
		assertNotNull(issuerNode);
		assertEquals(issuerName, issuerNode.getTextContent());

		Node audienceNode = XPathAPI.selectSingleNode(resultDocument,
				"//saml:Audience", nsElement);
		assertNotNull(audienceNode);
		assertEquals(applicationName, audienceNode.getTextContent());

		Node recipientNode = XPathAPI
				.selectSingleNode(
						resultDocument,
						"/samlp:Response/saml:Assertion/saml:Subject/saml:SubjectConfirmation/saml:SubjectConfirmationData/@Recipient",
						nsElement);
		assertNotNull(recipientNode);
		LOG.debug("recipient: " + recipientNode.getTextContent());
		assertEquals(target, recipientNode.getTextContent());

	}
}
