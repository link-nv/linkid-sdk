/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.sdk.ws;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.InputStream;
import javax.xml.soap.*;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import net.link.util.test.web.DomTestUtils;
import net.link.util.test.web.ws.TestSOAPMessageContext;
import net.link.util.ws.security.username.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xml.security.utils.Constants;
import org.apache.xpath.XPathAPI;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.Node;


public class WSSecurityUsernameTokenClientHandlerTest {

    private static final Log LOG = LogFactory.getLog( WSSecurityUsernameTokenClientHandlerTest.class );

    private WSSecurityUsernameTokenHandler testedInstance;
    private final String username = "foo";
    private final String password = "bar";

    @Before
    public void setUp()
            throws Exception {

        testedInstance = new WSSecurityUsernameTokenHandler( new AbstractWSSecurityUsernameTokenCallback() {
            @Override
            public String getUsername() {

                return username;
            }

            @Override
            public String getPassword() {

                return password;
            }

            @Override
            public String handle(final String username) {

                if (username.equals( WSSecurityUsernameTokenClientHandlerTest.this.username ))
                    return password;

                return null;
            }
        } );
    }

    @Test
    public void handleMessageAddsWsSecuritySoapHeader()
            throws Exception {

        // Setup Data
        MessageFactory messageFactory = MessageFactory.newInstance( SOAPConstants.SOAP_1_1_PROTOCOL );
        InputStream testSoapMessageInputStream = WSSecurityUsernameTokenClientHandlerTest.class.getResourceAsStream( "/test-soap-message.xml" );

        SOAPMessage message = messageFactory.createMessage( null, testSoapMessageInputStream );

        SOAPMessageContext soapMessageContext = new TestSOAPMessageContext( message, true );

        // Test
        testedInstance.handleMessage( soapMessageContext );

        // Verify
        SOAPMessage resultMessage = soapMessageContext.getMessage();
        SOAPPart resultSoapPart = resultMessage.getSOAPPart();
        LOG.debug( "result SOAP part: " + DomTestUtils.domToString( resultSoapPart ) );

        Element nsElement = resultSoapPart.createElement( "nsElement" );
        nsElement.setAttributeNS( Constants.NamespaceSpecNS, "xmlns:soap", "http://schemas.xmlsoap.org/soap/envelope/" );
        nsElement.setAttributeNS( Constants.NamespaceSpecNS, "xmlns:wsse",
                "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd" );
        nsElement.setAttributeNS( Constants.NamespaceSpecNS, "xmlns:wsu",
                "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd" );

        Node resultNode = XPathAPI.selectSingleNode( resultSoapPart, "/soap:Envelope/soap:Header/wsse:Security[@soap:mustUnderstand = '1']", nsElement );
        assertNotNull( resultNode );

        // check username
        resultNode = XPathAPI.selectSingleNode( resultSoapPart, "/soap:Envelope/soap:Header/wsse:Security/wsse:UsernameToken/wsse:Username", nsElement );
        assertNotNull( resultNode );
        assertEquals( username, resultNode.getTextContent() );

        // check password
        resultNode = XPathAPI.selectSingleNode( resultSoapPart, "/soap:Envelope/soap:Header/wsse:Security/wsse:UsernameToken/wsse:Password", nsElement );
        assertNotNull( resultNode );

        // check nonce
        resultNode = XPathAPI.selectSingleNode( resultSoapPart, "/soap:Envelope/soap:Header/wsse:Security/wsse:UsernameToken/wsse:Nonce", nsElement );
        assertNotNull( resultNode );

        // check created
        resultNode = XPathAPI.selectSingleNode( resultSoapPart, "/soap:Envelope/soap:Header/wsse:Security/wsse:UsernameToken/wsu:Created", nsElement );
        assertNotNull( resultNode );

        File tmpFile = File.createTempFile( "ws-security-message-", ".xml" );
        DomTestUtils.saveDocument( resultSoapPart, tmpFile );
    }

    @Test
    public void handleMessageInvalidIncomingMessage()
            throws Exception {

        // Setup data
        MessageFactory messageFactory = MessageFactory.newInstance( SOAPConstants.SOAP_1_1_PROTOCOL );
        InputStream testSoapMessageInputStream = WSSecurityServerHandlerTest.class.getResourceAsStream(
                "/test-ws-security-username-token-invalid-message.xml" );
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
        InputStream testSoapMessageInputStream = WSSecurityServerHandlerTest.class.getResourceAsStream( "/test-ws-security-username-token-valid-message.xml" );
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

    @Test
    public void handleMessageUnknownUser()
            throws Exception {

        WSSecurityUsernameTokenHandler instance = new WSSecurityUsernameTokenHandler( new AbstractWSSecurityUsernameTokenCallback() {
            @Override
            public String getUsername() {

                return username;
            }

            @Override
            public String getPassword() {

                return password;
            }

            @Override
            public String handle(final String username) {

                return null;
            }
        } );

        // Setup Data
        MessageFactory messageFactory = MessageFactory.newInstance( SOAPConstants.SOAP_1_1_PROTOCOL );
        InputStream testSoapMessageInputStream = WSSecurityServerHandlerTest.class.getResourceAsStream( "/test-ws-security-username-token-valid-message.xml" );
        assertNotNull( testSoapMessageInputStream );

        SOAPMessage message = messageFactory.createMessage( null, testSoapMessageInputStream );

        SOAPMessageContext soapMessageContext = new TestSOAPMessageContext( message, true );

        // Operate : let client put ws-security header first
        instance.handleMessage( soapMessageContext );

        // switch to inbound for validation
        soapMessageContext.put( MessageContext.MESSAGE_OUTBOUND_PROPERTY, false );

        // Operate : validate ws-security header
        try {
            instance.handleMessage( soapMessageContext );
            fail();
        }
        catch (Exception e) {
            // expected
        }
    }

    @Test
    public void handleMessageAddsWsSecuritySoapHeaderPlainText()
            throws Exception {

        WSSecurityUsernameTokenHandler instance = new WSSecurityUsernameTokenHandler( new AbstractWSSecurityUsernameTokenCallback() {

            @Override
            public boolean isDigestPassword() {

                return false;
            }

            @Override
            public String getUsername() {

                return username;
            }

            @Override
            public String getPassword() {

                return password;
            }

            @Override
            public String handle(final String username) {

                return null;
            }
        } );

        // Setup Data
        MessageFactory messageFactory = MessageFactory.newInstance( SOAPConstants.SOAP_1_1_PROTOCOL );
        InputStream testSoapMessageInputStream = WSSecurityUsernameTokenClientHandlerTest.class.getResourceAsStream( "/test-soap-message.xml" );

        SOAPMessage message = messageFactory.createMessage( null, testSoapMessageInputStream );

        SOAPMessageContext soapMessageContext = new TestSOAPMessageContext( message, true );

        // Test
        instance.handleMessage( soapMessageContext );

        // Verify
        SOAPMessage resultMessage = soapMessageContext.getMessage();
        SOAPPart resultSoapPart = resultMessage.getSOAPPart();
        LOG.debug( "result SOAP part: " + DomTestUtils.domToString( resultSoapPart ) );

        Element nsElement = resultSoapPart.createElement( "nsElement" );
        nsElement.setAttributeNS( Constants.NamespaceSpecNS, "xmlns:soap", "http://schemas.xmlsoap.org/soap/envelope/" );
        nsElement.setAttributeNS( Constants.NamespaceSpecNS, "xmlns:wsse",
                "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd" );
        nsElement.setAttributeNS( Constants.NamespaceSpecNS, "xmlns:wsu",
                "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd" );

        Node resultNode = XPathAPI.selectSingleNode( resultSoapPart, "/soap:Envelope/soap:Header/wsse:Security[@soap:mustUnderstand = '1']", nsElement );
        assertNotNull( resultNode );

        // check username
        resultNode = XPathAPI.selectSingleNode( resultSoapPart, "/soap:Envelope/soap:Header/wsse:Security/wsse:UsernameToken/wsse:Username", nsElement );
        assertNotNull( resultNode );
        assertEquals( username, resultNode.getTextContent() );

        // check password
        resultNode = XPathAPI.selectSingleNode( resultSoapPart, "/soap:Envelope/soap:Header/wsse:Security/wsse:UsernameToken/wsse:Password", nsElement );
        assertNotNull( resultNode );
        assertEquals( password, resultNode.getTextContent() );

        // check nonce
        resultNode = XPathAPI.selectSingleNode( resultSoapPart, "/soap:Envelope/soap:Header/wsse:Security/wsse:UsernameToken/wsse:Nonce", nsElement );
        assertNotNull( resultNode );

        // check created
        resultNode = XPathAPI.selectSingleNode( resultSoapPart, "/soap:Envelope/soap:Header/wsse:Security/wsse:UsernameToken/wsu:Created", nsElement );
        assertNotNull( resultNode );

        File tmpFile = File.createTempFile( "ws-security-message-", ".xml" );
        DomTestUtils.saveDocument( resultSoapPart, tmpFile );
    }

    @Test
    public void handleMessageValidIncomingMessagePlaintext()
            throws Exception {

        WSSecurityUsernameTokenHandler instance = new WSSecurityUsernameTokenHandler( new AbstractWSSecurityUsernameTokenCallback() {

            @Override
            public boolean isDigestPassword() {

                return false;
            }

            @Override
            public String getUsername() {

                return username;
            }

            @Override
            public String getPassword() {

                return password;
            }

            @Override
            public String handle(final String username) {

                return password;
            }
        } );

        // Setup Data
        MessageFactory messageFactory = MessageFactory.newInstance( SOAPConstants.SOAP_1_1_PROTOCOL );
        InputStream testSoapMessageInputStream = WSSecurityServerHandlerTest.class.getResourceAsStream(
                "/test-ws-security-username-token-valid-message-plaintext.xml" );
        assertNotNull( testSoapMessageInputStream );

        SOAPMessage message = messageFactory.createMessage( null, testSoapMessageInputStream );

        SOAPMessageContext soapMessageContext = new TestSOAPMessageContext( message, true );

        // Operate : let client put ws-security header first
        instance.handleMessage( soapMessageContext );

        // switch to inbound for validation
        soapMessageContext.put( MessageContext.MESSAGE_OUTBOUND_PROPERTY, false );

        // Operate : validate ws-security header
        boolean result = instance.handleMessage( soapMessageContext );

        // Verify
        assertTrue( result );
    }
}
