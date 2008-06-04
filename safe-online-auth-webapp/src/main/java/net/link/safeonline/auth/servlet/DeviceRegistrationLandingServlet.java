/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.auth.servlet;

import java.io.IOException;
import java.security.KeyPair;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.link.safeonline.auth.LoginManager;
import net.link.safeonline.authentication.exception.DeviceNotFoundException;
import net.link.safeonline.authentication.exception.NodeNotFoundException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.service.NodeAuthenticationService;
import net.link.safeonline.authentication.service.SamlAuthorityService;
import net.link.safeonline.device.sdk.ErrorPage;
import net.link.safeonline.device.sdk.ProtocolContext;
import net.link.safeonline.device.sdk.exception.RegistrationFinalizationException;
import net.link.safeonline.device.sdk.exception.RegistrationInitializationException;
import net.link.safeonline.device.sdk.reg.saml2.Saml2Handler;
import net.link.safeonline.entity.DeviceMappingEntity;
import net.link.safeonline.service.DeviceMappingService;
import net.link.safeonline.servlet.AbstractInjectionServlet;
import net.link.safeonline.util.ee.AuthIdentityServiceClient;
import net.link.safeonline.util.ee.IdentityServiceClient;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Device registration landing page.
 * 
 * This landing page handles the SAML requests sent out by an external device
 * provider, and sends back a response containing the UUID for the registering
 * subject.
 * 
 * @author wvdhaute
 * 
 */
public class DeviceRegistrationLandingServlet extends AbstractInjectionServlet {

	private static final long serialVersionUID = 1L;

	private static final Log LOG = LogFactory
			.getLog(DeviceRegistrationLandingServlet.class);

	@EJB(mappedName = "SafeOnline/DeviceMappingServiceBean/local")
	private DeviceMappingService deviceMappingService;

	@EJB(mappedName = "SafeOnline/SamlAuthorityServiceBean/local")
	private SamlAuthorityService samlAuthorityService;

	@EJB(mappedName = "SafeOnline/NodeAuthenticationServiceBean/local")
	private NodeAuthenticationService nodeAuthenticationService;

	@Override
	protected void invokeGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		handleLanding(request, response);
	}

	@Override
	protected void invokePost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		handleLanding(request, response);
	}

	private void handleLanding(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		Saml2Handler handler = Saml2Handler.getSaml2Handler(request);
		IdentityServiceClient identityServiceClient = new IdentityServiceClient();
		KeyPair keyPair = new KeyPair(identityServiceClient.getPublicKey(),
				identityServiceClient.getPrivateKey());
		AuthIdentityServiceClient authIdentityServiceClient = new AuthIdentityServiceClient();
		KeyPair authKeyPair = new KeyPair(authIdentityServiceClient
				.getPublicKey(), authIdentityServiceClient.getPrivateKey());
		String nodeName;
		try {
			nodeName = this.nodeAuthenticationService
					.authenticate(authIdentityServiceClient.getCertificate());
		} catch (NodeNotFoundException e) {
			ErrorPage.errorPage(e.getMessage(), response);
			return;
		}

		handler.init(this.configParams, keyPair);

		try {
			handler.initialize(request, authIdentityServiceClient
					.getCertificate(), authKeyPair);
		} catch (RegistrationInitializationException e) {
			ErrorPage.errorPage(e.getMessage(), response);
			return;
		}

		ProtocolContext protocolContext = ProtocolContext
				.getProtocolContext(request.getSession());
		String deviceName = protocolContext.getDeviceName();
		String userId = LoginManager.getUsername(request.getSession());
		try {
			LOG
					.debug("get device mapping for " + deviceName + " for "
							+ userId);
			DeviceMappingEntity deviceMapping = this.deviceMappingService
					.getDeviceMapping(userId, deviceName);
			LOG.debug("device mapping id: " + deviceMapping.getId());

			protocolContext.setMappingId(deviceMapping.getId());
			protocolContext.setValidity(this.samlAuthorityService
					.getAuthnAssertionValidity());
			protocolContext.setIssuer(nodeName);
		} catch (SubjectNotFoundException e) {
			ErrorPage.errorPage(e.getMessage(), response);
			return;
		} catch (DeviceNotFoundException e) {
			ErrorPage.errorPage(e.getMessage(), response);
			return;
		}

		try {
			handler.finalize(request, response);
		} catch (RegistrationFinalizationException e) {
			ErrorPage.errorPage(e.getMessage(), response);
			return;
		}
	}
}
