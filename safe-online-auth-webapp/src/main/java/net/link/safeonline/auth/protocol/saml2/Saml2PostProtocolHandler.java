/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.auth.protocol.saml2;

import java.security.cert.X509Certificate;

import javax.servlet.http.HttpServletRequest;
import javax.xml.namespace.QName;

import net.link.safeonline.auth.protocol.ProtocolException;
import net.link.safeonline.auth.protocol.ProtocolHandler;
import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.service.ApplicationAuthenticationService;
import net.link.safeonline.util.ee.EjbUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opensaml.common.SAMLObject;
import org.opensaml.common.binding.BindingException;
import org.opensaml.common.binding.security.SAMLSecurityPolicy;
import org.opensaml.common.binding.security.SAMLSecurityPolicyFactory;
import org.opensaml.saml2.binding.decoding.HTTPPostDecoder;
import org.opensaml.saml2.binding.decoding.HTTPPostDecoderBuilder;
import org.opensaml.saml2.binding.security.SAML2ProtocolMessageRuleFactory;
import org.opensaml.saml2.core.AuthnRequest;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.ws.security.SecurityPolicyException;
import org.opensaml.xml.parse.BasicParserPool;
import org.opensaml.xml.security.x509.BasicX509Credential;
import org.opensaml.xml.signature.SignatureValidator;
import org.opensaml.xml.validation.ValidationException;

/**
 * Server-side protocol handler for the SAML2 Browser POST authentication
 * protocol.
 * 
 * @author fcorneli
 * 
 */
public class Saml2PostProtocolHandler implements ProtocolHandler {

	private static final Log LOG = LogFactory
			.getLog(Saml2PostProtocolHandler.class);

	public String getName() {
		return "SAML v2 Browser POST Authentication Protocol";
	}

	public String handleRequest(HttpServletRequest authnRequest)
			throws ProtocolException {
		LOG.debug("request method: " + authnRequest.getMethod());
		if (false == "POST".equals(authnRequest.getMethod())) {
			return null;
		}
		LOG.debug("POST request");
		String encodedSamlRequest = authnRequest.getParameter("SAMLRequest");
		if (null == encodedSamlRequest) {
			return null;
		}
		LOG.debug("SAMLRequest parameter found");

		HTTPPostDecoderBuilder postDecoderBuilder = new HTTPPostDecoderBuilder();
		BasicParserPool parser = new BasicParserPool();
		parser.setNamespaceAware(true);
		postDecoderBuilder.setParser(parser);
		HTTPPostDecoder postDecoder = postDecoderBuilder.buildDecoder();
		postDecoder.setRequest(authnRequest);

		SAMLSecurityPolicyFactory samlSecurityPolicyFactory = new SAMLSecurityPolicyFactory();
		samlSecurityPolicyFactory.setIssuerRole(new QName("someURI", "dummy",
				"abc123"));
		samlSecurityPolicyFactory.setIssuerProtocol("dummy-protocol");

		/*
		 * We will validate the signature outside of the opensaml2 framework.
		 */
		samlSecurityPolicyFactory.setRequiredAuthenticatedIssuer(false);

		SAML2ProtocolMessageRuleFactory samlProtocolMessageRuleFactory = new SAML2ProtocolMessageRuleFactory();
		samlSecurityPolicyFactory.getPolicyRuleFactories().add(
				samlProtocolMessageRuleFactory);
		/*
		 * We tried to use
		 * SAMLProtocolMessageXMLSignatureSecurityPolicyRuleFactory here, but
		 * the code is overly complex and the PKIX validation is using the Java
		 * certpathbuilder stuff.
		 */

		SAMLSecurityPolicy samlSecurityPolicy = (SAMLSecurityPolicy) samlSecurityPolicyFactory
				.createPolicyInstance();
		postDecoder.setSecurityPolicy(samlSecurityPolicy);
		try {
			postDecoder.decode();
		} catch (BindingException e) {
			throw new ProtocolException("SAML request binding exception: "
					+ e.getMessage());
		} catch (SecurityPolicyException e) {
			throw new ProtocolException("SAML security policy exception");
		}

		SAMLObject samlMessage = postDecoder.getSAMLMessage();
		if (false == samlMessage instanceof AuthnRequest) {
			throw new ProtocolException(
					"SAML message not an authentication request message");
		}
		AuthnRequest samlAuthnRequest = (AuthnRequest) samlMessage;
		Issuer issuer = samlAuthnRequest.getIssuer();
		String issuerName = issuer.getValue();
		LOG.debug("issuer name: " + issuerName);

		ApplicationAuthenticationService applicationAuthenticationService = EjbUtils
				.getEJB(
						"SafeOnline/ApplicationAuthenticationServiceBean/local",
						ApplicationAuthenticationService.class);

		X509Certificate certificate;
		try {
			certificate = applicationAuthenticationService
					.getCertificate(issuerName);
		} catch (ApplicationNotFoundException e) {
			throw new ProtocolException("application not found: " + issuerName);
		}

		BasicX509Credential basicX509Credential = new BasicX509Credential();
		basicX509Credential.setPublicKey(certificate.getPublicKey());
		SignatureValidator signatureValidator = new SignatureValidator(
				basicX509Credential);
		try {
			signatureValidator.validate(samlAuthnRequest.getSignature());
		} catch (ValidationException e) {
			throw new ProtocolException("signature validation error: "
					+ e.getMessage());
		}

		return issuerName;
	}
}
