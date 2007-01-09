/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.identity;

import java.io.File;
import java.security.KeyPair;
import java.security.cert.X509Certificate;

import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.transform.TransformerException;

import junit.framework.TestCase;
import net.link.safeonline.identity.IdentityStatementFactory;
import net.link.safeonline.p11sc.SmartCard;
import net.link.safeonline.test.util.DomTestUtils;
import net.link.safeonline.test.util.PkiTestUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class IdentityStatementFactoryTest extends TestCase {

	private static final Log LOG = LogFactory
			.getLog(IdentityStatementFactoryTest.class);

	private IdentityStatementFactory testedInstance;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.testedInstance = new IdentityStatementFactory();
	}

	public void testCreateIdentityStatement() throws Exception {
		// setup
		String testGivenName = "test-given-name";
		String testSurname = "test-surname";
		String testStreet = "test-street";
		String testPostalCode = "test-postal-code";
		String testCity = "test-city";
		KeyPair testKeyPair = PkiTestUtils.generateKeyPair();
		X509Certificate testCertificate = PkiTestUtils
				.generateSelfSignedCertificate(testKeyPair, "CN=Test");
		SmartCard testSmartCard = new SoftwareSmartCard(testGivenName,
				"test-surname", "test-street", "test-postal-code", "test-city",
				testKeyPair.getPrivate(), testCertificate);

		// operate
		String result = this.testedInstance
				.createIdentityStatement(testSmartCard);

		// verify: data availability
		assertNotNull(result);
		LOG.debug("result document: " + result);
		Document resultDocument = DomTestUtils.parseDocument(result);
		Element nsElement = resultDocument.createElement("ns");
		nsElement.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:is",
				"urn:net:lin-k:safe-online:identity-statement:1.0");
		nsElement.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:ds",
				"http://www.w3.org/2000/09/xmldsig#");
		Node identityStatementNode = XPathAPI.selectSingleNode(resultDocument,
				"/is:identity-statement", nsElement);
		assertNotNull(identityStatementNode);

		assertXPathEquals(testGivenName, resultDocument,
				"/is:identity-statement/is:identity-data/is:given-name",
				nsElement);
		assertXPathEquals(testSurname, resultDocument,
				"/is:identity-statement/is:identity-data/is:surname", nsElement);
		assertXPathEquals(testStreet, resultDocument,
				"/is:identity-statement/is:identity-data/is:street", nsElement);
		assertXPathEquals(testPostalCode, resultDocument,
				"/is:identity-statement/is:identity-data/is:postal-code",
				nsElement);
		assertXPathEquals(testCity, resultDocument,
				"/is:identity-statement/is:identity-data/is:city", nsElement);

		// verify: certificate availability
		Node x509CertNode = XPathAPI
				.selectSingleNode(
						resultDocument,
						"/is:identity-statement/ds:Signature/ds:KeyInfo/ds:X509Data/ds:X509Certificate",
						nsElement);
		assertNotNull(x509CertNode);

		// verify: signature presence
		NodeList signatureNodeList = resultDocument.getElementsByTagNameNS(
				XMLSignature.XMLNS, "Signature");
		assertFalse(0 == signatureNodeList.getLength());

		// verify: signature
		DOMValidateContext validateContext = new DOMValidateContext(testKeyPair
				.getPublic(), signatureNodeList.item(0));
		XMLSignatureFactory factory = XMLSignatureFactory.getInstance("DOM");
		XMLSignature signature = factory.unmarshalXMLSignature(validateContext);
		assertTrue(signature.validate(validateContext));

		if (false) {
			File tmpFile = File.createTempFile("identity-statement-", ".xml");
			DomTestUtils.saveDocument(resultDocument, tmpFile);
		}

		// verify: tampered signature
		Node surnameNode = XPathAPI
				.selectSingleNode(resultDocument,
						"/is:identity-statement/is:identity-data/is:surname",
						nsElement);
		surnameNode.setTextContent(testSurname + "-foobar");
		signature = factory.unmarshalXMLSignature(validateContext);
		assertFalse(signature.validate(validateContext));
	}

	private void assertXPathEquals(String expected, Node baseNode,
			String xpath, Element nsElement) throws TransformerException {
		Node node = XPathAPI.selectSingleNode(baseNode, xpath, nsElement);
		assertNotNull(xpath, node);
		assertEquals(expected, expected, node.getTextContent());
	}
}
