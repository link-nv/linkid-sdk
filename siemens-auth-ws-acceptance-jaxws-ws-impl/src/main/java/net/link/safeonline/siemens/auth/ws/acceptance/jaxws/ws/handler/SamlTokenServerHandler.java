/*
 * SafeOnline project.
 * 
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.siemens.auth.ws.acceptance.jaxws.ws.handler;

import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import javax.annotation.PostConstruct;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import net.link.safeonline.sdk.ws.ServerCrypto;
import net.link.safeonline.sdk.ws.WSSecurityUtil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.security.WSSecurityEngine;
import org.apache.ws.security.WSSecurityEngineResult;
import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.components.crypto.Crypto;
import org.apache.ws.security.message.token.Timestamp;
import org.joda.time.DateTime;
import org.joda.time.Instant;


/**
 * <h2>{@link SamlTokenServerHandler}<br>
 * <sub>[in short] (TODO).</sub></h2>
 * 
 * <p>
 * [description / usage].
 * </p>
 * 
 * <p>
 * <i>Jan 23, 2009</i>
 * </p>
 * 
 * @author wvdhaute
 */
public class SamlTokenServerHandler implements SOAPHandler<SOAPMessageContext> {

    private static final Log LOG = LogFactory.getLog(SamlTokenServerHandler.class);


    @PostConstruct
    public void postConstructCallback() {

        LOG.debug("ready");
    }

    /**
     * {@inheritDoc}
     */
    public Set<QName> getHeaders() {

        Set<QName> headers = new HashSet<QName>();
        headers.add(new QName("http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd", "Security"));
        return headers;
    }

    /**
     * {@inheritDoc}
     */
    public boolean handleMessage(SOAPMessageContext soapMessageContext) {

        Boolean outboundProperty = (Boolean) soapMessageContext.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);

        SOAPMessage soapMessage = soapMessageContext.getMessage();
        SOAPPart soapPart = soapMessage.getSOAPPart();

        if (true == outboundProperty.booleanValue())
            // dont handle outbound
            return true;

        handleInboundDocument(soapPart, soapMessageContext);

        return true;
    }

    @SuppressWarnings("unchecked")
    private void handleInboundDocument(SOAPPart soapPart, SOAPMessageContext soapMessageContext) {

        LOG.debug("WS-Security header validation");
        WSSecurityEngine securityEngine = WSSecurityEngine.getInstance();
        Crypto crypto = new ServerCrypto();

        Vector<WSSecurityEngineResult> wsSecurityEngineResults;
        try {
            wsSecurityEngineResults = securityEngine.processSecurityHeader(soapPart, null, null, crypto);
        } catch (WSSecurityException e) {
            LOG.debug("WS-Security error: " + e.getMessage(), e);
            throw WSSecurityUtil.createSOAPFaultException("The signature or decryption was invalid", "FailedCheck");
        }
        LOG.debug("results: " + wsSecurityEngineResults);
        if (null == wsSecurityEngineResults)
            throw WSSecurityUtil.createSOAPFaultException("An error was discovered processing the <wsse:Security> header.",
                    "InvalidSecurity");
        Timestamp timestamp = null;
        Set<String> signedElements = null;
        X509Certificate certificate = null;
        for (WSSecurityEngineResult result : wsSecurityEngineResults) {
            Set<String> resultSignedElements = (Set<String>) result.get(WSSecurityEngineResult.TAG_SIGNED_ELEMENT_IDS);
            if (null != resultSignedElements) {
                signedElements = resultSignedElements;
            }
            certificate = (X509Certificate) result.get(WSSecurityEngineResult.TAG_X509_CERTIFICATE);
            if (null != certificate) {
                LOG.debug("retrieved certificate from header");
            }

            Timestamp resultTimestamp = (Timestamp) result.get(WSSecurityEngineResult.TAG_TIMESTAMP);
            if (null != resultTimestamp) {
                timestamp = resultTimestamp;
            }
        }

        if (null == signedElements)
            throw WSSecurityUtil.createSOAPFaultException("The signature or decryption was invalid", "FailedCheck");
        LOG.debug("signed elements: " + signedElements);

        /*
         * Check timestamp.
         */
        if (null == timestamp)
            throw WSSecurityUtil.createSOAPFaultException("missing Timestamp in WS-Security header", "InvalidSecurity");
        String timestampId = timestamp.getID();
        if (false == signedElements.contains(timestampId))
            throw WSSecurityUtil.createSOAPFaultException("Timestamp not signed", "FailedCheck");
        Calendar created = timestamp.getCreated();
        long maxOffset = 1000 * 60 * 5;
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

    /**
     * {@inheritDoc}
     */
    public void close(MessageContext messageContext) {

        // empty

    }

    /**
     * {@inheritDoc}
     */
    public boolean handleFault(SOAPMessageContext soapMessageContext) {

        return true;
    }

}
