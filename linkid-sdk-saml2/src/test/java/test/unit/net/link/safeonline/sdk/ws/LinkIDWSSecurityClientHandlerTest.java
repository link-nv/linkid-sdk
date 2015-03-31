/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.sdk.ws;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.InputStream;
import java.security.KeyPair;
import java.security.PrivateKey;
import javax.xml.soap.*;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import net.link.util.common.CertificateChain;
import net.link.util.test.pkix.PkiTestUtils;
import net.link.util.test.web.DomTestUtils;
import net.link.util.test.web.ws.TestSOAPMessageContext;
import net.link.util.ws.security.x509.AbstractWSSecurityConfiguration;
import net.link.util.ws.security.x509.WSSecurityX509TokenHandler;
import org.apache.xml.security.utils.Constants;
import org.apache.xpath.XPathAPI;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.Node;


public class LinkIDWSSecurityClientHandlerTest {

    private WSSecurityX509TokenHandler testedInstance;

    @Before
    public void setUp()
            throws Exception {

        final KeyPair keyPair = PkiTestUtils.generateKeyPair();
        final CertificateChain certificateChain = new CertificateChain( PkiTestUtils.generateSelfSignedCertificate( keyPair, "CN=Test" ) );

        testedInstance = new WSSecurityX509TokenHandler( new AbstractWSSecurityConfiguration() {
            @Override
            public boolean isCertificateChainTrusted(final CertificateChain aCertificateChain) {

                return certificateChain.equals( aCertificateChain );
            }

            @Override
            public CertificateChain getIdentityCertificateChain() {

                return certificateChain;
            }

            @Override
            public PrivateKey getPrivateKey() {

                return keyPair.getPrivate();
            }
        } );
    }

    @Test
    public void handleMessageAddsWsSecuritySoapHeader()
            throws Exception {

        // Setup Data
        MessageFactory messageFactory = MessageFactory.newInstance( SOAPConstants.SOAP_1_1_PROTOCOL );
        InputStream testSoapMessageInputStream = LinkIDWSSecurityClientHandlerTest.class.getResourceAsStream( "/test-soap-message.xml" );

        SOAPMessage message = messageFactory.createMessage( null, testSoapMessageInputStream );

        SOAPMessageContext soapMessageContext = new TestSOAPMessageContext( message, true );

        // Test
        WSSecurityX509TokenHandler.addSignedElement( soapMessageContext, "test-id" );
        testedInstance.handleMessage( soapMessageContext );

        // Verify
        SOAPMessage resultMessage = soapMessageContext.getMessage();
        SOAPPart resultSoapPart = resultMessage.getSOAPPart();

        Element nsElement = resultSoapPart.createElement( "nsElement" );
        nsElement.setAttributeNS( Constants.NamespaceSpecNS, "xmlns:soap", "http://schemas.xmlsoap.org/soap/envelope/" );
        nsElement.setAttributeNS( Constants.NamespaceSpecNS, "xmlns:wsse",
                "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd" );
        nsElement.setAttributeNS( Constants.NamespaceSpecNS, "xmlns:ds", "http://www.w3.org/2000/09/xmldsig#" );

        Node resultNode = XPathAPI.selectSingleNode( resultSoapPart, "/soap:Envelope/soap:Header/wsse:Security[@soap:mustUnderstand = '1']", nsElement );
        assertNotNull( resultNode );

        resultNode = XPathAPI.selectSingleNode( resultSoapPart,
                "/soap:Envelope/soap:Header/wsse:Security/ds:Signature/ds:SignedInfo/ds:Reference[@URI='#test-id']", nsElement );
        assertNotNull( resultNode );

        assertEquals( 3.0, XPathAPI.eval( resultSoapPart, "count(//ds:Reference)", nsElement ).num() );

        File tmpFile = File.createTempFile( "ws-security-message-", ".xml" );
        DomTestUtils.saveDocument( resultSoapPart, tmpFile );
    }

    @Test
    public void handleMessageInvalidIncomingMessage()
            throws Exception {

        // Setup data
        MessageFactory messageFactory = MessageFactory.newInstance( SOAPConstants.SOAP_1_1_PROTOCOL );
        InputStream testSoapMessageInputStream = LinkIDWSSecurityServerHandlerTest.class.getResourceAsStream( "/test-ws-security-invalid-message.xml" );
        assertNotNull( testSoapMessageInputStream );

        SOAPMessage message = messageFactory.createMessage( null, testSoapMessageInputStream );

        SOAPMessageContext soapMessageContext = new TestSOAPMessageContext( message, false );

        // Operate
        try {
            testedInstance.handleMessage( soapMessageContext );
            fail();
        }
        catch (Exception e) {
            // expected
        }
    }

    @Test
    public void handleMessageValidIncomingMessage()
            throws Exception {

        // Setup Data
        MessageFactory messageFactory = MessageFactory.newInstance( SOAPConstants.SOAP_1_1_PROTOCOL );
        InputStream testSoapMessageInputStream = LinkIDWSSecurityClientHandlerTest.class.getResourceAsStream( "/test-soap-message.xml" );
        assertNotNull( testSoapMessageInputStream );

        SOAPMessage message = messageFactory.createMessage( null, testSoapMessageInputStream );

        SOAPMessageContext soapMessageContext = new TestSOAPMessageContext( message, true );

        // Operate : let client put ws-security header first
        testedInstance.handleMessage( soapMessageContext );

        // switch to inbound for validation
        soapMessageContext.put( MessageContext.MESSAGE_OUTBOUND_PROPERTY, false );

        // Operate : validate ws-security header
        boolean result = testedInstance.handleMessage( soapMessageContext );

        // Verify
        assertTrue( result );
    }
}
