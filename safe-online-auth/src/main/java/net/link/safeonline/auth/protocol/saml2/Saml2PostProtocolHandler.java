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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.auth.LoginManager;
import net.link.safeonline.auth.protocol.ProtocolContext;
import net.link.safeonline.auth.protocol.ProtocolException;
import net.link.safeonline.auth.protocol.ProtocolHandler;
import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.exception.NodeNotFoundException;
import net.link.safeonline.authentication.service.ApplicationAuthenticationService;
import net.link.safeonline.authentication.service.DevicePolicyService;
import net.link.safeonline.authentication.service.NodeAuthenticationService;
import net.link.safeonline.authentication.service.SamlAuthorityService;
import net.link.safeonline.entity.DeviceEntity;
import net.link.safeonline.pkix.exception.TrustDomainNotFoundException;
import net.link.safeonline.pkix.model.PkiValidator;
import net.link.safeonline.sdk.auth.saml2.AuthnResponseFactory;
import net.link.safeonline.sdk.auth.saml2.AuthnResponseUtil;
import net.link.safeonline.sdk.auth.saml2.SamlRequestSecurityPolicyResolver;
import net.link.safeonline.util.ee.AuthIdentityServiceClient;
import net.link.safeonline.util.ee.EjbUtils;
import net.link.safeonline.util.ee.IdentityServiceClient;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opensaml.DefaultBootstrap;
import org.opensaml.common.SAMLObject;
import org.opensaml.common.binding.BasicSAMLMessageContext;
import org.opensaml.saml2.binding.decoding.HTTPPostDecoder;
import org.opensaml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml2.core.AuthnRequest;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.saml2.core.RequestedAuthnContext;
import org.opensaml.saml2.core.Response;
import org.opensaml.ws.message.decoder.MessageDecodingException;
import org.opensaml.ws.security.SecurityPolicyException;
import org.opensaml.ws.transport.http.HttpServletRequestAdapter;
import org.opensaml.xml.ConfigurationException;
import org.opensaml.xml.security.SecurityException;
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

	private final AuthIdentityServiceClient authIdentityServiceClient;

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
		this.authIdentityServiceClient = new AuthIdentityServiceClient();
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

		messageContext
				.setSecurityPolicyResolver(new SamlRequestSecurityPolicyResolver());

		HTTPPostDecoder decoder = new HTTPPostDecoder();
		try {
			decoder.decode(messageContext);
		} catch (MessageDecodingException e) {
			LOG.debug("SAML message decoding error: " + e.getMessage());
			throw new ProtocolException("SAML message decoding error");
		} catch (SecurityPolicyException e) {
			LOG.debug("security policy error: " + e.getMessage());
			throw new ProtocolException("security policy error");
		} catch (SecurityException e) {
			LOG.debug("security error: " + e.getMessage());
			throw new ProtocolException("security error");
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

		DevicePolicyService devicePolicyService = EjbUtils.getEJB(
				"SafeOnline/DevicePolicyServiceBean/local",
				DevicePolicyService.class);

		RequestedAuthnContext requestedAuthnContext = samlAuthnRequest
				.getRequestedAuthnContext();
		Set<DeviceEntity> devices;
		if (null != requestedAuthnContext) {
			List<AuthnContextClassRef> authnContextClassRefs = requestedAuthnContext
					.getAuthnContextClassRefs();
			devices = new HashSet<DeviceEntity>();
			for (AuthnContextClassRef authnContextClassRef : authnContextClassRefs) {
				String authnContextClassRefValue = authnContextClassRef
						.getAuthnContextClassRef();
				LOG.debug("authentication context class reference: "
						+ authnContextClassRefValue);
				List<DeviceEntity> authnDevices = devicePolicyService
						.listDevices(authnContextClassRefValue);
				if (null == authnDevices || authnDevices.size() == 0) {
					LOG.error("AuthnContextClassRef not supported: "
							+ authnContextClassRefValue);
					throw new ProtocolException(
							"AuthnContextClassRef not supported: "
									+ authnContextClassRefValue);
				}
				devices.addAll(authnDevices);
			}
		} else {
			devices = null;
		}

		return new ProtocolContext(issuerName, assertionConsumerService,
				devices);
	}

	public void authnResponse(HttpSession session,
			HttpServletResponse authnResponse) throws ProtocolException {
		PrivateKey privateKey = this.identityServiceClient.getPrivateKey();
		PublicKey publicKey = this.identityServiceClient.getPublicKey();
		String userId = LoginManager.getUsername(session);
		String target = LoginManager.getTarget(session);
		String applicationId = LoginManager.getApplication(session);
		LOG.debug("user Id: " + userId);
		LOG.debug("target URL: " + target);
		LOG.debug("application: " + applicationId);

		NodeAuthenticationService nodeAuthenticationService = EjbUtils.getEJB(
				"SafeOnline/NodeAuthenticationServiceBean/local",
				NodeAuthenticationService.class);
		String nodeName;
		try {
			nodeName = nodeAuthenticationService
					.authenticate(this.authIdentityServiceClient
							.getCertificate());
		} catch (NodeNotFoundException e) {
			throw new ProtocolException("unknown node");
		}

		String authnContextClass = getAuthnContextClass(session);
		String issuerName = nodeName;
		int validity = this.samlAuthorityService.getAuthnAssertionValidity();
		String inResponseTo = (String) session
				.getAttribute(IN_RESPONSE_TO_ATTRIBUTE);
		if (null == inResponseTo) {
			throw new ProtocolException(
					"missing IN_RESPONSE_TO session attribute");
		}
		Response responseMessage = AuthnResponseFactory.createAuthResponse(
				inResponseTo, applicationId, issuerName, userId,
				authnContextClass, validity, target);
		try {
			AuthnResponseUtil.sendAuthnResponse(responseMessage, target,
					publicKey, privateKey, authnResponse);
		} catch (ServletException e) {
			throw new ProtocolException(e.getMessage());
		}
	}

	private String getAuthnContextClass(HttpSession session) {
		DeviceEntity device = LoginManager.getAuthenticationDevice(session);
		return device.getAuthenticationContextClass();
	}
}
