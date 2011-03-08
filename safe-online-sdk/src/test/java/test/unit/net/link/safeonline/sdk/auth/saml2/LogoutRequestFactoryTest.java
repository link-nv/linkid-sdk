/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.sdk.auth.saml2;

import static org.junit.Assert.*;

import java.security.KeyPair;
import java.util.UUID;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import net.link.safeonline.sdk.auth.protocol.saml2.LogoutRequestFactory;
import net.link.safeonline.sdk.auth.protocol.saml2.Saml2Util;
import net.link.util.test.pkix.PkiTestUtils;
import net.link.util.test.web.DomTestUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xml.security.utils.Constants;
import org.apache.xpath.XPathAPI;
import org.apache.xpath.objects.XObject;
import org.jcp.xml.dsig.internal.dom.XMLDSigRI;
import org.junit.Test;
import org.opensaml.saml2.core.LogoutRequest;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * Unit test for logout request factory.
 *
 * @author wvdhaute
 */
public class LogoutRequestFactoryTest {

    private static final Log LOG = LogFactory.getLog( LogoutRequestFactoryTest.class );

    @Test
    public void createLogoutRequest()
            throws Exception {

        // Setup Data
        String subjectName = UUID.randomUUID().toString();
        String applicationName = "test-application-id";
        KeyPair keyPair = PkiTestUtils.generateKeyPair();
        String destinationURL = "https://test.idp.com/entry";
        String session = "test-session-info";

        // Test
        long begin = System.currentTimeMillis();
        LogoutRequest samlLogoutRequest = LogoutRequestFactory.createLogoutRequest( subjectName, applicationName, destinationURL, session );
        String samlLogoutRequestToken = Saml2Util.sign( samlLogoutRequest, keyPair, null );
        long end = System.currentTimeMillis();

        // Verify
        assertNotNull( samlLogoutRequestToken );
        LOG.debug( "duration: " + (end - begin) + " ms" );
        LOG.debug( "result message: " + samlLogoutRequestToken );

        Document resultDocument = DomTestUtils.parseDocument( samlLogoutRequestToken );

        Element nsElement = createNsElement( resultDocument );
        Element logoutRequestElement = (Element) XPathAPI.selectSingleNode( resultDocument, "/samlp2:LogoutRequest", nsElement );
        assertNotNull( logoutRequestElement );

        Element issuerElement = (Element) XPathAPI.selectSingleNode( resultDocument, "/samlp2:LogoutRequest/saml2:Issuer", nsElement );
        assertNotNull( issuerElement );
        assertEquals( applicationName, issuerElement.getTextContent() );

        Node destinationNode = XPathAPI.selectSingleNode( resultDocument, "/samlp2:LogoutRequest/@Destination", nsElement );
        assertNotNull( destinationNode );
        assertEquals( destinationURL, destinationNode.getTextContent() );

        Element nameIDElement = (Element) XPathAPI.selectSingleNode( resultDocument, "/samlp2:LogoutRequest/saml2:NameID", nsElement );
        assertNotNull( nameIDElement );
        assertEquals( subjectName, nameIDElement.getTextContent() );

        Node formatNode = XPathAPI.selectSingleNode( resultDocument, "/samlp2:LogoutRequest/saml2:NameID/@Format", nsElement );
        assertNotNull( formatNode );
        assertEquals( "urn:oasis:names:tc:SAML:2.0:nameid-format:entity", formatNode.getTextContent() );

        Node sessionInfoNode = XPathAPI.selectSingleNode( resultDocument,
                                                          "/samlp2:LogoutRequest/samlp2:Extensions/samlp2:SessionInfo/@Session",
                                                          nsElement );
        assertNotNull( sessionInfoNode );
        assertEquals( session, sessionInfoNode.getTextContent() );

        // verify signature
        NodeList signatureNodeList = resultDocument.getElementsByTagNameNS( XMLSignature.XMLNS, "Signature" );
        assertEquals( 1, signatureNodeList.getLength() );

        DOMValidateContext validateContext = new DOMValidateContext( keyPair.getPublic(), signatureNodeList.item( 0 ) );
        XMLSignatureFactory signatureFactory = XMLSignatureFactory.getInstance( "DOM", new XMLDSigRI() );

        XMLSignature signature = signatureFactory.unmarshalXMLSignature( validateContext );
        boolean resultValidity = signature.validate( validateContext );
        assertTrue( resultValidity );

        Element dsNsElement = resultDocument.createElement( "nsElement" );
        dsNsElement.setAttributeNS( Constants.NamespaceSpecNS, "xmlns:ds", "http://www.w3.org/2000/09/xmldsig#" );
        XObject xObject = XPathAPI.eval( resultDocument, "count(//ds:Reference)", dsNsElement );
        LOG.debug( "count: " + xObject.num() );
        assertEquals( 1.0, xObject.num(), 0 );
    }

    private static Element createNsElement(Document document) {

        Element nsElement = document.createElement( "nsElement" );
        nsElement.setAttributeNS( Constants.NamespaceSpecNS, "xmlns:samlp2", "urn:oasis:names:tc:SAML:2.0:protocol" );
        nsElement.setAttributeNS( Constants.NamespaceSpecNS, "xmlns:saml2", "urn:oasis:names:tc:SAML:2.0:assertion" );
        return nsElement;
    }
}
