/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.sdk.auth.saml2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.security.KeyPair;

import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMValidateContext;

import net.link.safeonline.sdk.auth.saml2.AuthnRequestFactory;
import net.link.safeonline.sdk.auth.saml2.Challenge;
import net.link.safeonline.test.util.DomTestUtils;
import net.link.safeonline.test.util.PkiTestUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xml.security.utils.Constants;
import org.apache.xpath.XPathAPI;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Unit test for authentication request factory.
 * 
 * @author fcorneli
 * 
 */
public class AuthnRequestFactoryTest {

	private static final Log LOG = LogFactory
			.getLog(AuthnRequestFactoryTest.class);

	@Test
	public void createAuthnRequest() throws Exception {
		// setup
		String applicationName = "test-application-id";
		KeyPair keyPair = PkiTestUtils.generateKeyPair();
		String assertionConsumerServiceURL = "http://test.assertion.consumer.service";
		Challenge<String> challenge = new Challenge<String>();

		// operate
		long begin = System.currentTimeMillis();
		String result = AuthnRequestFactory.createAuthnRequest(applicationName,
				keyPair, assertionConsumerServiceURL, challenge);
		long end = System.currentTimeMillis();

		// verify
		assertNotNull(result);
		LOG.debug("duration: " + (end - begin) + " ms");
		LOG.debug("result message: " + result);

		String challengeValue = challenge.getValue();
		LOG.debug("challenge value: " + challengeValue);
		assertNotNull(challengeValue);

		Document resultDocument = DomTestUtils.parseDocument(result);

		Element nsElement = createNsElement(resultDocument);
		Element authnRequestElement = (Element) XPathAPI.selectSingleNode(
				resultDocument, "/samlp2:AuthnRequest", nsElement);
		assertNotNull(authnRequestElement);

		Element issuerElement = (Element) XPathAPI.selectSingleNode(
				resultDocument, "/samlp2:AuthnRequest/saml2:Issuer", nsElement);
		assertNotNull(issuerElement);
		assertEquals(applicationName, issuerElement.getTextContent());

		Node resultAssertionConsumerServiceURLNode = XPathAPI.selectSingleNode(
				resultDocument,
				"/samlp2:AuthnRequest/@AssertionConsumerServiceURL", nsElement);
		assertNotNull(resultAssertionConsumerServiceURLNode);
		assertEquals(assertionConsumerServiceURL,
				resultAssertionConsumerServiceURLNode.getTextContent());

		// verify signature
		NodeList signatureNodeList = resultDocument.getElementsByTagNameNS(
				javax.xml.crypto.dsig.XMLSignature.XMLNS, "Signature");
		assertEquals(1, signatureNodeList.getLength());

		DOMValidateContext validateContext = new DOMValidateContext(keyPair
				.getPublic(), signatureNodeList.item(0));
		XMLSignatureFactory signatureFactory = XMLSignatureFactory.getInstance(
				"DOM", new org.jcp.xml.dsig.internal.dom.XMLDSigRI());

		XMLSignature signature = signatureFactory
				.unmarshalXMLSignature(validateContext);
		boolean resultValidity = signature.validate(validateContext);
		assertTrue(resultValidity);
	}

	@Test
	public void createAuthnRequestDSAKey() throws Exception {
		// setup
		String applicationName = "test-application-id";
		KeyPair keyPair = PkiTestUtils.generateKeyPair("DSA");
		LOG.debug("key pair algo: " + keyPair.getPublic().getAlgorithm());

		// operate
		String result = AuthnRequestFactory.createAuthnRequest(applicationName,
				keyPair, null, null);
		LOG.debug("result: " + result);
	}

	private Element createNsElement(Document document) {
		Element nsElement = document.createElement("nsElement");
		nsElement.setAttributeNS(Constants.NamespaceSpecNS, "xmlns:samlp2",
				"urn:oasis:names:tc:SAML:2.0:protocol");
		nsElement.setAttributeNS(Constants.NamespaceSpecNS, "xmlns:saml2",
				"urn:oasis:names:tc:SAML:2.0:assertion");
		return nsElement;
	}
}
