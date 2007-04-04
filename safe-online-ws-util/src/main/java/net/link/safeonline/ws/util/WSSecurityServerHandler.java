/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.ws.util;

import java.security.cert.X509Certificate;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.security.WSSecurityEngine;
import org.apache.ws.security.WSSecurityEngineResult;
import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.components.crypto.Crypto;
import org.w3c.dom.Document;

/**
 * JAX-WS SOAP Handler that provider WS-Security server-side verification.
 * 
 * @author fcorneli
 * 
 */
public class WSSecurityServerHandler implements SOAPHandler<SOAPMessageContext> {

	private static final Log LOG = LogFactory
			.getLog(WSSecurityServerHandler.class);

	public Set<QName> getHeaders() {
		Set<QName> headers = new HashSet<QName>();
		headers
				.add(new QName(
						"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd",
						"Security"));
		return headers;

	}

	public void close(MessageContext messageContext) {
	}

	public boolean handleFault(SOAPMessageContext soapMessageContext) {
		return true;
	}

	public boolean handleMessage(SOAPMessageContext soapMessageContext) {
		Boolean outboundProperty = (Boolean) soapMessageContext
				.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
		if (true == outboundProperty.booleanValue()) {
			/*
			 * We only need to verify the WS-Security SOAP header on inbound
			 * messages.
			 */
			return true;
		}

		SOAPMessage soapMessage = soapMessageContext.getMessage();
		SOAPPart soapPart = soapMessage.getSOAPPart();

		handleDocument(soapPart, soapMessageContext);

		return true;
	}

	@SuppressWarnings("unchecked")
	private void handleDocument(Document document,
			SOAPMessageContext soapMessageContext) {
		LOG.debug("WS-Security header validation");
		WSSecurityEngine securityEngine = WSSecurityEngine.getInstance();
		Crypto crypto = new ServerCrypto();

		Vector<WSSecurityEngineResult> wsSecurityEngineResults;
		try {
			wsSecurityEngineResults = securityEngine.processSecurityHeader(
					document, null, null, crypto);
		} catch (WSSecurityException e) {
			throw new RuntimeException("WS-Security error");
		}
		LOG.debug("results: " + wsSecurityEngineResults);
		if (null == wsSecurityEngineResults) {
			throw new RuntimeException("missing WS-Security header");
		}
		for (WSSecurityEngineResult result : wsSecurityEngineResults) {
			X509Certificate certificate = result.getCertificate();
			if (null == certificate) {
				continue;
			}
			soapMessageContext
					.put(
							ApplicationCertificateValidatorHandler.CERTIFICATE_PROPERTY,
							certificate);
			break;
		}
	}
}
