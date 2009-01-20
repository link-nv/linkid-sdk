/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.ws;

import java.io.StringWriter;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import javax.annotation.PostConstruct;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.MessageContext.Scope;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import net.link.safeonline.util.ee.EjbUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.security.SOAPConstants;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.WSEncryptionPart;
import org.apache.ws.security.WSSecurityEngine;
import org.apache.ws.security.WSSecurityEngineResult;
import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.components.crypto.Crypto;
import org.apache.ws.security.message.WSSecHeader;
import org.apache.ws.security.message.WSSecSignature;
import org.apache.ws.security.message.WSSecTimestamp;
import org.apache.ws.security.message.token.Timestamp;
import org.joda.time.DateTime;
import org.joda.time.Instant;
import org.w3c.dom.Node;


/**
 * JAX-WS SOAP Handler that provides WS-Security server-side verification.
 * 
 * @author fcorneli
 * 
 */
public class WSSecurityServerHandler implements SOAPHandler<SOAPMessageContext> {

    private static final Log               LOG                         = LogFactory.getLog(WSSecurityServerHandler.class);

    public static final String             CERTIFICATE_PROPERTY        = WSSecurityServerHandler.class + ".x509";

    public static final String             SIGNED_ELEMENTS_CONTEXT_KEY = WSSecurityServerHandler.class + ".signed.elements";

    private WSSecurityConfigurationService wsSecurityConfigurationService;

    private String                         wsSecurityConfigurationServiceJndiName;

    private boolean                        wsSecurityOptionalInboudSignature;


    @PostConstruct
    public void postConstructCallback() {

        loadDependencies();
        System.setProperty("com.sun.xml.ws.fault.SOAPFaultBuilder.disableCaptureStackTrace", "true");
        wsSecurityConfigurationService = EjbUtils.getEJB(wsSecurityConfigurationServiceJndiName,
                WSSecurityConfigurationService.class);
    }

    private void loadDependencies() {

        try {
            Context ctx = new javax.naming.InitialContext();
            Context env = (Context) ctx.lookup("java:comp/env");
            wsSecurityConfigurationServiceJndiName = (String) env.lookup("wsSecurityConfigurationServiceJndiName");
            wsSecurityOptionalInboudSignature = (Boolean) env.lookup("wsSecurityOptionalInboudSignature");
        } catch (NamingException e) {
            LOG.debug("naming exception: " + e.getMessage());
            throw new RuntimeException("WS Security Configuration JNDI path or \"wsSecurityOptionalInboudSignature\" not specified");
        }
    }

    public Set<QName> getHeaders() {

        Set<QName> headers = new HashSet<QName>();
        headers.add(new QName("http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd", "Security"));
        return headers;
    }

    public void close(@SuppressWarnings("unused") MessageContext messageContext) {

        // empty
    }

    public boolean handleFault(@SuppressWarnings("unused") SOAPMessageContext soapMessageContext) {

        return true;
    }

    public boolean handleMessage(SOAPMessageContext soapMessageContext) {

        Boolean outboundProperty = (Boolean) soapMessageContext.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);

        SOAPMessage soapMessage = soapMessageContext.getMessage();
        SOAPPart soapPart = soapMessage.getSOAPPart();

        if (true == outboundProperty.booleanValue()) {
            handleOutboundDocument(soapPart, soapMessageContext);
            return true;
        }

        handleInboundDocument(soapPart, soapMessageContext);

        return true;
    }

    /**
     * Handles the outbound SOAP message. This method will add an unsigned WS-Security Timestamp in the SOAP header in case the message
     * integrity check is set to false. This means the .NET WCF BasicHttpBinding over SSL is used. The unsigned timestamp is required for
     * .NET 2/3 clients by the WCF framework.
     * 
     * If the OLAS binding ( AssymetricBinding without encryption in .NET WCF ) is used a signed timestamp will be added in the SOAP header.
     * This is required for .NET 2/3 clients by the WCF framework.
     * 
     * @param document
     */
    private void handleOutboundDocument(SOAPPart document, SOAPMessageContext soapMessageContext) {

        LOG.debug("handle outbound document");

        boolean skipMessageIntegrityCheck = false;
        if (wsSecurityOptionalInboudSignature) {
            LOG.debug("inbound message is set to optional signed");
            skipMessageIntegrityCheck = false;
        } else {
            X509Certificate certificate = getCertificate(soapMessageContext);
            if (null == certificate)
                throw new RuntimeException("no certificate found on JAX-WS context");
            skipMessageIntegrityCheck = wsSecurityConfigurationService.skipMessageIntegrityCheck(certificate);
        }

        if (skipMessageIntegrityCheck) {
            WSSecHeader wsSecHeader = new WSSecHeader();
            wsSecHeader.insertSecurityHeader(document);
            WSSecTimestamp wsSecTimeStamp = new WSSecTimestamp();
            wsSecTimeStamp.setTimeToLive(0);
            wsSecTimeStamp.prepare(document);
            wsSecTimeStamp.prependToHeader(wsSecHeader);
        } else {
            LOG.debug("adding WS-Security SOAP header");

            WSSecHeader wsSecHeader = new WSSecHeader();
            wsSecHeader.insertSecurityHeader(document);
            WSSecSignature wsSecSignature = new WSSecSignature();
            wsSecSignature.setKeyIdentifierType(WSConstants.BST_DIRECT_REFERENCE);
            Crypto crypto = new ClientCrypto(wsSecurityConfigurationService.getCertificate(),
                    wsSecurityConfigurationService.getPrivateKey());
            try {
                wsSecSignature.prepare(document, crypto, wsSecHeader);

                SOAPConstants soapConstants = org.apache.ws.security.util.WSSecurityUtil.getSOAPConstants(document.getDocumentElement());

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
                wsEncryptionParts.add(new WSEncryptionPart(wsSecTimeStamp.getId()));

                wsSecSignature.addReferencesToSign(wsEncryptionParts, wsSecHeader);

                wsSecSignature.prependToHeader(wsSecHeader);

                wsSecSignature.prependBSTElementToHeader(wsSecHeader);

                wsSecSignature.computeSignature();

            } catch (WSSecurityException e) {
                throw new RuntimeException("WSS4J error: " + e.getMessage(), e);
            }

            try {
                LOG.debug("document: " + domToString(document));
            } catch (TransformerException e1) {
                LOG.debug("transformer exception");
            }
        }
    }

    private String domToString(Node domNode)
            throws TransformerException {

        Source source = new DOMSource(domNode);
        StringWriter stringWriter = new StringWriter();
        Result result = new StreamResult(stringWriter);
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.transform(source, result);
        return stringWriter.toString();
    }

    @SuppressWarnings("unchecked")
    private void handleInboundDocument(SOAPPart document, SOAPMessageContext soapMessageContext) {

        LOG.debug("WS-Security header validation");
        WSSecurityEngine securityEngine = WSSecurityEngine.getInstance();
        Crypto crypto = new ServerCrypto();

        Vector<WSSecurityEngineResult> wsSecurityEngineResults;
        try {
            wsSecurityEngineResults = securityEngine.processSecurityHeader(document, null, null, crypto);
        } catch (WSSecurityException e) {
            LOG.debug("WS-Security error: " + e.getMessage(), e);
            throw WSSecurityUtil.createSOAPFaultException("The signature or decryption was invalid", "FailedCheck");
        }
        LOG.debug("results: " + wsSecurityEngineResults);
        if (null == wsSecurityEngineResults) {
            if (wsSecurityOptionalInboudSignature) {
                LOG.debug("inbound message is set to optional signed");
                return;
            }
            throw WSSecurityUtil.createSOAPFaultException("An error was discovered processing the <wsse:Security> header.",
                    "InvalidSecurity");
        }
        Timestamp timestamp = null;
        Set<String> signedElements = null;
        for (WSSecurityEngineResult result : wsSecurityEngineResults) {
            Set<String> resultSignedElements = (Set<String>) result.get(WSSecurityEngineResult.TAG_SIGNED_ELEMENT_IDS);
            if (null != resultSignedElements) {
                signedElements = resultSignedElements;
            }
            X509Certificate certificate = (X509Certificate) result.get(WSSecurityEngineResult.TAG_X509_CERTIFICATE);
            if (null != certificate) {
                setCertificate(soapMessageContext, certificate);
            }

            Timestamp resultTimestamp = (Timestamp) result.get(WSSecurityEngineResult.TAG_TIMESTAMP);
            if (null != resultTimestamp) {
                timestamp = resultTimestamp;
            }
        }

        if (null == signedElements)
            throw WSSecurityUtil.createSOAPFaultException("The signature or decryption was invalid", "FailedCheck");
        LOG.debug("signed elements: " + signedElements);
        soapMessageContext.put(SIGNED_ELEMENTS_CONTEXT_KEY, signedElements);

        /*
         * Check timestamp.
         */
        if (null == timestamp)
            throw WSSecurityUtil.createSOAPFaultException("missing Timestamp in WS-Security header", "InvalidSecurity");
        String timestampId = timestamp.getID();
        if (false == signedElements.contains(timestampId))
            throw WSSecurityUtil.createSOAPFaultException("Timestamp not signed", "FailedCheck");
        Calendar created = timestamp.getCreated();
        long maxOffset = wsSecurityConfigurationService.getMaximumWsSecurityTimestampOffset();
        DateTime createdDateTime = new DateTime(created);
        Instant createdInstant = createdDateTime.toInstant();
        Instant nowInstant = new DateTime().toInstant();
        long offset = Math.abs(createdInstant.getMillis() - nowInstant.getMillis());
        if (offset > maxOffset) {
            LOG.debug("timestamp offset: " + offset);
            LOG.debug("maximum allowed offset: " + maxOffset);
            throw WSSecurityUtil.createSOAPFaultException("WS-Security Created Timestamp offset exceeded", "FailedCheck");
        }
    }

    private static void setCertificate(SOAPMessageContext context, X509Certificate certificate) {

        context.put(CERTIFICATE_PROPERTY, certificate);
        context.setScope(CERTIFICATE_PROPERTY, Scope.APPLICATION);
    }

    /**
     * Gives back the X509 certificate that was set previously by a WS-Security handler.
     * 
     * @param context
     */
    public static X509Certificate getCertificate(SOAPMessageContext context) {

        X509Certificate certificate = (X509Certificate) context.get(CERTIFICATE_PROPERTY);
        return certificate;
    }

    /**
     * Gives back the X509 certificate that was set previously by a WS-Security handler.
     * 
     * @param context
     */
    public static X509Certificate getCertificate(WebServiceContext context) {

        MessageContext messageContext = context.getMessageContext();
        X509Certificate certificate = (X509Certificate) messageContext.get(CERTIFICATE_PROPERTY);
        return certificate;
    }

    /**
     * Checks whether a WS-Security handler did verify that the element with given Id was signed correctly.
     * 
     * @param id
     * @param context
     */
    @SuppressWarnings("unchecked")
    public static boolean isSignedElement(String id, SOAPMessageContext context) {

        Set<String> signedElements = (Set<String>) context.get(SIGNED_ELEMENTS_CONTEXT_KEY);
        if (null == signedElements)
            return false;
        boolean result = signedElements.contains(id);
        return result;
    }
}
