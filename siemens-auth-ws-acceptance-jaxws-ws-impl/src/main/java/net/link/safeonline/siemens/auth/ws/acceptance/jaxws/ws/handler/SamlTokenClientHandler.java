/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.siemens.auth.ws.acceptance.jaxws.ws.handler;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.ws.Binding;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import net.link.safeonline.sdk.ws.ClientCrypto;
import oasis.names.tc.saml._2_0.assertion.AssertionType;
import oasis.names.tc.saml._2_0.assertion.ObjectFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.WSEncryptionPart;
import org.apache.ws.security.components.crypto.Crypto;
import org.apache.ws.security.message.WSSecHeader;
import org.apache.ws.security.message.WSSecSignature;
import org.apache.ws.security.message.WSSecTimestamp;
import org.w3c.dom.Element;


public class SamlTokenClientHandler implements SOAPHandler<SOAPMessageContext> {

    private static final Log      LOG                = LogFactory.getLog(SamlTokenClientHandler.class);

    private static final String   WS_SECURITY_NS_URI = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd";

    private final AssertionType   assertion;

    private final X509Certificate certificate;

    private final PrivateKey      privateKey;


    /**
     * Main constructor.
     */
    public SamlTokenClientHandler(AssertionType assertion, X509Certificate certificate, PrivateKey privateKey) {

        this.assertion = assertion;
        this.certificate = certificate;
        this.privateKey = privateKey;
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

        try {
            LOG.debug("adding SAML Token");

            WSSecHeader wsSecHeader = new WSSecHeader();
            wsSecHeader.insertSecurityHeader(soapPart);

            WSSecTimestamp wsSecTimeStamp = new WSSecTimestamp();
            wsSecTimeStamp.setTimeToLive(0);
            /*
             * If ttl is zero then there will be no Expires element within the Timestamp. Eventually we want to let the service itself
             * decide how long the message validity period is.
             */
            wsSecTimeStamp.prepare(soapPart);
            wsSecTimeStamp.prependToHeader(wsSecHeader);

            Element securityHeader = wsSecHeader.getSecurityHeader();

            // marshall SAML Assertion into XML
            JAXBContext context = JAXBContext.newInstance(net.lin_k.safe_online.auth.ObjectFactory.class);
            Marshaller marshaller = context.createMarshaller();
            ObjectFactory objectFactory = new ObjectFactory();

            org.w3c.dom.Document assertionElement = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            marshaller.marshal(objectFactory.createAssertion(assertion), assertionElement);

            LOG.debug("assertion: " + assertionElement.getDocumentElement().toString());

            securityHeader.appendChild(soapPart.importNode(assertionElement.getDocumentElement(), true));

            LOG.debug("adding signature");

            WSSecSignature wsSecSignature = new WSSecSignature();
            wsSecSignature.setKeyIdentifierType(WSConstants.BST_DIRECT_REFERENCE);
            Crypto crypto = new ClientCrypto(certificate, privateKey);
            wsSecSignature.prepare(soapPart, crypto, wsSecHeader);

            Vector<WSEncryptionPart> wsEncryptionParts = new Vector<WSEncryptionPart>();
            wsEncryptionParts.add(new WSEncryptionPart(wsSecTimeStamp.getId()));

            wsSecSignature.addReferencesToSign(wsEncryptionParts, wsSecHeader);

            wsSecSignature.prependToHeader(wsSecHeader);

            wsSecSignature.prependBSTElementToHeader(wsSecHeader);

            wsSecSignature.computeSignature();

        } catch (Exception e) {
            LOG.error("Exception caught: " + e.getMessage(), e);
        }

    }

    public static void addNewHandler(Object port, AssertionType assertion, X509Certificate certificate, PrivateKey privateKey) {

        BindingProvider bindingProvider = (BindingProvider) port;
        Binding binding = bindingProvider.getBinding();
        @SuppressWarnings("unchecked")
        List<Handler> handlerChain = binding.getHandlerChain();
        Handler<SOAPMessageContext> wsSecurityHandler = new SamlTokenClientHandler(assertion, certificate, privateKey);
        handlerChain.add(wsSecurityHandler);
        binding.setHandlerChain(handlerChain);
    }
}
