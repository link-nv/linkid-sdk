/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.sdk.auth.saml2;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileOutputStream;
import java.security.KeyPair;
import javax.xml.XMLConstants;
import net.link.safeonline.sdk.auth.protocol.LogoutProtocolRequestContext;
import net.link.safeonline.sdk.auth.protocol.saml2.LinkIDSaml2Utils;
import net.link.safeonline.sdk.auth.protocol.saml2.LogoutResponseFactory;
import net.link.util.common.DomUtils;
import net.link.util.test.pkix.PkiTestUtils;
import net.link.util.test.web.DomTestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xpath.XPathAPI;
import org.junit.Test;
import org.opensaml.saml2.core.LogoutResponse;
import org.w3c.dom.*;


public class LogoutResponseFactoryTest {

    private static final Log LOG = LogFactory.getLog( LogoutResponseFactoryTest.class );

    @Test
    public void createLogoutResponse()
            throws Exception {

        // Setup Data
        String inResponseTo = "id-in-response-to-test-id";
        String issuerName = "test-issuer-name";
        String destination = "https://sp.test.com";
        KeyPair signerKeyPair = PkiTestUtils.generateKeyPair();

        // Test
        long begin = System.currentTimeMillis();
        LogoutProtocolRequestContext logoutRequest = new LogoutProtocolRequestContext( inResponseTo, null, null, null, null );
        LogoutResponse samlLogoutResponse = LogoutResponseFactory.createLogoutResponse( false, logoutRequest, issuerName, destination );
        String samlLogoutResponseToken = DomUtils.domToString( LinkIDSaml2Utils.sign( samlLogoutResponse, signerKeyPair, null ));
        long end = System.currentTimeMillis();

        // Verify
        assertNotNull( samlLogoutResponseToken );
        LOG.debug( "duration: " + (end - begin) + " ms" );
        LOG.debug( "result message: " + samlLogoutResponseToken );
        File tmpFile = File.createTempFile( "saml-response-", ".xml" );
        FileOutputStream tmpOutput = new FileOutputStream( tmpFile );
        IOUtils.write( samlLogoutResponseToken, tmpOutput );
        IOUtils.closeQuietly( tmpOutput );

        Document resultDocument = DomTestUtils.parseDocument( samlLogoutResponseToken );

        Node inResponseToNode = XPathAPI.selectSingleNode( resultDocument, "/saml2p:LogoutResponse/@InResponseTo" );
        assertNotNull( inResponseToNode );
        assertEquals( inResponseTo, inResponseToNode.getTextContent() );

        Node destinationNode = XPathAPI.selectSingleNode( resultDocument, "/saml2p:LogoutResponse/@Destination" );
        assertNotNull( destinationNode );
        assertEquals( destination, destinationNode.getTextContent() );

        // Document document = responseElement.getOwnerDocument();
        Element nsElement = resultDocument.createElement( "nsElement" );
        nsElement.setAttributeNS( XMLConstants.XMLNS_ATTRIBUTE_NS_URI, "xmlns:saml2p", "urn:oasis:names:tc:SAML:2.0:protocol" );
        nsElement.setAttributeNS( XMLConstants.XMLNS_ATTRIBUTE_NS_URI, "xmlns:saml2", "urn:oasis:names:tc:SAML:2.0:assertion" );

        Node issuerNode = XPathAPI.selectSingleNode( resultDocument, "/saml2p:LogoutResponse/saml2:Issuer", nsElement );
        assertNotNull( issuerNode );
        assertEquals( issuerName, issuerNode.getTextContent() );
    }
}
