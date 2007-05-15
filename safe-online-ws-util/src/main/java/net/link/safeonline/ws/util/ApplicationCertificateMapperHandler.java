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

import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.service.AuthenticationService;
import net.link.safeonline.util.ee.EjbUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Application Certificate JAX-WS Login Handler. This JAX-WS SOAP handler maps a
 * trusted certificate to an application name.
 * 
 * @author fcorneli
 * 
 */
public class ApplicationCertificateMapperHandler implements
		SOAPHandler<SOAPMessageContext> {

	private static final Log LOG = LogFactory
			.getLog(ApplicationCertificateMapperHandler.class);

	private AuthenticationService authenticationService;

	@PostConstruct
	public void postConstructCallback() {
		this.authenticationService = EjbUtils.getEJB(
				"SafeOnline/AuthenticationServiceBean/local",
				AuthenticationService.class);
	}

	public Set<QName> getHeaders() {
		return null;
	}

	public void close(MessageContext context) {
	}

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
		String applicationName;
		try {
			applicationName = this.authenticationService
					.authenticate(certificate);
		} catch (ApplicationNotFoundException e) {
			throw new RuntimeException("unknown application");
		}

		context.put(ApplicationLoginHandler.APPLICATION_NAME_PROPERTY,
				applicationName);
	}

	private void logout(SOAPMessageContext context) {
		LOG.debug("logout");
	}
}
