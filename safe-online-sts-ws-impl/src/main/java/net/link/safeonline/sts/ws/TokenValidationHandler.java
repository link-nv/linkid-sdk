/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sts.ws;

import java.security.PublicKey;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.transform.TransformerException;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.MessageContext.Scope;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import javax.xml.ws.soap.SOAPFaultException;

import net.link.safeonline.util.ee.IdentityServiceClient;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xml.security.exceptions.XMLSecurityException;
import org.apache.xml.security.signature.XMLSignature;
import org.apache.xml.security.utils.Constants;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * SOAP JAX-WS handler to verify the signature on the token to be validated.
 * 
 * @author fcorneli
 * 
 */
public class TokenValidationHandler implements SOAPHandler<SOAPMessageContext> {

	private static final Log LOG = LogFactory
			.getLog(TokenValidationHandler.class);

	public static final String VALIDITY_CONTEXT_VAR = TokenValidationHandler.class
			.getName()
			+ ".Validity";

	private PublicKey publicKey;

	@PostConstruct
	public void postConstructCallback() {
		LOG.debug("post construct");
		System
				.setProperty(
						"com.sun.xml.ws.fault.SOAPFaultBuilder.disableCaptureStackTrace",
						"true");
		IdentityServiceClient identityServiceClient = new IdentityServiceClient();
		this.publicKey = identityServiceClient.getPublicKey();
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
		Boolean outboundProperty = (Boolean) soapContext
				.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
		if (true == outboundProperty) {
			return true;
		}
		SOAPMessage soapMessage = soapContext.getMessage();
		SOAPPart soapPart = soapMessage.getSOAPPart();

		Element tokenSignatureElement = findTokenSignatureElement(soapPart);
		if (null == tokenSignatureElement) {
			/*
			 * Nothing to do here.
			 */
			return true;
		}

		Boolean result;
		try {
			XMLSignature xmlSignature = new XMLSignature(tokenSignatureElement,
					null);
			LOG.debug("checking token signature");
			result = xmlSignature.checkSignatureValue(this.publicKey);
		} catch (XMLSecurityException e) {
			LOG.error("XML signature error: " + e.getMessage(), e);
			throw createSOAPFaultException("XML signature error",
					"InvalidSecurityToken");
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
		nsElement.setAttributeNS(Constants.NamespaceSpecNS, "xmlns:soap",
				"http://schemas.xmlsoap.org/soap/envelope/");
		nsElement.setAttributeNS(Constants.NamespaceSpecNS, "xmlns:ds",
				"http://www.w3.org/2000/09/xmldsig#");
		nsElement.setAttributeNS(Constants.NamespaceSpecNS, "xmlns:samlp",
				"urn:oasis:names:tc:SAML:2.0:protocol");
		nsElement.setAttributeNS(Constants.NamespaceSpecNS, "xmlns:wst",
				"http://docs.oasis-open.org/ws-sx/ws-trust/200512/");
		try {
			Element tokenSignatureElement = (Element) XPathAPI
					.selectSingleNode(
							document,
							"/soap:Envelope/soap:Body/wst:RequestSecurityToken/wst:ValidateTarget/samlp:Response/ds:Signature",
							nsElement);
			return tokenSignatureElement;
		} catch (TransformerException e) {
			throw new RuntimeException("XPath error: " + e.getMessage());
		}
	}

	/**
	 * Gives back the result of the token signature validation.
	 * 
	 * @param context
	 */
	public static boolean getValidity(WebServiceContext context) {
		MessageContext messageContext = context.getMessageContext();
		Boolean validity = (Boolean) messageContext.get(VALIDITY_CONTEXT_VAR);
		if (null == validity) {
			return false;
		}
		return validity;
	}

	private SOAPFaultException createSOAPFaultException(String faultString,
			String wstFaultCode) {
		SOAPFault soapFault;
		try {
			SOAPFactory soapFactory = SOAPFactory.newInstance();
			soapFault = soapFactory.createFault(faultString, new QName(
					"http://docs.oasis-open.org/ws-sx/ws-trust/200512",
					wstFaultCode, "wst"));
		} catch (SOAPException e) {
			throw new RuntimeException("SOAP error");
		}
		return new SOAPFaultException(soapFault);
	}
}
