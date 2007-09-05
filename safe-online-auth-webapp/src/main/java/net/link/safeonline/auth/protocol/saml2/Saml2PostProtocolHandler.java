/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.auth.protocol.saml2;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.auth.Device;
import net.link.safeonline.auth.protocol.ProtocolContext;
import net.link.safeonline.auth.protocol.ProtocolException;
import net.link.safeonline.auth.protocol.ProtocolHandler;
import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.service.ApplicationAuthenticationService;
import net.link.safeonline.authentication.service.SamlAuthorityService;
import net.link.safeonline.pkix.exception.TrustDomainNotFoundException;
import net.link.safeonline.pkix.model.PkiValidator;
import net.link.safeonline.util.ee.EjbUtils;
import net.link.safeonline.util.ee.IdentityServiceClient;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.log.Log4JLogChute;
import org.opensaml.DefaultBootstrap;
import org.opensaml.common.SAMLObject;
import org.opensaml.common.binding.BasicSAMLMessageContext;
import org.opensaml.saml2.binding.decoding.HTTPPostDecoder;
import org.opensaml.saml2.binding.encoding.HTTPPostEncoder;
import org.opensaml.saml2.binding.security.SAML2ProtocolMessageRule;
import org.opensaml.saml2.core.AuthnRequest;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.metadata.AssertionConsumerService;
import org.opensaml.ws.message.decoder.MessageDecodingException;
import org.opensaml.ws.message.encoder.MessageEncodingException;
import org.opensaml.ws.security.SecurityPolicy;
import org.opensaml.ws.security.SecurityPolicyException;
import org.opensaml.ws.security.provider.BasicSecurityPolicy;
import org.opensaml.ws.security.provider.HTTPRule;
import org.opensaml.ws.security.provider.MandatoryIssuerRule;
import org.opensaml.ws.transport.http.HttpServletRequestAdapter;
import org.opensaml.ws.transport.http.HttpServletResponseAdapter;
import org.opensaml.xml.ConfigurationException;
import org.opensaml.xml.security.SecurityHelper;
import org.opensaml.xml.security.credential.BasicCredential;
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

	public static final String NAME = "SAML v2 Browser POST Authentication Protocol";

	private final IdentityServiceClient identityServiceClient;

	private final SamlAuthorityService samlAuthorityService;

	static {
		System
				.setProperty(
						"javax.xml.validation.SchemaFactory:http://www.w3.org/2001/XMLSchema",
						"org.apache.xerces.jaxp.validation.XMLSchemaFactory");
		try {
			DefaultBootstrap.bootstrap();
		} catch (ConfigurationException e) {
			throw new RuntimeException(
					"could not bootstrap the OpenSAML2 library");
		}
	}

	public static final String IN_RESPONSE_TO_ATTRIBUTE = Saml2PostProtocolHandler.class
			.getName()
			+ ".IN_RESPONSE_TO";

	public Saml2PostProtocolHandler() {
		this.identityServiceClient = new IdentityServiceClient();
		this.samlAuthorityService = EjbUtils.getEJB(
				"SafeOnline/SamlAuthorityServiceBean/local",
				SamlAuthorityService.class);
	}

	public String getName() {
		return NAME;
	}

	public ProtocolContext handleRequest(HttpServletRequest authnRequest)
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

		BasicSAMLMessageContext<SAMLObject, SAMLObject, SAMLObject> messageContext = new BasicSAMLMessageContext<SAMLObject, SAMLObject, SAMLObject>();
		messageContext
				.setInboundMessageTransport(new HttpServletRequestAdapter(
						authnRequest));

		SecurityPolicy securityPolicy = new BasicSecurityPolicy();
		securityPolicy.getPolicyRules().add(new HTTPRule(null, "POST", false));
		securityPolicy.getPolicyRules().add(new SAML2ProtocolMessageRule());
		securityPolicy.getPolicyRules().add(new MandatoryIssuerRule());
		// securityPolicy.getPolicyRules().add(new MessageReplayRule(...));
		messageContext.setSecurityPolicy(securityPolicy);

		HTTPPostDecoder decoder = new HTTPPostDecoder();
		try {
			decoder.decode(messageContext);
		} catch (MessageDecodingException e) {
			LOG.debug("SAML message decoding error: " + e.getMessage());
			throw new ProtocolException("SAML message decoding error");
		} catch (SecurityPolicyException e) {
			LOG.debug("security policy error: " + e.getMessage());
			throw new ProtocolException("security policy error");
		}

		SAMLObject samlMessage = messageContext.getInboundSAMLMessage();
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

		PkiValidator pkiValidator = EjbUtils.getEJB(
				"SafeOnline/PkiValidatorBean/local", PkiValidator.class);

		boolean certificateValid;
		try {
			certificateValid = pkiValidator.validateCertificate(
					SafeOnlineConstants.SAFE_ONLINE_APPLICATIONS_TRUST_DOMAIN,
					certificate);
		} catch (TrustDomainNotFoundException e) {
			throw new ProtocolException("trust domain error");
		}

		if (false == certificateValid) {
			throw new ProtocolException("certificate was not found to be valid");
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

		String assertionConsumerService = samlAuthnRequest
				.getAssertionConsumerServiceURL();
		if (null == assertionConsumerService) {
			LOG.debug("missing AssertionConsumerServiceURL");
			throw new ProtocolException("missing AssertionConsumerServiceURL");
		}

		String samlAuthnRequestId = samlAuthnRequest.getID();
		LOG.debug("SAML authn request ID: " + samlAuthnRequestId);
		HttpSession session = authnRequest.getSession();
		session.setAttribute(IN_RESPONSE_TO_ATTRIBUTE, samlAuthnRequestId);

		return new ProtocolContext(issuerName, assertionConsumerService);
	}

	@SuppressWarnings("unchecked")
	public void authnResponse(HttpSession session,
			HttpServletResponse authnResponse) throws ProtocolException {
		PrivateKey privateKey = this.identityServiceClient.getPrivateKey();
		PublicKey publicKey = this.identityServiceClient.getPublicKey();
		String userId = (String) session.getAttribute("username");
		String target = (String) session.getAttribute("target");
		LOG.debug("user Id: " + userId);
		LOG.debug("target URL: " + target);

		BasicSAMLMessageContext messageContext = new BasicSAMLMessageContext();
		messageContext
				.setOutboundMessageTransport(new HttpServletResponseAdapter(
						authnResponse));
		SafeOnlineAuthnContextClass authnContextClass = getAuthnContextClass(session);
		String issuerName = this.samlAuthorityService.getIssuerName();
		int validity = this.samlAuthorityService.getAuthnAssertionValidity();
		String inResponseTo = (String) session
				.getAttribute(IN_RESPONSE_TO_ATTRIBUTE);
		if (null == inResponseTo) {
			throw new ProtocolException(
					"missing IN_RESPONSE_TO session attribute");
		}
		Response responseMessage = AuthnResponseFactory.createAuthResponse(
				inResponseTo, issuerName, userId, authnContextClass, validity);
		messageContext.setOutboundSAMLMessage(responseMessage);

		AssertionConsumerService assertionConsumerService = AuthnResponseFactory
				.buildXMLObject(AssertionConsumerService.class,
						AssertionConsumerService.DEFAULT_ELEMENT_NAME);
		assertionConsumerService.setLocation(target);
		messageContext.setPeerEntityEndpoint(assertionConsumerService);

		BasicCredential signingCredential = SecurityHelper.getSimpleCredential(
				publicKey, privateKey);
		messageContext
				.setOutboundSAMLMessageSigningCredential(signingCredential);

		Properties velocityProperties = new Properties();
		velocityProperties.put("resource.loader", "class");
		velocityProperties.put(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS,
				Log4JLogChute.class.getName());
		velocityProperties.put(Log4JLogChute.RUNTIME_LOG_LOG4J_LOGGER,
				Saml2PostProtocolHandler.class.getName());
		velocityProperties
				.put("class.resource.loader.class",
						"org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
		VelocityEngine velocityEngine;
		try {
			velocityEngine = new VelocityEngine(velocityProperties);
			velocityEngine.init();
		} catch (Exception e) {
			throw new ProtocolException("could not initialize velocity engine");
		}

		HTTPPostEncoder postEncoder = new HTTPPostEncoder(velocityEngine,
				"/templates/saml2-post-binding.vm");
		try {
			postEncoder.encode(messageContext);
		} catch (MessageEncodingException e) {
			LOG.debug("message encoding exception: " + e.getMessage(), e);
			throw new ProtocolException("message encoding error: "
					+ e.getMessage());
		}
	}

	private SafeOnlineAuthnContextClass getAuthnContextClass(HttpSession session)
			throws ProtocolException {
		String device = (String) session
				.getAttribute(Device.AUTHN_DEVICE_ATTRIBUTE);
		if ("beid".equals(device)) {
			return SafeOnlineAuthnContextClass.SMART_CARD_PKI;
		}
		if ("password".equals(device)) {
			return SafeOnlineAuthnContextClass.PASSWORD_PROTECTED_TRANSPORT;
		}
		throw new ProtocolException("unsupported device: " + device);
	}
}
