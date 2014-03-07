/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.sdk.auth.saml2;

import static org.junit.Assert.*;

import java.security.KeyPair;
import java.util.UUID;
import net.link.safeonline.sdk.auth.protocol.saml2.LinkIDSaml2Utils;
import net.link.safeonline.sdk.auth.protocol.saml2.LogoutRequestFactory;
import net.link.util.common.DomUtils;
import net.link.util.saml.Saml2Utils;
import net.link.util.test.pkix.PkiTestUtils;
import net.link.util.test.web.DomTestUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xml.security.utils.Constants;
import org.apache.xpath.XPathAPI;
import org.junit.Test;
import org.opensaml.saml2.core.LogoutRequest;
import org.w3c.dom.*;


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
        String samlLogoutRequestToken = DomUtils.domToString( LinkIDSaml2Utils.sign( samlLogoutRequest, keyPair, null ) );
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

        Node sessionInfoNode = XPathAPI.selectSingleNode( resultDocument, "/samlp2:LogoutRequest/samlp2:Extensions/samlp2:SessionInfo/@Session", nsElement );
        assertNotNull( sessionInfoNode );
        assertEquals( session, sessionInfoNode.getTextContent() );

        // verify signature
        assertNotNull( samlLogoutRequest.getSignature() );
        assertNotNull( samlLogoutRequest.getSignature().getKeyInfo() );

        Saml2Utils.validateSignature( samlLogoutRequest.getSignature(), null, null );
    }

    private static Element createNsElement(Document document) {

        Element nsElement = document.createElement( "nsElement" );
        nsElement.setAttributeNS( Constants.NamespaceSpecNS, "xmlns:samlp2", "urn:oasis:names:tc:SAML:2.0:protocol" );
        nsElement.setAttributeNS( Constants.NamespaceSpecNS, "xmlns:saml2", "urn:oasis:names:tc:SAML:2.0:assertion" );
        return nsElement;
    }
}
