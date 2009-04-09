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

import java.io.File;
import java.io.FileOutputStream;
import java.security.KeyPair;
import java.util.Collections;
import java.util.Set;

import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMValidateContext;

import net.link.safeonline.saml.common.Challenge;
import net.link.safeonline.sdk.auth.saml2.AuthnRequestFactory;
import net.link.safeonline.test.util.DomTestUtils;
import net.link.safeonline.test.util.PkiTestUtils;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xml.security.utils.Constants;
import org.apache.xpath.XPathAPI;
import org.apache.xpath.objects.XObject;
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

    private static final Log LOG = LogFactory.getLog(AuthnRequestFactoryTest.class);


    @Test
    public void createAuthnRequest()
            throws Exception {

        // setup
        String applicationName = "test-application-id";
        KeyPair keyPair = PkiTestUtils.generateKeyPair();
        String assertionConsumerServiceURL = "http://test.assertion.consumer.service";
        Challenge<String> challenge = new Challenge<String>();
        String destinationURL = "https://test.idp.com/entry";
        String device = "device";
        String session = "test-session-info";

        // operate
        long begin = System.currentTimeMillis();
        Set<String> devices = Collections.singleton(device);
        String result = AuthnRequestFactory.createAuthnRequest(applicationName, null, null, keyPair, assertionConsumerServiceURL,
                destinationURL, challenge, devices, false, session);
        long end = System.currentTimeMillis();

        // verify
        assertNotNull(result);
        LOG.debug("duration: " + (end - begin) + " ms");
        LOG.debug("result message: " + result);
        File tmpFile = File.createTempFile("saml-authn-request-", ".xml");
        FileOutputStream tmpOutput = new FileOutputStream(tmpFile);
        IOUtils.write(result, tmpOutput);
        IOUtils.closeQuietly(tmpOutput);

        String challengeValue = challenge.getValue();
        LOG.debug("challenge value: " + challengeValue);
        assertNotNull(challengeValue);

        Document resultDocument = DomTestUtils.parseDocument(result);

        Element nsElement = createNsElement(resultDocument);
        Element authnRequestElement = (Element) XPathAPI.selectSingleNode(resultDocument, "/samlp2:AuthnRequest", nsElement);
        assertNotNull(authnRequestElement);

        Element issuerElement = (Element) XPathAPI.selectSingleNode(resultDocument, "/samlp2:AuthnRequest/saml2:Issuer", nsElement);
        assertNotNull(issuerElement);
        assertEquals(applicationName, issuerElement.getTextContent());

        Node resultAssertionConsumerServiceURLNode = XPathAPI.selectSingleNode(resultDocument,
                "/samlp2:AuthnRequest/@AssertionConsumerServiceURL", nsElement);
        assertNotNull(resultAssertionConsumerServiceURLNode);
        assertEquals(assertionConsumerServiceURL, resultAssertionConsumerServiceURLNode.getTextContent());

        Node protocolBindingNode = XPathAPI.selectSingleNode(resultDocument, "/samlp2:AuthnRequest/@ProtocolBinding", nsElement);
        assertNotNull(protocolBindingNode);
        LOG.debug("protocol binding: " + protocolBindingNode.getTextContent());
        assertEquals("urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST", protocolBindingNode.getTextContent());

        Node destinationNode = XPathAPI.selectSingleNode(resultDocument, "/samlp2:AuthnRequest/@Destination", nsElement);
        assertNotNull(destinationNode);
        assertEquals(destinationURL, destinationNode.getTextContent());

        Node allowCreateNode = XPathAPI
                                       .selectSingleNode(resultDocument, "/samlp2:AuthnRequest/samlp2:NameIDPolicy/@AllowCreate", nsElement);
        assertNotNull(allowCreateNode);
        assertEquals("true", allowCreateNode.getTextContent());

        Node sessionInfoNode = XPathAPI.selectSingleNode(resultDocument,
                "/samlp2:AuthnRequest/samlp2:Extensions/samlp2:SessionInfo/@Session", nsElement);
        assertNotNull(sessionInfoNode);
        assertEquals(session, sessionInfoNode.getTextContent());

        // verify signature
        NodeList signatureNodeList = resultDocument.getElementsByTagNameNS(javax.xml.crypto.dsig.XMLSignature.XMLNS, "Signature");
        assertEquals(1, signatureNodeList.getLength());

        DOMValidateContext validateContext = new DOMValidateContext(keyPair.getPublic(), signatureNodeList.item(0));
        XMLSignatureFactory signatureFactory = XMLSignatureFactory.getInstance("DOM", new org.jcp.xml.dsig.internal.dom.XMLDSigRI());

        XMLSignature signature = signatureFactory.unmarshalXMLSignature(validateContext);
        boolean resultValidity = signature.validate(validateContext);
        assertTrue(resultValidity);

        Element dsNsElement = resultDocument.createElement("nsElement");
        dsNsElement.setAttributeNS(Constants.NamespaceSpecNS, "xmlns:ds", "http://www.w3.org/2000/09/xmldsig#");
        XObject xObject = XPathAPI.eval(resultDocument, "count(//ds:Reference)", dsNsElement);
        LOG.debug("count: " + xObject.num());
        assertEquals(1.0, xObject.num(), 0);
    }

    @Test
    public void createAuthnRequestDSAKey()
            throws Exception {

        // setup
        String applicationName = "test-application-id";
        KeyPair keyPair = PkiTestUtils.generateKeyPair("DSA");
        LOG.debug("key pair algo: " + keyPair.getPublic().getAlgorithm());

        // operate
        String result = AuthnRequestFactory.createAuthnRequest(applicationName, null, null, keyPair, null, null, null, null, false, null);
        LOG.debug("result: " + result);
    }

    private Element createNsElement(Document document) {

        Element nsElement = document.createElement("nsElement");
        nsElement.setAttributeNS(Constants.NamespaceSpecNS, "xmlns:samlp2", "urn:oasis:names:tc:SAML:2.0:protocol");
        nsElement.setAttributeNS(Constants.NamespaceSpecNS, "xmlns:saml2", "urn:oasis:names:tc:SAML:2.0:assertion");
        return nsElement;
    }
}
