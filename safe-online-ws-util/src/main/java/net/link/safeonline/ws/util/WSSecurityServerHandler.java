/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.ws.util;

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

import net.link.safeonline.config.model.ConfigurationManager;
import net.link.safeonline.util.ee.EjbUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.security.WSSecurityEngine;
import org.apache.ws.security.WSSecurityEngineResult;
import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.components.crypto.Crypto;
import org.apache.ws.security.message.WSSecHeader;
import org.apache.ws.security.message.WSSecTimestamp;
import org.apache.ws.security.message.token.Timestamp;
import org.joda.time.DateTime;
import org.joda.time.Instant;

/**
 * JAX-WS SOAP Handler that provider WS-Security server-side verification.
 * 
 * @author fcorneli
 * 
 */
public class WSSecurityServerHandler implements SOAPHandler<SOAPMessageContext> {

	private static final Log LOG = LogFactory
			.getLog(WSSecurityServerHandler.class);

	public static final String CERTIFICATE_PROPERTY = WSSecurityServerHandler.class
			+ ".x509";

	public static final String SIGNED_ELEMENTS_CONTEXT_KEY = WSSecurityServerHandler.class
			+ ".signed.elements";

	private ConfigurationManager configurationManager;

	@PostConstruct
	public void postConstructCallback() {
		System
				.setProperty(
						"com.sun.xml.ws.fault.SOAPFaultBuilder.disableCaptureStackTrace",
						"true");
		this.configurationManager = EjbUtils.getEJB(
				"SafeOnline/ConfigurationManagerBean/local",
				ConfigurationManager.class);
	}

	public Set<QName> getHeaders() {
		Set<QName> headers = new HashSet<QName>();
		headers
				.add(new QName(
						"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd",
						"Security"));
		return headers;
	}

	public void close(@SuppressWarnings("unused")
	MessageContext messageContext) {
		// empty
	}

	public boolean handleFault(@SuppressWarnings("unused")
	SOAPMessageContext soapMessageContext) {
		return true;
	}

	public boolean handleMessage(SOAPMessageContext soapMessageContext) {
		Boolean outboundProperty = (Boolean) soapMessageContext
				.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);

		SOAPMessage soapMessage = soapMessageContext.getMessage();
		SOAPPart soapPart = soapMessage.getSOAPPart();

		if (true == outboundProperty.booleanValue()) {
			handleOutboundDocument(soapPart);
			return true;
		}

		handleInboundDocument(soapPart, soapMessageContext);

		return true;
	}

	/**
	 * Handles the outbound SOAP message. This method will simply add an
	 * unsigned WS-Security Timestamp in the SOAP header. This is required for
	 * .NET 2/3 clients.
	 * 
	 * @param document
	 */
	private void handleOutboundDocument(SOAPPart document) {
		LOG.debug("handle outbound document");
		WSSecHeader wsSecHeader = new WSSecHeader();
		wsSecHeader.insertSecurityHeader(document);
		WSSecTimestamp wsSecTimeStamp = new WSSecTimestamp();
		wsSecTimeStamp.setTimeToLive(0);
		wsSecTimeStamp.prepare(document);
		wsSecTimeStamp.prependToHeader(wsSecHeader);
	}

	@SuppressWarnings("unchecked")
	private void handleInboundDocument(SOAPPart document,
			SOAPMessageContext soapMessageContext) {
		LOG.debug("WS-Security header validation");
		WSSecurityEngine securityEngine = WSSecurityEngine.getInstance();
		Crypto crypto = new ServerCrypto();

		Vector<WSSecurityEngineResult> wsSecurityEngineResults;
		try {
			wsSecurityEngineResults = securityEngine.processSecurityHeader(
					document, null, null, crypto);
		} catch (WSSecurityException e) {
			LOG.debug("WS-Security error: " + e.getMessage(), e);
			throw WSSecurityUtil.createSOAPFaultException(
					"The signature or decryption was invalid", "FailedCheck");
		}
		LOG.debug("results: " + wsSecurityEngineResults);
		if (null == wsSecurityEngineResults) {
			throw WSSecurityUtil
					.createSOAPFaultException(
							"An error was discovered processing the <wsse:Security> header.",
							"InvalidSecurity");
		}
		Timestamp timestamp = null;
		Set<String> signedElements = null;
		for (WSSecurityEngineResult result : wsSecurityEngineResults) {
			Set<String> resultSignedElements = (Set<String>) result
					.get(WSSecurityEngineResult.TAG_SIGNED_ELEMENT_IDS);
			if (null != resultSignedElements) {
				signedElements = resultSignedElements;
			}
			X509Certificate certificate = (X509Certificate) result
					.get(WSSecurityEngineResult.TAG_X509_CERTIFICATE);
			if (null != certificate) {
				soapMessageContext.put(CERTIFICATE_PROPERTY, certificate);
			}

			Timestamp resultTimestamp = (Timestamp) result
					.get(WSSecurityEngineResult.TAG_TIMESTAMP);
			if (null != resultTimestamp) {
				timestamp = resultTimestamp;
			}
		}

		if (null == signedElements) {
			throw WSSecurityUtil.createSOAPFaultException(
					"The signature or decryption was invalid", "FailedCheck");
		}
		LOG.debug("signed elements: " + signedElements);
		soapMessageContext.put(SIGNED_ELEMENTS_CONTEXT_KEY, signedElements);

		/*
		 * Check timestamp.
		 */
		if (null == timestamp) {
			throw WSSecurityUtil.createSOAPFaultException(
					"missing Timestamp in WS-Security header",
					"InvalidSecurity");
		}
		String timestampId = timestamp.getID();
		if (false == signedElements.contains(timestampId)) {
			throw WSSecurityUtil.createSOAPFaultException(
					"Timestamp not signed", "FailedCheck");
		}
		Calendar created = timestamp.getCreated();
		long maxOffset = this.configurationManager
				.getMaximumWsSecurityTimestampOffset();
		DateTime createdDateTime = new DateTime(created);
		Instant createdInstant = createdDateTime.toInstant();
		Instant nowInstant = new DateTime().toInstant();
		long offset = Math.abs(createdInstant.getMillis()
				- nowInstant.getMillis());
		if (offset > maxOffset) {
			LOG.debug("timestamp offset: " + offset);
			LOG.debug("maximum allowed offset: " + maxOffset);
			throw WSSecurityUtil.createSOAPFaultException(
					"WS-Security Created Timestamp offset exceeded",
					"FailedCheck");
		}
	}

	/**
	 * Gives back the X509 certificate that was set previously by a WS-Security
	 * handler.
	 * 
	 * @param context
	 */
	public static X509Certificate getCertificate(SOAPMessageContext context) {
		X509Certificate certificate = (X509Certificate) context
				.get(CERTIFICATE_PROPERTY);
		return certificate;
	}

	/**
	 * Checks whether a WS-Security handler did verify that the element with
	 * given Id was signed correctly.
	 * 
	 * @param id
	 * @param context
	 */
	@SuppressWarnings("unchecked")
	public static boolean isSignedElement(String id, SOAPMessageContext context) {
		Set<String> signedElements = (Set<String>) context
				.get(SIGNED_ELEMENTS_CONTEXT_KEY);
		if (null == signedElements) {
			return false;
		}
		boolean result = signedElements.contains(id);
		return result;
	}
}
