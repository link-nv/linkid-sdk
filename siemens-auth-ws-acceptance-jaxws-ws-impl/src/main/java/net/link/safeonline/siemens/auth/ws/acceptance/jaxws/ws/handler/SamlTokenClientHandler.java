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
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.ws.Binding;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import net.link.safeonline.saml.common.Saml2SubjectConfirmationMethod;
import net.link.safeonline.sdk.ws.ClientCrypto;
import oasis.names.tc.saml._2_0.assertion.AssertionType;
import oasis.names.tc.saml._2_0.assertion.ObjectFactory;
import oasis.names.tc.saml._2_0.assertion.SubjectConfirmationType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.WSEncryptionPart;
import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.components.crypto.Crypto;
import org.apache.ws.security.message.WSSecHeader;
import org.apache.ws.security.message.WSSecSignature;
import org.apache.ws.security.message.WSSecTimestamp;
import org.apache.ws.security.saml2.WSSecSignatureSAML2;
import org.w3c.dom.Element;


public class SamlTokenClientHandler implements SOAPHandler<SOAPMessageContext> {

    private static final Log      LOG                = LogFactory.getLog(SamlTokenClientHandler.class);

    private static final String   WS_SECURITY_NS_URI = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd";

    public static final String    WSU_NS             = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd";

    public static final String    WSU_PREFIX         = "wsu";

    public static final String    XMLNS_NS           = "http://www.w3.org/2000/xmlns/";

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

            Vector<WSEncryptionPart> wsEncryptionParts = new Vector<WSEncryptionPart>();
            wsEncryptionParts.add(new WSEncryptionPart(wsSecTimeStamp.getId()));

            Crypto crypto = new ClientCrypto(certificate, privateKey);

            for (JAXBElement<?> element : assertion.getSubject().getContent()) {
                if (element.getValue() instanceof SubjectConfirmationType) {
                    SubjectConfirmationType subjectConfirmation = (SubjectConfirmationType) element.getValue();
                    if (subjectConfirmation.getMethod().equals(Saml2SubjectConfirmationMethod.HOLDER_OF_KEY.getMethodURI())) {

                        LOG.debug("adding saml2 signature");
                        WSSecSignatureSAML2 wsSecSignatureSAML2 = new WSSecSignatureSAML2();
                        wsSecSignatureSAML2.setKeyIdentifierType(WSConstants.BST_DIRECT_REFERENCE);
                        wsSecSignatureSAML2.setParts(wsEncryptionParts);
                        wsSecSignatureSAML2.build(soapPart, crypto, assertion, null, null, null, wsSecHeader);

                    } else {

                        LOG.debug("adding assertion");
                        addAssertion(soapPart, wsSecHeader);
                        wsEncryptionParts.add(new WSEncryptionPart(assertion.getID()));

                        LOG.debug("adding signature");
                        WSSecSignature wsSecSignature = new WSSecSignature();
                        wsSecSignature.setKeyIdentifierType(WSConstants.BST_DIRECT_REFERENCE);
                        wsSecSignature.prepare(soapPart, crypto, wsSecHeader);
                        wsSecSignature.addReferencesToSign(wsEncryptionParts, wsSecHeader);
                        wsSecSignature.prependToHeader(wsSecHeader);
                        wsSecSignature.prependBSTElementToHeader(wsSecHeader);
                        wsSecSignature.computeSignature();

                    }
                }
            }

        } catch (Exception e) {
            LOG.error("Exception caught: " + e.getMessage(), e);
        }
    }

    /**
     * Marshall SAML 2 assertion, wsu:Id with value the assertion's ID is added to included the assertion in the signature.
     */
    private void addAssertion(SOAPPart soapPart, WSSecHeader wsSecHeader)
            throws WSSecurityException {

        // marshall SAML Assertion into XML
        try {
            JAXBContext context = JAXBContext.newInstance(net.lin_k.safe_online.auth.ObjectFactory.class);
            Marshaller marshaller = context.createMarshaller();
            ObjectFactory objectFactory = new ObjectFactory();

            org.w3c.dom.Document assertionElement = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            marshaller.marshal(objectFactory.createAssertion(assertion), assertionElement);
            Element samlToken = assertionElement.getDocumentElement();

            samlToken.setAttributeNS(XMLNS_NS, "xmlns:" + WSU_PREFIX, WSU_NS);
            samlToken.setAttributeNS(WSU_NS, WSU_PREFIX + ":Id", assertion.getID());

            wsSecHeader.getSecurityHeader().appendChild(soapPart.importNode(samlToken, true));

        } catch (ParserConfigurationException e) {
            throw new WSSecurityException(WSSecurityException.FAILED_SIGNATURE, "noSAMLdoc", null, e);
        } catch (JAXBException e) {
            throw new WSSecurityException(WSSecurityException.FAILED_SIGNATURE, "noSAMLdoc", null, e);
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
