/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.device.sdk.reg.saml2;

import java.io.Serializable;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.link.safeonline.device.sdk.ProtocolContext;
import net.link.safeonline.device.sdk.exception.RegistrationFinalizationException;
import net.link.safeonline.device.sdk.exception.RegistrationInitializationException;
import net.link.safeonline.sdk.auth.saml2.AuthnRequestUtil;
import net.link.safeonline.sdk.auth.saml2.AuthnResponseFactory;
import net.link.safeonline.sdk.auth.saml2.AuthnResponseUtil;
import net.link.safeonline.sdk.ws.sts.TrustDomainType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opensaml.DefaultBootstrap;
import org.opensaml.saml2.core.AuthnRequest;
import org.opensaml.saml2.core.RequestedAuthnContext;
import org.opensaml.saml2.core.Response;
import org.opensaml.xml.ConfigurationException;

/**
 * Saml2 Handler used by OLAS to validate incoming SAML requests from remote
 * device issuers who want to register/update or remove a device. And to send
 * out a SAML2 response to the remote device issuer.
 * 
 * The incoming SAML request contains:
 * <ul>
 * <li>Application name: The remote device webapp's name</li>
 * <li>Device name: The name of the registrating device as known to OLAS
 * <li></li>
 * </ul>
 * 
 * The outgoing SAML response contains:
 * <ul>
 * <li>Device mapping ID: The id mapping the registrating device to the
 * registrating OLAS subject. This ID should be used by the remote device issuer
 * to identify the OLAS subject.</li>
 * </ul>
 * 
 * @author wvdhaute
 * 
 */
public class Saml2Handler implements Serializable {

	private static final Log LOG = LogFactory.getLog(Saml2Handler.class);

	private static final long serialVersionUID = 1L;

	private HttpSession session;

	private String stsWsLocation;

	private KeyPair applicationKeyPair;

	public static final String SAML2_HANDLER = Saml2Handler.class.getName()
			+ ".SAML2_HANDLER";

	public static final String IN_RESPONSE_TO_ATTRIBUTE = Saml2Handler.class
			.getName()
			+ ".IN_RESPONSE_TO";

	public static final String TARGET_URL = Saml2Handler.class.getName()
			+ ".TARGET_URL";

	static {
		/*
		 * Next is because Sun loves to endorse crippled versions of Xerces.
		 */
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

	private Saml2Handler(HttpServletRequest request) {
		this.session = request.getSession();
		this.session.setAttribute(SAML2_HANDLER, this);
	}

	public static Saml2Handler getSaml2Handler(HttpServletRequest request) {
		Saml2Handler instance = (Saml2Handler) request.getSession()
				.getAttribute(SAML2_HANDLER);

		if (null == instance)
			instance = new Saml2Handler(request);

		return instance;
	}

	public void init(Map<String, String> configParams,
			KeyPair newApplicationKeyPair) {
		this.stsWsLocation = configParams.get("WsLocation");
		this.applicationKeyPair = newApplicationKeyPair;
	}

	public void initialize(HttpServletRequest request,
			X509Certificate authCertificate, KeyPair authKeyPair)
			throws RegistrationInitializationException {

		AuthnRequest samlAuthnRequest;
		try {
			samlAuthnRequest = AuthnRequestUtil.validateAuthnRequest(request,
					this.stsWsLocation, authCertificate, authKeyPair
							.getPrivate(), TrustDomainType.DEVICE);
		} catch (ServletException e) {
			throw new RegistrationInitializationException(e.getMessage());
		}

		String assertionConsumerService = samlAuthnRequest
				.getAssertionConsumerServiceURL();

		if (null == assertionConsumerService)
			throw new RegistrationInitializationException(
					"missing AssertionConsumerServiceURL");

		if (samlAuthnRequest.getConditions().getAudienceRestrictions()
				.isEmpty()) {
			throw new RegistrationInitializationException(
					"No audience restriction was specified.");
		}

		if (samlAuthnRequest.getConditions().getAudienceRestrictions().get(0)
				.getAudiences().isEmpty()) {
			throw new RegistrationInitializationException(
					"No audience specified in audience restriction");
		}

		String application = samlAuthnRequest.getConditions()
				.getAudienceRestrictions().get(0).getAudiences().get(0)
				.getAudienceURI();

		if (null == application)
			throw new RegistrationInitializationException(
					"No target application was specified");

		String samlAuthnRequestId = samlAuthnRequest.getID();

		RequestedAuthnContext requestedAuthnContext = samlAuthnRequest
				.getRequestedAuthnContext();
		if (null == requestedAuthnContext) {
			throw new RegistrationInitializationException(
					"No device authentication context was specified.");
		}

		if (requestedAuthnContext.getAuthnContextClassRefs().size() != 1) {
			throw new RegistrationInitializationException(
					"Only 1 authentication context reference allowed.");
		}

		String registeredDevice = requestedAuthnContext
				.getAuthnContextClassRefs().get(0).getAuthnContextClassRef();
		if (null == registeredDevice) {
			throw new RegistrationInitializationException(
					"No device authentication context reference was specified.");
		}

		this.session.setAttribute(IN_RESPONSE_TO_ATTRIBUTE, samlAuthnRequestId);
		this.session.setAttribute(TARGET_URL, assertionConsumerService);

		ProtocolContext protocolContext = ProtocolContext
				.getProtocolContext(request.getSession());
		protocolContext.setDeviceName(registeredDevice);
		protocolContext.setApplication(application);
	}

	public void finalize(HttpServletRequest request,
			HttpServletResponse response)
			throws RegistrationFinalizationException {
		ProtocolContext protocolContext = ProtocolContext
				.getProtocolContext(request.getSession());
		String registeredDevice = protocolContext.getDeviceName();
		String mappingId = protocolContext.getMappingId();
		String applicationId = protocolContext.getApplication();
		String target = (String) this.session.getAttribute(TARGET_URL);
		LOG.debug("target: " + target);
		String inResponseTo = (String) this.session
				.getAttribute(IN_RESPONSE_TO_ATTRIBUTE);
		if (null == inResponseTo) {
			throw new RegistrationFinalizationException(
					"missing IN_RESPONSE_TO session attribute");
		}

		String issuerName = protocolContext.getIssuer();
		PrivateKey privateKey = this.applicationKeyPair.getPrivate();
		PublicKey publicKey = this.applicationKeyPair.getPublic();
		int validity = protocolContext.getValidity();

		Response responseMessage = AuthnResponseFactory.createAuthResponse(
				inResponseTo, applicationId, issuerName, mappingId,
				registeredDevice, validity, target);

		try {
			AuthnResponseUtil.sendAuthnResponse(responseMessage, target,
					publicKey, privateKey, response);
		} catch (ServletException e) {
			throw new RegistrationFinalizationException(e.getMessage());
		}
	}

}
