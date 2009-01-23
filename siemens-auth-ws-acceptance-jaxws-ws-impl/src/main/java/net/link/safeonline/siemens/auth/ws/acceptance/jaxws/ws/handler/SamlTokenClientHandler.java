/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.siemens.auth.ws.acceptance.jaxws.ws.handler;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.ws.Binding;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import oasis.names.tc.saml._2_0.assertion.AssertionType;
import oasis.names.tc.saml._2_0.assertion.ObjectFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class SamlTokenClientHandler implements SOAPHandler<SOAPMessageContext> {

    private static final Log    LOG                = LogFactory.getLog(SamlTokenClientHandler.class);

    private static final String WS_SECURITY_NS_URI = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd";

    private final AssertionType assertion;


    /**
     * Main constructor.
     */
    public SamlTokenClientHandler(AssertionType assertion) {

        this.assertion = assertion;
    }

    public Set<QName> getHeaders() {

        Set<QName> headers = new HashSet<QName>();
        headers.add(new QName(WS_SECURITY_NS_URI, "Security"));
        return headers;
    }

    public void close(MessageContext messageContext) {

        // empty
    }

    public boolean handleFault(SOAPMessageContext soapMessageContext) {

        return true;
    }

    @SuppressWarnings("unchecked")
    public boolean handleMessage(SOAPMessageContext soapMessageContext) {

        Boolean outboundProperty = (Boolean) soapMessageContext.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
        if (false == outboundProperty.booleanValue())
            /*
             * We only need to add the WS-Security SOAP header to the outbound messages.
             */
            return true;

        SOAPMessage soapMessage = soapMessageContext.getMessage();
        SOAPPart soapPart = soapMessage.getSOAPPart();

        handleDocument(soapPart);

        return true;
    }

    private void handleDocument(SOAPPart soapPart) {

        if (null == assertion) {
            LOG.debug("no assertion specified, will NOT sign message");
            return;
        }

        LOG.debug("adding SAML Token");

        try {
            SOAPEnvelope soapEnvelope = soapPart.getEnvelope();
            Name wsseHeaderName = soapEnvelope.createName("Security", "wsse", WS_SECURITY_NS_URI);
            if (soapEnvelope.getHeader() == null) {
                soapEnvelope.addHeader();
            }
            SOAPHeaderElement securityElement = soapEnvelope.getHeader().addHeaderElement(wsseHeaderName);

            // marshall Assertion Java class into XML
            JAXBContext context = JAXBContext.newInstance(net.lin_k.safe_online.auth.ObjectFactory.class);
            Marshaller marshaller = context.createMarshaller();
            ObjectFactory objectFactory = new ObjectFactory();

            org.w3c.dom.Document assertionElement = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            marshaller.marshal(objectFactory.createAssertion(assertion), assertionElement);
            securityElement.appendChild(soapPart.importNode(assertionElement, true));

        } catch (Exception e) {
            LOG.debug("Exception caught: " + e.getMessage());
        }

    }

    public static void addNewHandler(Object port, AssertionType assertion) {

        BindingProvider bindingProvider = (BindingProvider) port;
        Binding binding = bindingProvider.getBinding();
        @SuppressWarnings("unchecked")
        List<Handler> handlerChain = binding.getHandlerChain();
        Handler<SOAPMessageContext> wsSecurityHandler = new SamlTokenClientHandler(assertion);
        handlerChain.add(wsSecurityHandler);
        binding.setHandlerChain(handlerChain);
    }
}
