/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.attrib.ws;

import java.security.cert.X509Certificate;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.xml.namespace.QName;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.authentication.exception.TrustDomainNotFoundException;
import net.link.safeonline.model.PkiValidator;
import net.link.safeonline.util.ee.EjbUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Application Certificate JAX-WS Login Handler. This JAX-WS SOAP handler will
 * validate the incoming certificate as being a trusted application certificate
 * and will perform the JAAS login.
 * 
 * @author fcorneli
 * 
 */
public class ApplicationCertificateLoginHandler implements
		SOAPHandler<SOAPMessageContext> {

	private static final Log LOG = LogFactory
			.getLog(ApplicationCertificateLoginHandler.class);

	public static final String CERTIFICATE_PROPERTY = "net.link.safeonline.x509";

	private static final String LOGINCONTEXT_PROPERTY = "net.link.safeonline.logincontext";

	private PkiValidator pkiValidator;

	@PostConstruct
	public void postConstructCallback() {
		this.pkiValidator = EjbUtils.getEJB(
				"SafeOnline/PkiValidatorBean/local", PkiValidator.class);
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
		X509Certificate certificate = (X509Certificate) context
				.get(CERTIFICATE_PROPERTY);
		if (null == certificate) {
			throw new RuntimeException(
					"no client certificate found on JAX-WS context");
		}
		boolean result;
		try {
			result = this.pkiValidator.validateCertificate(
					SafeOnlineConstants.SAFE_ONLINE_APPLICATIONS_TRUST_DOMAIN,
					certificate);
		} catch (TrustDomainNotFoundException e1) {
			throw new RuntimeException("application trust domain not found");
		}
		if (false == result) {
			throw new RuntimeException("certificate not trusted");
		}
		X509CertificateCallbackHandler callbackHandler = new X509CertificateCallbackHandler(
				certificate);
		try {
			LoginContext loginContext = new LoginContext("client-login",
					callbackHandler);
			LOG.debug("performing login...");
			loginContext.login();
			context.put(LOGINCONTEXT_PROPERTY, loginContext);
		} catch (LoginException e) {
			throw new RuntimeException("JAAS login error: " + e.getMessage());
		}
	}

	private void logout(SOAPMessageContext context) {
		LOG.debug("logout");
		LoginContext loginContext = (LoginContext) context
				.get(LOGINCONTEXT_PROPERTY);
		if (null == loginContext) {
			throw new RuntimeException(
					"no JAAS login context present on the JAX-WS context");
		}
		try {
			loginContext.logout();
		} catch (LoginException e) {
			throw new RuntimeException("JAAS logout error");
		}
	}
}
