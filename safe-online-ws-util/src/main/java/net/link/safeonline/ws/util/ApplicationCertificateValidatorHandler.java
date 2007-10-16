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

	private static final Log LOG = LogFactory
			.getLog(ApplicationCertificateValidatorHandler.class);

	private PkiValidator pkiValidator;

	@PostConstruct
	public void postConstructCallback() {
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
		} catch (TrustDomainNotFoundException e) {
			throw new RuntimeException("application trust domain not found");
		}
		if (false == result) {
			throw new RuntimeException("certificate not trusted");
		}
	}

	@SuppressWarnings("unused")
	private void logout(SOAPMessageContext context) {
		LOG.debug("logout");
	}
}
