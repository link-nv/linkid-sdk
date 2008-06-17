/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.device.sdk.auth.saml2;

import java.io.IOException;
import java.io.Serializable;
import java.security.KeyPair;
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
import net.link.safeonline.sdk.ws.sts.TrustDomainType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xml.security.utils.Base64;
import org.opensaml.DefaultBootstrap;
import org.opensaml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml2.core.AuthnRequest;
import org.opensaml.saml2.core.RequestedAuthnContext;
import org.opensaml.xml.ConfigurationException;

/**
 * SAML handler used by remote device issuers to handle an incoming SAML
 * authentication request and store the retrieved information on the session
 * into {@link AuthenticationContext}.
 * 
 * After authenticating it will post a SAML authentication response containing
 * the necessary assertions or a SAML authentication response telling the
 * authentication has failed.
 * 
 * @author wvdhaute
 * 
 */
public class Saml2Handler implements Serializable {

	private static final long serialVersionUID = 1L;

	private static final Log LOG = LogFactory.getLog(Saml2Handler.class);

	public static final String SAML2_POST_BINDING_VM_RESOURCE = "/net/link/safeonline/device/sdk/saml2/saml2-post-binding.vm";

	private HttpSession session;

	private String stsWsLocation;

	private String issuer;

	private KeyPair applicationKeyPair;

	private X509Certificate applicationCertificate;

	public static final String SAML2_HANDLER = Saml2Handler.class.getName()
			+ ".SAML2_HANDLER";

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

	public static Saml2Handler findSaml2Handler(HttpServletRequest request) {
		Saml2Handler instance = (Saml2Handler) request.getSession()
				.getAttribute(SAML2_HANDLER);
		return instance;
	}

	public void init(Map<String, String> configParams,
			X509Certificate newApplicationCertificate,
			KeyPair newApplicationKeyPair)
			throws AuthenticationInitializationException {
		this.stsWsLocation = configParams.get("StsWsLocation");
		this.issuer = configParams.get("DeviceName");
		this.applicationCertificate = newApplicationCertificate;
		this.applicationKeyPair = newApplicationKeyPair;
		if (null == this.stsWsLocation) {
			throw new AuthenticationInitializationException(
					"Missing STS WS Location ( \"StsWsLocation\" )");
		}
	}

	public void initAuthentication(HttpServletRequest request)
			throws AuthenticationInitializationException {

		AuthnRequest samlAuthnRequest;
		try {
			samlAuthnRequest = AuthnRequestUtil.validateAuthnRequest(request,
					this.stsWsLocation, this.applicationCertificate,
					this.applicationKeyPair.getPrivate(), TrustDomainType.NODE);
		} catch (ServletException e) {
			throw new AuthenticationInitializationException(e.getMessage());
		}

		String assertionConsumerService = samlAuthnRequest
				.getAssertionConsumerServiceURL();

		if (null == assertionConsumerService)
			throw new AuthenticationInitializationException(
					"missing AssertionConsumerServiceURL");

		if (samlAuthnRequest.getConditions().getAudienceRestrictions()
				.isEmpty())
			throw new AuthenticationInitializationException(
					"missing audience restriction");

		String application = samlAuthnRequest.getConditions()
				.getAudienceRestrictions().get(0).getAudiences().get(0)
				.getAudienceURI();

		if (null == application) {
			throw new AuthenticationInitializationException(
					"No target application was specified");
		}
		LOG.debug("application: " + application);

		String nodeName = samlAuthnRequest.getIssuer().getValue();

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
		} else {
			devices = null;
		}

		this.session.setAttribute("applicationId", application);

		AuthenticationContext authenticationContext = AuthenticationContext
				.getAuthenticationContext(request.getSession());
		authenticationContext.setWantedDevices(devices);
		authenticationContext.setApplication(application);
		authenticationContext.setNodeName(nodeName);
		authenticationContext.setInResponseTo(samlAuthnRequestId);
		authenticationContext.setTargetUrl(assertionConsumerService);
		authenticationContext.setIssuer(this.issuer);
	}

	public void finalizeAuthentication(HttpServletRequest request,
			HttpServletResponse response)
			throws AuthenticationFinalizationException {
		AuthenticationContext authenticationContext = AuthenticationContext
				.getAuthenticationContext(request.getSession());
		String usedDevice = authenticationContext.getUsedDevice();
		String userId = authenticationContext.getUserId();
		String applicationId = authenticationContext.getApplication();
		String target = authenticationContext.getTargetUrl();
		String inResponseTo = authenticationContext.getInResponseTo();
		if (null == inResponseTo) {
			throw new AuthenticationFinalizationException(
					"missing IN_RESPONSE_TO session attribute");
		}

		String issuerName = authenticationContext.getIssuer();
		int validity = authenticationContext.getValidity();

		String samlResponseToken;
		if (null == userId || null == usedDevice) {
			/*
			 * Authentication must have failed
			 */
			samlResponseToken = AuthnResponseFactory.createAuthResponseFailed(
					inResponseTo, issuerName, this.applicationKeyPair, target);
		} else {
			/*
			 * Authentication was successful
			 */
			samlResponseToken = AuthnResponseFactory.createAuthResponse(
					inResponseTo, applicationId, issuerName, userId,
					usedDevice, this.applicationKeyPair, validity, target);
		}

		String encodedSamlResponseToken = Base64.encode(samlResponseToken
				.getBytes());

		String templateResourceName = SAML2_POST_BINDING_VM_RESOURCE;

		try {
			AuthnResponseUtil.sendAuthnResponse(encodedSamlResponseToken,
					templateResourceName, target, response);
		} catch (ServletException e) {
			throw new AuthenticationFinalizationException(e.getMessage());
		} catch (IOException e) {
			throw new AuthenticationFinalizationException(e.getMessage());
		}
	}

}
