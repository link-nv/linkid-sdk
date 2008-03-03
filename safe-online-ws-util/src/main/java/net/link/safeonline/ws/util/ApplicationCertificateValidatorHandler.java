/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.ws.util;

import java.security.cert.X509Certificate;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.xml.namespace.QName;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.pkix.exception.TrustDomainNotFoundException;
import net.link.safeonline.pkix.model.PkiValidator;
import net.link.safeonline.util.ee.EjbUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Application Certificate Validator JAX-WS Handler. This JAX-WS SOAP handler
 * will validate the incoming certificate as being a trusted application
 * certificate.
 * 
 * @author fcorneli
 * 
 */
public class ApplicationCertificateValidatorHandler implements
		SOAPHandler<SOAPMessageContext> {

	public static final String CERTIFICATE_DOMAIN_PROPERTY = ApplicationCertificateValidatorHandler.class
			.getName()
			+ ".CertificateDomain";

	public enum CertificateDomain {
		APPLICATION, DEVICE, OLAS
	}

	private static final Log LOG = LogFactory
			.getLog(ApplicationCertificateValidatorHandler.class);

	private PkiValidator pkiValidator;

	@PostConstruct
	public void postConstructCallback() {
		System
				.setProperty(
						"com.sun.xml.ws.fault.SOAPFaultBuilder.disableCaptureStackTrace",
						"true");
		this.pkiValidator = EjbUtils.getEJB(
				"SafeOnline/PkiValidatorBean/local", PkiValidator.class);
	}

	public Set<QName> getHeaders() {
		return null;
	}

	@SuppressWarnings("unused")
	public void close(MessageContext context) {
		// empty
	}

	@SuppressWarnings("unused")
	public boolean handleFault(SOAPMessageContext context) {
		return true;
	}

	public boolean handleMessage(SOAPMessageContext context) {
		Boolean outboundProperty = (Boolean) context
				.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
		if (false == outboundProperty.booleanValue()) {
			login(context);
			return true;
		}
		logout(context);
		return true;
	}

	private void login(SOAPMessageContext context) {
		LOG.debug("login");
		X509Certificate certificate = WSSecurityServerHandler
				.getCertificate(context);
		if (null == certificate) {
			throw new RuntimeException(
					"no client certificate found on JAX-WS context");
		}
		boolean result;
		try {
			result = this.pkiValidator.validateCertificate(
					SafeOnlineConstants.SAFE_ONLINE_APPLICATIONS_TRUST_DOMAIN,
					certificate);
			setCertificateDomain(CertificateDomain.APPLICATION, context);
		} catch (TrustDomainNotFoundException e) {
			throw WSSecurityUtil.createSOAPFaultException(
					"application trust domain not found",
					"FailedAuthentication");
		}
		if (false == result)
			try {
				result = this.pkiValidator.validateCertificate(
						SafeOnlineConstants.SAFE_ONLINE_DEVICES_TRUST_DOMAIN,
						certificate);
				setCertificateDomain(CertificateDomain.DEVICE, context);
			} catch (TrustDomainNotFoundException e) {
				throw WSSecurityUtil.createSOAPFaultException(
						"devices trust domain not found",
						"FailedAuthentication");
			}
		if (false == result) {
			try {
				result = this.pkiValidator.validateCertificate(
						SafeOnlineConstants.SAFE_ONLINE_OLAS_TRUST_DOMAIN,
						certificate);
				setCertificateDomain(CertificateDomain.OLAS, context);
			} catch (TrustDomainNotFoundException e) {
				throw WSSecurityUtil.createSOAPFaultException(
						"olas trust domain not found", "FailedAuthentication");
			}
		}

		if (false == result) {
			throw WSSecurityUtil.createSOAPFaultException(
					"certificate not trusted", "FailedAuthentication");
		}
	}

	@SuppressWarnings("unused")
	private void logout(SOAPMessageContext context) {
		LOG.debug("logout");
	}

	private static void setCertificateDomain(
			CertificateDomain certificateDomain,
			SOAPMessageContext soapMessageContext) {
		soapMessageContext.put(CERTIFICATE_DOMAIN_PROPERTY, certificateDomain);
	}

	private static CertificateDomain getCertificateDomain(
			SOAPMessageContext soapMessageContext) {
		CertificateDomain certificateDomain = (CertificateDomain) soapMessageContext
				.get(CERTIFICATE_DOMAIN_PROPERTY);
		if (null == certificateDomain) {
			throw new RuntimeException(
					"no certificate domain found on JAX-WS context");
		}
		return certificateDomain;
	}

	public static boolean isDeviceCertificate(
			SOAPMessageContext soapMessageContext) {
		return getCertificateDomain(soapMessageContext).equals(
				CertificateDomain.DEVICE);
	}

	public static boolean isApplicationCertificate(
			SOAPMessageContext soapMessageContext) {
		return getCertificateDomain(soapMessageContext).equals(
				CertificateDomain.APPLICATION);
	}

	public static boolean isCoreCertificate(
			SOAPMessageContext soapMessageContext) {
		return getCertificateDomain(soapMessageContext).equals(
				CertificateDomain.OLAS);
	}
}
