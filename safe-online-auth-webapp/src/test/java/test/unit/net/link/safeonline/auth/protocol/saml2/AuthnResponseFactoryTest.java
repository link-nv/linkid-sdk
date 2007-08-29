/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.auth.protocol.saml2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import javax.xml.XMLConstants;

import net.link.safeonline.auth.protocol.saml2.AuthnResponseFactory;
import net.link.safeonline.auth.protocol.saml2.SafeOnlineAuthnContextClass;
import net.link.safeonline.test.util.DomTestUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xpath.XPathAPI;
import org.junit.Test;
import org.opensaml.Configuration;
import org.opensaml.saml2.core.Response;
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

		// operate
		Response response = AuthnResponseFactory.createAuthResponse(
				inResponseTo, issuerName, subjectName,
				SafeOnlineAuthnContextClass.PASSWORD_PROTECTED_TRANSPORT,
				validity);

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
		LOG.debug("response: " + DomTestUtils.domToString(responseElement));

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
	}
}
