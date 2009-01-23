/*
 * SafeOnline project.
 * 
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.siemens.auth.ws.acceptance.jaxws.ws.handler;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


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

    /**
     * TODO
     */
    private void handleInboundDocument(SOAPPart soapPart, SOAPMessageContext soapMessageContext) {

        LOG.debug("WS-Security header validation");

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
