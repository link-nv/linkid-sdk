/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.device.sdk.saml2;

import java.io.IOException;
import java.io.Serializable;
import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.link.safeonline.device.sdk.ProtocolContext;
import net.link.safeonline.device.sdk.exception.DeviceFinalizationException;
import net.link.safeonline.device.sdk.exception.DeviceInitializationException;
import net.link.safeonline.sdk.auth.saml2.AuthnRequestUtil;
import net.link.safeonline.sdk.auth.saml2.AuthnResponseFactory;
import net.link.safeonline.sdk.auth.saml2.AuthnResponseUtil;
import net.link.safeonline.sdk.auth.saml2.DeviceOperationType;
import net.link.safeonline.sdk.ws.sts.TrustDomainType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xml.security.utils.Base64;
import org.opensaml.DefaultBootstrap;
import org.opensaml.saml2.core.AuthnRequest;
import org.opensaml.saml2.core.RequestedAuthnContext;
import org.opensaml.xml.ConfigurationException;

/**
 * SAML handler used by remote device issuers to handle an incoming SAML
 * authentication request used for registration, updating or removal and store
 * the retrieved information on the session into {@link ProtocolContext}.
 * 
 * After registrating, updating or removing it will post a SAML authentication
 * response containing the necessary assertions or a SAML authentication
 * response telling the authentication has failed.
 * 
 * @author wvdhaute
 * 
 */
public class Saml2Handler implements Serializable {

	private static final long serialVersionUID = 1L;

	private static final Log LOG = LogFactory.getLog(Saml2Handler.class);

	public static final String SAML2_POST_BINDING_VM_RESOURCE = "/net/link/safeonline/device/sdk/saml2/binding/saml2-post-binding.vm";

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

	private Saml2Handler() {
	}

	public static Saml2Handler getSaml2Handler(HttpServletRequest request) {
		Saml2Handler instance = (Saml2Handler) request.getSession()
				.getAttribute(SAML2_HANDLER);
		if (null == instance) {
			instance = new Saml2Handler();
			request.getSession().setAttribute(SAML2_HANDLER, instance);
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
			KeyPair newApplicationKeyPair) throws DeviceInitializationException {
		this.stsWsLocation = configParams.get("StsWsLocation");
		this.issuer = configParams.get("DeviceName");
		this.applicationCertificate = newApplicationCertificate;
		this.applicationKeyPair = newApplicationKeyPair;
		if (null == this.stsWsLocation) {
			throw new DeviceInitializationException(
					"Missing STS WS Location ( \"StsWsLocation\" )");
		}
	}

	public DeviceOperationType initDeviceOperation(HttpServletRequest request)
			throws DeviceInitializationException {

		AuthnRequest samlAuthnRequest;
		try {
			samlAuthnRequest = AuthnRequestUtil.validateAuthnRequest(request,
					this.stsWsLocation, this.applicationCertificate,
					this.applicationKeyPair.getPrivate(), TrustDomainType.NODE);
		} catch (ServletException e) {
			throw new DeviceInitializationException(e.getMessage());
		}

		String assertionConsumerService = samlAuthnRequest
				.getAssertionConsumerServiceURL();
		if (null == assertionConsumerService) {
			throw new DeviceInitializationException(
					"missing AssertionConsumerServiceURL");
		}
		LOG.debug("assertion consumer: " + assertionConsumerService);

		if (null == samlAuthnRequest.getConditions()) {
			throw new DeviceInitializationException("missing condition");
		}
		if (samlAuthnRequest.getConditions().getAudienceRestrictions()
				.isEmpty())
			throw new DeviceInitializationException(
					"missing audience restriction");

		String nodeName = samlAuthnRequest.getIssuer().getValue();
		LOG.debug("node name: " + nodeName);

		String samlAuthnRequestId = samlAuthnRequest.getID();

		RequestedAuthnContext requestedAuthnContext = samlAuthnRequest
				.getRequestedAuthnContext();
		if (null == requestedAuthnContext) {
			throw new DeviceInitializationException(
					"missing requested authentication context");
		}
		if (requestedAuthnContext.getAuthnContextClassRefs().size() != 1) {
			throw new DeviceInitializationException(
					"authentication context should contain exactly 1 reference");
		}
		String device = requestedAuthnContext.getAuthnContextClassRefs().get(0)
				.getAuthnContextClassRef();
		LOG.debug("device: " + device);

		if (null == samlAuthnRequest.getSubject()) {
			throw new DeviceInitializationException("missing subject");
		}
		if (null == samlAuthnRequest.getSubject().getNameID()) {
			throw new DeviceInitializationException("missing subject name ID");
		}
		String deviceUserId = samlAuthnRequest.getSubject().getNameID()
				.getValue();
		LOG.debug("device user id: " + deviceUserId);

		if (samlAuthnRequest.getConditions().getAudienceRestrictions().size() != 1) {
			throw new DeviceInitializationException(
					"authentication request should contain exactly 1 audience restriction");
		}
		if (samlAuthnRequest.getConditions().getAudienceRestrictions().get(0)
				.getAudiences().size() != 1) {
			throw new DeviceInitializationException(
					"authentication request should contain exactly 1 audience");
		}

		DeviceOperationType deviceOperation = DeviceOperationType
				.valueOf(samlAuthnRequest.getConditions()
						.getAudienceRestrictions().get(0).getAudiences().get(0)
						.getAudienceURI());
		LOG.debug("device operation: " + deviceOperation);

		ProtocolContext protocolContext = ProtocolContext
				.getProtocolContext(request.getSession());
		protocolContext.setIssuer(this.issuer);
		protocolContext.setTargetUrl(assertionConsumerService);
		protocolContext.setInResponseTo(samlAuthnRequestId);
		protocolContext.setWantedDevice(device);
		protocolContext.setSubject(deviceUserId);
		protocolContext.setNodeName(nodeName);
		protocolContext.setDeviceOperation(deviceOperation);

		request.getSession().setAttribute("userId", deviceUserId);

		return deviceOperation;
	}

	public void abortDeviceOperation(HttpServletRequest request,
			HttpServletResponse response) throws DeviceFinalizationException {
		ProtocolContext protocolContext = ProtocolContext
				.getProtocolContext(request.getSession());
		String issuerName = protocolContext.getIssuer();
		String target = protocolContext.getTargetUrl();
		String inResponseTo = protocolContext.getInResponseTo();
		if (null == inResponseTo) {
			throw new DeviceFinalizationException(
					"missing IN_RESPONSE_TO session attribute");
		}

		String samlResponseToken = AuthnResponseFactory
				.createAuthResponseUnsupported(inResponseTo, issuerName,
						this.applicationKeyPair, target);

		String encodedSamlResponseToken = Base64.encode(samlResponseToken
				.getBytes());

		String templateResourceName = SAML2_POST_BINDING_VM_RESOURCE;

		try {
			AuthnResponseUtil.sendAuthnResponse(encodedSamlResponseToken,
					templateResourceName, target, response);
		} catch (ServletException e) {
			throw new DeviceFinalizationException(e.getMessage());
		} catch (IOException e) {
			throw new DeviceFinalizationException(e.getMessage());
		}
	}

	public void finalizeDeviceOperation(HttpServletRequest request,
			HttpServletResponse response) throws DeviceFinalizationException {
		ProtocolContext protocolContext = ProtocolContext
				.getProtocolContext(request.getSession());
		String usedDevice = protocolContext.getWantedDevice();
		String userId = protocolContext.getSubject();
		String target = protocolContext.getTargetUrl();
		String issuerName = protocolContext.getIssuer();
		int validity = protocolContext.getValidity();
		boolean deviceOperationSuccess = protocolContext.getSuccess();
		DeviceOperationType deviceOperation = protocolContext
				.getDeviceOperation();
		String inResponseTo = protocolContext.getInResponseTo();
		if (null == inResponseTo) {
			throw new DeviceFinalizationException(
					"missing IN_RESPONSE_TO session attribute");
		}

		String samlResponseToken;
		if (!deviceOperationSuccess) {
			/*
			 * Device operation have failed
			 */
			samlResponseToken = AuthnResponseFactory.createAuthResponseFailed(
					inResponseTo, issuerName, this.applicationKeyPair, target);
		} else {
			/*
			 * Device operation was successful
			 */
			samlResponseToken = AuthnResponseFactory.createAuthResponse(
					inResponseTo, deviceOperation.name(), issuerName, userId,
					usedDevice, this.applicationKeyPair, validity, target);
		}

		String encodedSamlResponseToken = Base64.encode(samlResponseToken
				.getBytes());

		String templateResourceName = SAML2_POST_BINDING_VM_RESOURCE;

		try {
			AuthnResponseUtil.sendAuthnResponse(encodedSamlResponseToken,
					templateResourceName, target, response);
		} catch (ServletException e) {
			throw new DeviceFinalizationException(e.getMessage());
		} catch (IOException e) {
			throw new DeviceFinalizationException(e.getMessage());
		}

		// destroy the session to prevent reuse
		request.getSession().invalidate();
	}
}
