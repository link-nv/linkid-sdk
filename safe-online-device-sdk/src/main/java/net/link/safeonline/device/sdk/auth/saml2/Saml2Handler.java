/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.device.sdk.auth.saml2;

import java.io.Serializable;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.link.safeonline.device.sdk.AuthenticationContext;
import net.link.safeonline.device.sdk.exception.AuthenticationFinalizationException;
import net.link.safeonline.device.sdk.exception.AuthenticationInitializationException;
import net.link.safeonline.sdk.auth.saml2.AuthnRequestUtil;
import net.link.safeonline.sdk.auth.saml2.AuthnResponseFactory;
import net.link.safeonline.sdk.auth.saml2.AuthnResponseUtil;

import org.opensaml.DefaultBootstrap;
import org.opensaml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml2.core.AuthnRequest;
import org.opensaml.saml2.core.RequestedAuthnContext;
import org.opensaml.saml2.core.Response;
import org.opensaml.xml.ConfigurationException;

public class Saml2Handler implements Serializable {

	private static final long serialVersionUID = 1L;

	private HttpSession session;

	private String wsLocation;

	private KeyPair applicationKeyPair;

	private X509Certificate applicationCertificate;

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

		if (null == instance) {
			instance = new Saml2Handler(request);
		}

		return instance;
	}

	public void init(Map<String, String> configParams,
			X509Certificate newApplicationCertificate,
			KeyPair newApplicationKeyPair) {
		this.wsLocation = configParams.get("WsLocation");
		this.applicationCertificate = newApplicationCertificate;
		this.applicationKeyPair = newApplicationKeyPair;
	}

	public void initAuthentication(HttpServletRequest request)
			throws AuthenticationInitializationException {

		AuthnRequest samlAuthnRequest;
		try {
			samlAuthnRequest = AuthnRequestUtil.validateAuthnRequest(request,
					this.wsLocation, this.applicationCertificate,
					this.applicationKeyPair.getPrivate());
		} catch (ServletException e) {
			throw new AuthenticationInitializationException(e.getMessage());
		}

		String assertionConsumerService = samlAuthnRequest
				.getAssertionConsumerServiceURL();

		if (null == assertionConsumerService)
			throw new AuthenticationInitializationException(
					"missing AssertionConsumerServiceURL");

		String application = null;
		try {
			application = samlAuthnRequest.getConditions()
					.getAudienceRestrictions().get(0).getAudiences().get(0)
					.getAudienceURI();
		} catch (Exception e) {
			// empty
		}

		if (null == application)
			throw new AuthenticationInitializationException(
					"No target application was specified");

		String samlAuthnRequestId = samlAuthnRequest.getID();

		RequestedAuthnContext requestedAuthnContext = samlAuthnRequest
				.getRequestedAuthnContext();
		Set<String> devices;

		if (null != requestedAuthnContext) {
			List<AuthnContextClassRef> authnContextClassRefs = requestedAuthnContext
					.getAuthnContextClassRefs();
			devices = new HashSet<String>();
			for (AuthnContextClassRef authnContextClassRef : authnContextClassRefs)
				devices.add(authnContextClassRef.getAuthnContextClassRef());
		} else
			devices = null;

		this.session.setAttribute(IN_RESPONSE_TO_ATTRIBUTE, samlAuthnRequestId);
		this.session.setAttribute(TARGET_URL, assertionConsumerService);

		AuthenticationContext authenticationContext = AuthenticationContext
				.getAuthenticationContext(request.getSession());
		authenticationContext.setWantedDevices(devices);
		authenticationContext.setApplication(application);
	}

	@SuppressWarnings("unchecked")
	public void finalizeAuthentication(HttpServletRequest request,
			HttpServletResponse response)
			throws AuthenticationFinalizationException {
		AuthenticationContext authenticationContext = AuthenticationContext
				.getAuthenticationContext(request.getSession());
		String usedDevice = authenticationContext.getUsedDevice();
		String userId = authenticationContext.getUserId();
		String applicationId = authenticationContext.getApplication();
		String target = (String) this.session.getAttribute(TARGET_URL);
		String inResponseTo = (String) this.session
				.getAttribute(IN_RESPONSE_TO_ATTRIBUTE);
		if (null == inResponseTo)
			throw new AuthenticationFinalizationException(
					"missing IN_RESPONSE_TO session attribute");

		String issuerName = authenticationContext.getIssuer();
		PrivateKey privateKey = this.applicationKeyPair.getPrivate();
		PublicKey publicKey = this.applicationKeyPair.getPublic();
		int validity = authenticationContext.getValidity();

		Response responseMessage = AuthnResponseFactory.createAuthResponse(
				inResponseTo, applicationId, issuerName, userId, usedDevice,
				validity, target);
		try {
			AuthnResponseUtil.sendAuthnResponse(responseMessage, target,
					publicKey, privateKey, response);
		} catch (ServletException e) {
			throw new AuthenticationFinalizationException(e.getMessage());
		}
	}

}
