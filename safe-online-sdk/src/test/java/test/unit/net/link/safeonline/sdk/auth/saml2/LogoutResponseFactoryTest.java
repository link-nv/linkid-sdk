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

import net.link.safeonline.sdk.auth.saml2.LogoutResponseFactory;
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


public class LogoutResponseFactoryTest {

    private static final Log LOG = LogFactory.getLog(LogoutResponseFactoryTest.class);


    @Test
    public void createLogoutResponse() throws Exception {

        // setup
        String inResponseTo = "id-in-response-to-test-id";
        String issuerName = "test-issuer-name";
        String destination = "https://sp.test.com";
        KeyPair signerKeyPair = PkiTestUtils.generateKeyPair();

        // operate
        long begin = System.currentTimeMillis();
        String result = LogoutResponseFactory
                .createLogoutResponse(inResponseTo, issuerName, signerKeyPair, destination);
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

        Node inResponseToNode = XPathAPI.selectSingleNode(resultDocument, "/samlp:LogoutResponse/@InResponseTo");
        assertNotNull(inResponseToNode);
        assertEquals(inResponseTo, inResponseToNode.getTextContent());

        Node destinationNode = XPathAPI.selectSingleNode(resultDocument, "/samlp:LogoutResponse/@Destination");
        assertNotNull(destinationNode);
        assertEquals(destination, destinationNode.getTextContent());

        // Document document = responseElement.getOwnerDocument();
        Element nsElement = resultDocument.createElement("nsElement");
        nsElement.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, "xmlns:samlp",
                "urn:oasis:names:tc:SAML:2.0:protocol");
        nsElement.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, "xmlns:saml",
                "urn:oasis:names:tc:SAML:2.0:assertion");

        Node issuerNode = XPathAPI.selectSingleNode(resultDocument, "/samlp:LogoutResponse/saml:Issuer", nsElement);
        assertNotNull(issuerNode);
        assertEquals(issuerName, issuerNode.getTextContent());
    }
}
