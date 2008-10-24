/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.sdk.ws;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.InputStream;
import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.util.Set;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.transform.dom.DOMSource;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.MessageContext.Scope;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import net.link.safeonline.sdk.ws.ClientCrypto;
import net.link.safeonline.sdk.ws.ServerCrypto;
import net.link.safeonline.sdk.ws.WSSecurityConfigurationService;
import net.link.safeonline.sdk.ws.WSSecurityServerHandler;
import net.link.safeonline.test.util.DomTestUtils;
import net.link.safeonline.test.util.JndiTestUtils;
import net.link.safeonline.test.util.PkiTestUtils;
import net.link.safeonline.test.util.TestSOAPMessageContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.WSEncryptionPart;
import org.apache.ws.security.WSSecurityEngine;
import org.apache.ws.security.WSSecurityEngineResult;
import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.components.crypto.Crypto;
import org.apache.ws.security.message.WSSecHeader;
import org.apache.ws.security.message.WSSecSignature;
import org.apache.ws.security.message.WSSecTimestamp;
import org.apache.ws.security.util.WSSecurityUtil;
import org.apache.xml.security.Init;
import org.apache.xml.security.utils.Constants;
import org.apache.xpath.XPathAPI;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Instant;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


public class WSSecurityServerHandlerTest {

    private static final Log               LOG = LogFactory.getLog(WSSecurityServerHandlerTest.class);

    private WSSecurityServerHandler        testedInstance;

    private JndiTestUtils                  jndiTestUtils;

    private WSSecurityConfigurationService mockWSSecurityConfigurationService;

    private Object[]                       mockObjects;


    @Before
    public void setUp() throws Exception {

        this.jndiTestUtils = new JndiTestUtils();
        this.jndiTestUtils.setUp();
        this.jndiTestUtils.bindComponent("java:comp/env/wsSecurityConfigurationServiceJndiName",
                "SafeOnline/WSSecurityConfigurationBean/local");

        this.mockWSSecurityConfigurationService = createMock(WSSecurityConfigurationService.class);

        this.jndiTestUtils.bindComponent("SafeOnline/WSSecurityConfigurationBean/local", this.mockWSSecurityConfigurationService);
        this.testedInstance = new WSSecurityServerHandler();
        this.testedInstance.postConstructCallback();

        this.mockObjects = new Object[] { this.mockWSSecurityConfigurationService };
    }

    @After
    public void tearDown() throws Exception {

        this.jndiTestUtils.tearDown();
    }

    @Test
    public void testHandleMessageAddsCertificateToContext() throws Exception {

        // setup
        MessageFactory messageFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL);
        InputStream testSoapMessageInputStream = WSSecurityServerHandlerTest.class.getResourceAsStream("/test-ws-security-message.xml");
        assertNotNull(testSoapMessageInputStream);

        SOAPMessage message = messageFactory.createMessage(null, testSoapMessageInputStream);

        SOAPMessageContext soapMessageContext = new TestSOAPMessageContext(message, false);

        // stubs
        expect(this.mockWSSecurityConfigurationService.getMaximumWsSecurityTimestampOffset()).andStubReturn(Long.MAX_VALUE);

        // prepare
        replay(this.mockObjects);

        // operate
        this.testedInstance.handleMessage(soapMessageContext);

        // verify
        verify(this.mockObjects);
        X509Certificate resultCertificate = WSSecurityServerHandler.getCertificate(soapMessageContext);
        assertNotNull(resultCertificate);
        assertTrue(WSSecurityServerHandler.isSignedElement("id-21414356", soapMessageContext));
        assertFalse(WSSecurityServerHandler.isSignedElement("id-foobar", soapMessageContext));
    }

    @Test
    public void testOutboundMessageHasTimestamp() throws Exception {

        // setup
        KeyPair keyPair = PkiTestUtils.generateKeyPair();
        X509Certificate certificate = PkiTestUtils.generateSelfSignedCertificate(keyPair, "CN=Test");

        MessageFactory messageFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL);
        InputStream testSoapMessageInputStream = WSSecurityServerHandlerTest.class.getResourceAsStream("/test-soap-message.xml");
        assertNotNull(testSoapMessageInputStream);

        SOAPMessage message = messageFactory.createMessage(null, testSoapMessageInputStream);

        SOAPMessageContext soapMessageContext = new TestSOAPMessageContext(message, true);
        soapMessageContext.put(WSSecurityServerHandler.CERTIFICATE_PROPERTY, certificate);
        soapMessageContext.setScope(WSSecurityServerHandler.CERTIFICATE_PROPERTY, Scope.APPLICATION);

        // stubs
        expect(this.mockWSSecurityConfigurationService.skipMessageIntegrityCheck(certificate)).andStubReturn(true);

        // prepare
        replay(this.mockObjects);

        // operate
        this.testedInstance.handleMessage(soapMessageContext);

        // verify
        SOAPMessage resultMessage = soapMessageContext.getMessage();
        SOAPPart resultSoapPart = resultMessage.getSOAPPart();
        LOG.debug("result SOAP part: " + DomTestUtils.domToString(resultSoapPart));
        verify(this.mockObjects);
        Element nsElement = resultSoapPart.createElement("nsElement");
        nsElement.setAttributeNS(Constants.NamespaceSpecNS, "xmlns:soap", "http://schemas.xmlsoap.org/soap/envelope/");
        nsElement.setAttributeNS(Constants.NamespaceSpecNS, "xmlns:wsse",
                "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd");
        nsElement.setAttributeNS(Constants.NamespaceSpecNS, "xmlns:wsu",
                "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd");
        assertNotNull("missing WS-Security timestamp", XPathAPI.selectSingleNode(resultSoapPart,
                "/soap:Envelope/soap:Header/wsse:Security/wsu:Timestamp/wsu:Created", nsElement));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testOutboundMessageSigned() throws Exception {

        // setup
        KeyPair keyPair = PkiTestUtils.generateKeyPair();
        X509Certificate certificate = PkiTestUtils.generateSelfSignedCertificate(keyPair, "CN=Test");

        KeyPair olasKeyPair = PkiTestUtils.generateKeyPair();
        X509Certificate olasCertificate = PkiTestUtils.generateSelfSignedCertificate(olasKeyPair, "CN=OLAS");

        MessageFactory messageFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL);
        InputStream testSoapMessageInputStream = WSSecurityServerHandlerTest.class.getResourceAsStream("/test-soap-message.xml");
        assertNotNull(testSoapMessageInputStream);

        SOAPMessage message = messageFactory.createMessage(null, testSoapMessageInputStream);

        SOAPMessageContext soapMessageContext = new TestSOAPMessageContext(message, true);
        soapMessageContext.put(WSSecurityServerHandler.CERTIFICATE_PROPERTY, certificate);
        soapMessageContext.setScope(WSSecurityServerHandler.CERTIFICATE_PROPERTY, Scope.APPLICATION);

        // stubs
        expect(this.mockWSSecurityConfigurationService.skipMessageIntegrityCheck(certificate)).andStubReturn(false);
        expect(this.mockWSSecurityConfigurationService.getCertificate()).andStubReturn(olasCertificate);
        expect(this.mockWSSecurityConfigurationService.getPrivateKey()).andStubReturn(olasKeyPair.getPrivate());
        expect(this.mockWSSecurityConfigurationService.getMaximumWsSecurityTimestampOffset()).andStubReturn(Long.MAX_VALUE);

        // prepare
        replay(this.mockObjects);

        // operate
        this.testedInstance.handleMessage(soapMessageContext);

        // verify signed message
        SOAPMessage signedMessage = soapMessageContext.getMessage();
        SOAPPart signedSoapPart = signedMessage.getSOAPPart();
        LOG.debug("signed SOAP part:" + DomTestUtils.domToString(signedSoapPart));
        soapMessageContext.put(MessageContext.MESSAGE_OUTBOUND_PROPERTY, false);

        this.testedInstance.handleMessage(soapMessageContext);

        // verify
        verify(this.mockObjects);
        X509Certificate resultCertificate = WSSecurityServerHandler.getCertificate(soapMessageContext);
        assertNotNull(resultCertificate);
        Set<String> signedElements = (Set<String>) soapMessageContext.get(WSSecurityServerHandler.SIGNED_ELEMENTS_CONTEXT_KEY);
        assertEquals(2, signedElements.size());
        LOG.debug("signed elements: " + signedElements);
    }

    @Test
    public void testHandleMessageInvalidSoapBody() throws Exception {

        // setup
        MessageFactory messageFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL);
        InputStream testSoapMessageInputStream = WSSecurityServerHandlerTest.class
                                                                                  .getResourceAsStream("/test-ws-security-invalid-message.xml");
        assertNotNull(testSoapMessageInputStream);

        SOAPMessage message = messageFactory.createMessage(null, testSoapMessageInputStream);

        SOAPMessageContext soapMessageContext = new TestSOAPMessageContext(message, false);

        // prepare
        replay(this.mockObjects);

        // operate & verify
        try {
            this.testedInstance.handleMessage(soapMessageContext);
            fail();
        } catch (RuntimeException e) {
            // expected
            verify(this.mockObjects);
        }
    }


    static {
        Init.init();
    }


    @Test
    public void signatureCheckingFailsWhenTimestampNotSigned() throws Exception {

        // setup
        KeyPair keyPair = PkiTestUtils.generateKeyPair();
        X509Certificate certificate = PkiTestUtils.generateSelfSignedCertificate(keyPair, "CN=Test");

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document document = documentBuilder.parse(WSSecurityServerHandlerTest.class.getResourceAsStream("/test-soap-message.xml"));

        // use WSSecurityClientHandler to sign message
        LOG.debug("adding WS-Security SOAP header");
        WSSecSignature wsSecSignature = new WSSecSignature();
        wsSecSignature.setKeyIdentifierType(WSConstants.BST_DIRECT_REFERENCE);
        Crypto crypto = new ClientCrypto(certificate, keyPair.getPrivate());
        WSSecHeader wsSecHeader = new WSSecHeader();
        wsSecHeader.insertSecurityHeader(document);
        try {
            wsSecSignature.prepare(document, crypto, wsSecHeader);

            org.apache.ws.security.SOAPConstants soapConstants = WSSecurityUtil.getSOAPConstants(document.getDocumentElement());

            Vector<WSEncryptionPart> wsEncryptionParts = new Vector<WSEncryptionPart>();
            WSEncryptionPart wsEncryptionPart = new WSEncryptionPart(soapConstants.getBodyQName().getLocalPart(),
                    soapConstants.getEnvelopeURI(), "Content");
            wsEncryptionParts.add(wsEncryptionPart);

            WSSecTimestamp wsSecTimeStamp = new WSSecTimestamp();
            wsSecTimeStamp.setTimeToLive(0);
            /*
             * If ttl is zero then there will be no Expires element within the Timestamp. Eventually we want to let the service itself
             * decide how long the message validity period is.
             */
            wsSecTimeStamp.prepare(document);
            wsSecTimeStamp.prependToHeader(wsSecHeader);

            wsSecSignature.addReferencesToSign(wsEncryptionParts, wsSecHeader);

            wsSecSignature.prependToHeader(wsSecHeader);

            wsSecSignature.prependBSTElementToHeader(wsSecHeader);

            wsSecSignature.computeSignature();

        } catch (WSSecurityException e) {
            throw new RuntimeException("WSS4J error: " + e.getMessage(), e);
        }

        LOG.debug("document: " + DomTestUtils.domToString(document));

        // stubs
        expect(this.mockWSSecurityConfigurationService.getMaximumWsSecurityTimestampOffset()).andStubReturn(Long.MAX_VALUE);

        // prepare
        replay(this.mockObjects);

        // operate
        MessageFactory messageFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL);
        SOAPMessage message = messageFactory.createMessage();
        DOMSource domSource = new DOMSource(document);
        SOAPPart soapPart = message.getSOAPPart();
        soapPart.setContent(domSource);

        message.getSOAPHeader();

        SOAPMessageContext soapMessageContext = new TestSOAPMessageContext(message, false);

        // operate & verify
        try {
            this.testedInstance.handleMessage(soapMessageContext);
            fail();
        } catch (RuntimeException e) {
            LOG.debug("expected exception: ", e);
            assertEquals("Timestamp not signed", e.getMessage());
            // expected
            verify(this.mockObjects);
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void wss4j() throws Exception {

        // setup
        KeyPair keyPair = PkiTestUtils.generateKeyPair();
        X509Certificate certificate = PkiTestUtils.generateSelfSignedCertificate(keyPair, "CN=Test");

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document document = documentBuilder.parse(WSSecurityServerHandlerTest.class.getResourceAsStream("/test-soap-message.xml"));

        // use WSSecurityClientHandler to sign message
        LOG.debug("adding WS-Security SOAP header");
        WSSecSignature wsSecSignature = new WSSecSignature();
        wsSecSignature.setKeyIdentifierType(WSConstants.BST_DIRECT_REFERENCE);
        Crypto clientCrypto = new ClientCrypto(certificate, keyPair.getPrivate());
        WSSecHeader wsSecHeader = new WSSecHeader();
        wsSecHeader.insertSecurityHeader(document);
        try {
            wsSecSignature.prepare(document, clientCrypto, wsSecHeader);

            String testId = "test-id";
            Vector<WSEncryptionPart> wsEncryptionParts = new Vector<WSEncryptionPart>();
            WSEncryptionPart wsBodyEncryptionPart = new WSEncryptionPart(testId);
            wsEncryptionParts.add(wsBodyEncryptionPart);

            WSSecTimestamp wsSecTimeStamp = new WSSecTimestamp();
            wsSecTimeStamp.setTimeToLive(0);
            /*
             * If ttl is zero then there will be no Expires element within the Timestamp. Eventually we want to let the service itself
             * decide how long the message validity period is.
             */
            wsSecTimeStamp.prepare(document);
            wsSecTimeStamp.prependToHeader(wsSecHeader);
            wsEncryptionParts.add(new WSEncryptionPart(wsSecTimeStamp.getId()));

            wsSecSignature.addReferencesToSign(wsEncryptionParts, wsSecHeader);

            wsSecSignature.prependToHeader(wsSecHeader);

            wsSecSignature.prependBSTElementToHeader(wsSecHeader);

            wsSecSignature.computeSignature();

            LOG.debug("document: " + DomTestUtils.domToString(document));

            // operate
            MessageFactory messageFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL);
            SOAPMessage message = messageFactory.createMessage();
            DOMSource domSource = new DOMSource(document);
            SOAPPart soapPart = message.getSOAPPart();
            soapPart.setContent(domSource);
            soapPart = message.getSOAPPart();
            WSSecurityEngine securityEngine = WSSecurityEngine.getInstance();
            Crypto serverCrypto = new ServerCrypto();
            Vector<WSSecurityEngineResult> wsSecurityEngineResults;
            wsSecurityEngineResults = securityEngine.processSecurityHeader(soapPart, null, null, serverCrypto);

            assertNotNull(wsSecurityEngineResults);
            for (WSSecurityEngineResult result : wsSecurityEngineResults) {
                Set<String> signedElements = (Set<String>) result.get(WSSecurityEngineResult.TAG_SIGNED_ELEMENT_IDS);
                if (null != signedElements) {
                    LOG.debug("signed elements: " + signedElements);
                    assertTrue(signedElements.contains(testId));
                    assertTrue(signedElements.contains(wsSecTimeStamp.getId()));
                }
            }
        } catch (WSSecurityException e) {
            throw new RuntimeException("WSS4J error: " + e.getMessage(), e);
        }
    }

    @Test
    public void maxMillis() throws Exception {

        DateTime dateTime = new DateTime("2007-02-26T15:06:11.824Z");
        LOG.debug("date time: " + dateTime);
        Instant instant = dateTime.toInstant();
        LOG.debug("instant: " + instant);
        DateTime now = new DateTime();
        LOG.debug("now: " + now);
        Instant nowInstant = now.toInstant();
        LOG.debug("now instant: " + nowInstant);
        long diff = Math.abs(nowInstant.getMillis() - instant.getMillis());
        LOG.debug("diff: " + diff);
        LOG.debug("Max: " + Long.MAX_VALUE);
        LOG.debug("diff > MAX?: " + (diff > Long.MAX_VALUE));
        Duration duration = new Duration(instant, nowInstant);
        LOG.debug("duration: " + duration);
    }
}
