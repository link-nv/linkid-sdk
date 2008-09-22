/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sts.ws;

import java.io.StringWriter;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPFault;
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
import javax.xml.ws.soap.SOAPFaultException;

import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.exception.DeviceNotFoundException;
import net.link.safeonline.authentication.exception.NodeNotFoundException;
import net.link.safeonline.authentication.service.ApplicationAuthenticationService;
import net.link.safeonline.authentication.service.DeviceAuthenticationService;
import net.link.safeonline.authentication.service.NodeAuthenticationService;
import net.link.safeonline.sdk.ws.sts.TrustDomainType;
import net.link.safeonline.util.ee.EjbUtils;
import net.link.safeonline.ws.common.WebServiceConstants;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xml.security.exceptions.XMLSecurityException;
import org.apache.xml.security.signature.XMLSignature;
import org.apache.xml.security.utils.Constants;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;


/**
 * SOAP JAX-WS handler to verify the signature on the token to be validated.
 * 
 * @author fcorneli
 * 
 */
public class TokenValidationHandler implements SOAPHandler<SOAPMessageContext> {

    private static final Log   LOG                  = LogFactory.getLog(TokenValidationHandler.class);

    public static final String VALIDITY_CONTEXT_VAR = TokenValidationHandler.class.getName() + ".Validity";


    @PostConstruct
    public void postConstructCallback() {

        LOG.debug("post construct");
        System.setProperty("com.sun.xml.ws.fault.SOAPFaultBuilder.disableCaptureStackTrace", "true");
    }

    public Set<QName> getHeaders() {

        return null;
    }

    public void close(MessageContext context) {

    }

    public boolean handleFault(SOAPMessageContext soapContext) {

        return true;
    }

    public boolean handleMessage(SOAPMessageContext soapContext) {

        Boolean outboundProperty = (Boolean) soapContext.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
        if (true == outboundProperty)
            return true;
        SOAPMessage soapMessage = soapContext.getMessage();
        SOAPPart soapPart = soapMessage.getSOAPPart();

        Element tokenSignatureElement = findTokenSignatureElement(soapPart);
        if (null == tokenSignatureElement)
            /*
             * Nothing to do here.
             */
            return true;

        Element issuerElement = findIssuerElement(soapPart);
        if (null == issuerElement) {
            LOG.debug("No issuer specified in token ...");
            throw createSOAPFaultException("No issuer specified in token", "InvalidSecurityToken");
        }
        String issuerName = issuerElement.getTextContent();
        LOG.debug("issuer name: " + issuerName);

        Element requestTypeElement = findRequestTypeElement(soapPart);
        if (null == requestTypeElement) {
            LOG.debug("No request type specified in token ...");
            throw createSOAPFaultException("No request type specified in token", "InvalidSecurityToken");
        }
        String requestType = requestTypeElement.getTextContent();
        LOG.debug("request type: " + requestType);
        if (false == requestType.startsWith(WebServiceConstants.WS_TRUST_REQUEST_TYPE + "Validate")) {
            LOG.debug("Only supporting the validation binding");
            throw createSOAPFaultException("Only supporting the validation binding", "InvalidSecurityToken");
        }

        String[] requestTypeParts = requestType.split("#");
        TrustDomainType trustDomain;
        if (requestTypeParts.length == 1) {
            trustDomain = TrustDomainType.APPLICATION;
        } else {
            trustDomain = TrustDomainType.valueOf(requestTypeParts[1]);
        }

        Boolean result;
        try {
            XMLSignature xmlSignature = new XMLSignature(tokenSignatureElement, null);
            LOG.debug("checking token signature");
            if (trustDomain == TrustDomainType.NODE) {
                NodeAuthenticationService nodeAuthenticationService = EjbUtils.getEJB(
                        "SafeOnline/NodeAuthenticationServiceBean/local", NodeAuthenticationService.class);
                try {
                    List<X509Certificate> nodeSigningCertificates = nodeAuthenticationService
                            .getSigningCertificates(issuerName);
                    result = false;
                    for (X509Certificate nodeSigningCertificate : nodeSigningCertificates) {
                        result = xmlSignature.checkSignatureValue(nodeSigningCertificate);
                        if (result == true) {
                            break;
                        }
                    }
                } catch (NodeNotFoundException e) {
                    LOG.debug("unknown token issuer: " + issuerName);
                    throw createSOAPFaultException("unknown token issuer: " + issuerName, "InvalidSecurityToken");
                }
            } else if (trustDomain == TrustDomainType.DEVICE) {
                DeviceAuthenticationService deviceAuthenticationService = EjbUtils.getEJB(
                        "SafeOnline/DeviceAuthenticationServiceBean/local", DeviceAuthenticationService.class);
                try {
                    List<X509Certificate> deviceCertificates = deviceAuthenticationService.getCertificates(issuerName);
                    result = false;
                    for (X509Certificate deviceCertificate : deviceCertificates) {
                        result = xmlSignature.checkSignatureValue(deviceCertificate);
                        if (result == true) {
                            break;
                        }
                    }
                } catch (DeviceNotFoundException e) {
                    LOG.debug("unknown token issuer: " + issuerName);
                    throw createSOAPFaultException("unknown token issuer: " + issuerName, "InvalidSecurityToken");
                }
            } else if (trustDomain == TrustDomainType.APPLICATION) {
                ApplicationAuthenticationService applicationAuthenticationService = EjbUtils
                        .getEJB("SafeOnline/ApplicationAuthenticationServiceBean/local",
                                ApplicationAuthenticationService.class);
                try {
                    List<X509Certificate> applicationCertificates = applicationAuthenticationService
                            .getCertificates(issuerName);
                    result = false;
                    for (X509Certificate applicationCertificate : applicationCertificates) {
                        result = xmlSignature.checkSignatureValue(applicationCertificate);
                        if (result == true) {
                            break;
                        }
                    }
                } catch (ApplicationNotFoundException e) {
                    LOG.debug("unknown token issuer: " + issuerName);
                    throw createSOAPFaultException("unknown token issuer: " + issuerName, "InvalidSecurityToken");
                }
            } else {
                LOG.debug("unknown token issuer: " + issuerName);
                throw createSOAPFaultException("unknown token issuer: " + issuerName, "InvalidSecurityToken");
            }
        } catch (XMLSecurityException e) {
            LOG.error("XML signature error: " + e.getMessage(), e);
            throw createSOAPFaultException("XML signature error", "InvalidSecurityToken");
        }

        setValidity(result, soapContext);

        return true;
    }

    private void setValidity(Boolean validity, SOAPMessageContext soapContext) {

        LOG.debug("validity: " + validity);
        soapContext.put(VALIDITY_CONTEXT_VAR, validity);
        soapContext.setScope(VALIDITY_CONTEXT_VAR, Scope.APPLICATION);
    }

    private Element findTokenSignatureElement(Document document) {

        Element nsElement = document.createElement("nsElement");
        nsElement.setAttributeNS(Constants.NamespaceSpecNS, "xmlns:soap", "http://schemas.xmlsoap.org/soap/envelope/");
        nsElement.setAttributeNS(Constants.NamespaceSpecNS, "xmlns:ds", "http://www.w3.org/2000/09/xmldsig#");
        nsElement.setAttributeNS(Constants.NamespaceSpecNS, "xmlns:samlp", "urn:oasis:names:tc:SAML:2.0:protocol");
        nsElement.setAttributeNS(Constants.NamespaceSpecNS, "xmlns:wst",
                "http://docs.oasis-open.org/ws-sx/ws-trust/200512/");
        LOG.debug("document: " + document.toString());
        try {
            Element tokenSignatureElement = (Element) XPathAPI.selectSingleNode(document,
                    "/soap:Envelope/soap:Body/wst:RequestSecurityToken/wst:ValidateTarget/samlp:Response/ds:Signature",
                    nsElement);
            if (null == tokenSignatureElement) {
                tokenSignatureElement = (Element) XPathAPI
                        .selectSingleNode(
                                document,
                                "/soap:Envelope/soap:Body/wst:RequestSecurityToken/wst:ValidateTarget/samlp:AuthnRequest/ds:Signature",
                                nsElement);
                if (null == tokenSignatureElement) {
                    tokenSignatureElement = (Element) XPathAPI
                            .selectSingleNode(
                                    document,
                                    "/soap:Envelope/soap:Body/wst:RequestSecurityToken/wst:ValidateTarget/samlp:LogoutResponse/ds:Signature",
                                    nsElement);
                    if (null == tokenSignatureElement) {
                        tokenSignatureElement = (Element) XPathAPI
                                .selectSingleNode(
                                        document,
                                        "/soap:Envelope/soap:Body/wst:RequestSecurityToken/wst:ValidateTarget/samlp:LogoutRequest/ds:Signature",
                                        nsElement);
                    }
                }
            }
            return tokenSignatureElement;
        } catch (TransformerException e) {
            throw new RuntimeException("XPath error: " + e.getMessage());
        }
    }

    private Element findIssuerElement(Document document) {

        Element nsElement = document.createElement("nsElement");
        nsElement.setAttributeNS(Constants.NamespaceSpecNS, "xmlns:soap", "http://schemas.xmlsoap.org/soap/envelope/");
        nsElement.setAttributeNS(Constants.NamespaceSpecNS, "xmlns:ds", "http://www.w3.org/2000/09/xmldsig#");
        nsElement.setAttributeNS(Constants.NamespaceSpecNS, "xmlns:samlp", "urn:oasis:names:tc:SAML:2.0:protocol");
        nsElement.setAttributeNS(Constants.NamespaceSpecNS, "xmlns:saml", "urn:oasis:names:tc:SAML:2.0:assertion");
        nsElement.setAttributeNS(Constants.NamespaceSpecNS, "xmlns:wst",
                "http://docs.oasis-open.org/ws-sx/ws-trust/200512/");
        try {
            LOG.debug("document: " + domToString(document));
        } catch (TransformerException e1) {
            LOG.debug("transformer exception");
        }
        try {
            Element issuerElement = (Element) XPathAPI.selectSingleNode(document,
                    "/soap:Envelope/soap:Body/wst:RequestSecurityToken/wst:ValidateTarget/samlp:Response/saml:Issuer",
                    nsElement);
            if (null == issuerElement) {
                issuerElement = (Element) XPathAPI
                        .selectSingleNode(
                                document,
                                "/soap:Envelope/soap:Body/wst:RequestSecurityToken/wst:ValidateTarget/samlp:AuthnRequest/saml:Issuer",
                                nsElement);
                if (null == issuerElement) {
                    issuerElement = (Element) XPathAPI
                            .selectSingleNode(
                                    document,
                                    "/soap:Envelope/soap:Body/wst:RequestSecurityToken/wst:ValidateTarget/samlp:LogoutResponse/saml:Issuer",
                                    nsElement);
                    if (null == issuerElement) {
                        issuerElement = (Element) XPathAPI
                                .selectSingleNode(
                                        document,
                                        "/soap:Envelope/soap:Body/wst:RequestSecurityToken/wst:ValidateTarget/samlp:LogoutRequest/saml:Issuer",
                                        nsElement);
                    }
                }
            }
            return issuerElement;
        } catch (TransformerException e) {
            throw new RuntimeException("XPath error: " + e.getMessage());
        }
    }

    private Element findRequestTypeElement(Document document) {

        Element nsElement = document.createElement("nsElement");
        nsElement.setAttributeNS(Constants.NamespaceSpecNS, "xmlns:soap", "http://schemas.xmlsoap.org/soap/envelope/");
        nsElement.setAttributeNS(Constants.NamespaceSpecNS, "xmlns:ds", "http://www.w3.org/2000/09/xmldsig#");
        nsElement.setAttributeNS(Constants.NamespaceSpecNS, "xmlns:samlp", "urn:oasis:names:tc:SAML:2.0:protocol");
        nsElement.setAttributeNS(Constants.NamespaceSpecNS, "xmlns:saml", "urn:oasis:names:tc:SAML:2.0:assertion");
        nsElement.setAttributeNS(Constants.NamespaceSpecNS, "xmlns:wst",
                "http://docs.oasis-open.org/ws-sx/ws-trust/200512/");
        try {
            LOG.debug("document: " + domToString(document));
        } catch (TransformerException e1) {
            LOG.debug("transformer exception");
        }
        try {
            Element issuerElement = (Element) XPathAPI.selectSingleNode(document,
                    "/soap:Envelope/soap:Body/wst:RequestSecurityToken/wst:RequestType", nsElement);
            return issuerElement;
        } catch (TransformerException e) {
            throw new RuntimeException("XPath error: " + e.getMessage());
        }
    }

    private String domToString(Node domNode) throws TransformerException {

        Source source = new DOMSource(domNode);
        StringWriter stringWriter = new StringWriter();
        Result result = new StreamResult(stringWriter);
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.transform(source, result);
        return stringWriter.toString();
    }

    /**
     * Gives back the result of the token signature validation.
     * 
     * @param context
     */
    public static boolean getValidity(WebServiceContext context) {

        MessageContext messageContext = context.getMessageContext();
        Boolean validity = (Boolean) messageContext.get(VALIDITY_CONTEXT_VAR);
        if (null == validity)
            return false;
        return validity;
    }

    private SOAPFaultException createSOAPFaultException(String faultString, String wstFaultCode) {

        SOAPFault soapFault;
        try {
            SOAPFactory soapFactory = SOAPFactory.newInstance();
            soapFault = soapFactory.createFault(faultString, new QName(
                    "http://docs.oasis-open.org/ws-sx/ws-trust/200512", wstFaultCode, "wst"));
        } catch (SOAPException e) {
            throw new RuntimeException("SOAP error");
        }
        return new SOAPFaultException(soapFault);
    }
}
