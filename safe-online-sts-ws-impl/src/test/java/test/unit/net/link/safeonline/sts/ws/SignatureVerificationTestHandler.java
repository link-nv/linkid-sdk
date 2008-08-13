/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.sts.ws;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.transform.TransformerException;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xml.security.exceptions.XMLSecurityException;
import org.apache.xml.security.signature.XMLSignature;
import org.apache.xml.security.signature.XMLSignatureException;
import org.apache.xml.security.utils.Constants;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * Test SOAP JAX-WS Handler to verify the different XML Signature of the STS request.
 * 
 * @author fcorneli
 * 
 */
public class SignatureVerificationTestHandler implements SOAPHandler<SOAPMessageContext> {

    private static final Log LOG = LogFactory.getLog(SignatureVerificationTestHandler.class);


    public Set<QName> getHeaders() {

        return null;
    }

    public void close(MessageContext context) {

    }

    public boolean handleFault(SOAPMessageContext soapContext) {

        return true;
    }

    public boolean handleMessage(SOAPMessageContext soapContext) {

        LOG.debug("handle message");
        Boolean outboundProperty = (Boolean) soapContext.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
        if (false == outboundProperty) {
            return true;
        }
        SOAPMessage soapMessage = soapContext.getMessage();
        SOAPPart soapPart = soapMessage.getSOAPPart();

        X509Certificate certificate;
        try {
            certificate = extractWSSecurityCertificate(soapPart);
        } catch (Exception e) {
            throw new RuntimeException("error: " + e.getMessage(), e);
        }

        try {
            verifyWSSecuritySignature(soapPart, certificate);
        } catch (Exception e) {
            throw new RuntimeException("error: " + e.getMessage(), e);
        }

        try {
            verifySamlpSignature(soapPart, certificate);
        } catch (Exception e) {
            throw new RuntimeException("error: " + e.getMessage(), e);
        }

        // Element signatureElement = (Element)
        // XPathAPI.selectSingleNode(soapPart, "//", nsElement);

        return true;
    }

    private void verifySamlpSignature(Document document, X509Certificate certificate) throws TransformerException,
            XMLSignatureException, XMLSecurityException {

        Element nsElement = createNsElement(document);
        Element samlpSignature = (Element) XPathAPI.selectSingleNode(document, "//samlp:Response/ds:Signature",
                nsElement);
        assertNotNull("SAMLp signature not found", samlpSignature);
        XMLSignature xmlSignature = new XMLSignature(samlpSignature, null);
        boolean signatureResult = xmlSignature.checkSignatureValue(certificate.getPublicKey());
        assertTrue("SAMLp signature invalid", signatureResult);
        LOG.debug("SAMLp signature valid");
    }

    private void verifyWSSecuritySignature(Document document, X509Certificate certificate) throws TransformerException,
            XMLSignatureException, XMLSecurityException {

        Element nsElement = createNsElement(document);
        Element wsSecuritySignatureElement = (Element) XPathAPI.selectSingleNode(document,
                "//wsse:Security/ds:Signature", nsElement);
        assertNotNull(wsSecuritySignatureElement);
        XMLSignature xmlSignature = new XMLSignature(wsSecuritySignatureElement, null);
        boolean signatureResult = xmlSignature.checkSignatureValue(certificate);
        assertTrue(signatureResult);
        LOG.debug("WS-Security signature valid");
    }

    private Element createNsElement(Document document) {

        Element nsElement = document.createElement("nsElement");
        nsElement.setAttributeNS(Constants.NamespaceSpecNS, "xmlns:ds", "http://www.w3.org/2000/09/xmldsig#");
        nsElement.setAttributeNS(Constants.NamespaceSpecNS, "xmlns:samlp", "urn:oasis:names:tc:SAML:2.0:protocol");
        nsElement.setAttributeNS(Constants.NamespaceSpecNS, "xmlns:wsse",
                "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd");
        return nsElement;
    }

    private X509Certificate extractWSSecurityCertificate(Document document) throws TransformerException,
            CertificateException {

        Element nsElement = createNsElement(document);
        Element binarySecurityTokenElement = (Element) XPathAPI.selectSingleNode(document,
                "//wsse:BinarySecurityToken", nsElement);
        String encodedBinarySecurityToken = binarySecurityTokenElement.getTextContent();
        byte[] binarySecurityToken = Base64.decodeBase64(encodedBinarySecurityToken.getBytes());
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        X509Certificate cert = (X509Certificate) certificateFactory.generateCertificate(new ByteArrayInputStream(
                binarySecurityToken));
        LOG.debug("certificate: " + cert);
        return cert;
    }
}
