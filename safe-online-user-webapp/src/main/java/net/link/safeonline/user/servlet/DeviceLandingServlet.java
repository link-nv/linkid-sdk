/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.user.servlet;

import java.io.IOException;
import java.security.KeyPair;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.link.safeonline.authentication.exception.DeviceNotFoundException;
import net.link.safeonline.authentication.exception.NodeNotFoundException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.service.NodeAuthenticationService;
import net.link.safeonline.authentication.service.SamlAuthorityService;
import net.link.safeonline.device.sdk.ErrorPage;
import net.link.safeonline.device.sdk.ProtocolContext;
import net.link.safeonline.device.sdk.exception.DeviceFinalizationException;
import net.link.safeonline.device.sdk.exception.DeviceInitializationException;
import net.link.safeonline.device.sdk.reg.saml2.Saml2Handler;
import net.link.safeonline.entity.DeviceMappingEntity;
import net.link.safeonline.entity.OlasEntity;
import net.link.safeonline.sdk.servlet.AbstractInjectionServlet;
import net.link.safeonline.service.DeviceMappingService;
import net.link.safeonline.util.ee.AuthIdentityServiceClient;
import net.link.safeonline.util.ee.IdentityServiceClient;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Device registration landing page.
 * 
 * This landing servlet handles the SAML requests sent out by an external device
 * provider, and sends back a response containing the UUID for the registrating
 * OLAS subject for this device. This landing is used for registration, updating
 * and removal.
 * 
 * @author wvdhaute
 * 
 */
public class DeviceLandingServlet extends AbstractInjectionServlet {

	private static final long serialVersionUID = 1L;

	private static final Log LOG = LogFactory
			.getLog(DeviceLandingServlet.class);

	@EJB(mappedName = "SafeOnline/DeviceMappingServiceBean/local")
	private DeviceMappingService deviceMappingService;

	@EJB(mappedName = "SafeOnline/SamlAuthorityServiceBean/local")
	private SamlAuthorityService samlAuthorityService;

	@EJB(mappedName = "SafeOnline/NodeAuthenticationServiceBean/local")
	private NodeAuthenticationService nodeAuthenticationService;

	@Override
	protected void invokePost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		LOG.debug("doPost");
		Saml2Handler handler = Saml2Handler.getSaml2Handler(request);
		IdentityServiceClient identityServiceClient = new IdentityServiceClient();
		KeyPair keyPair = new KeyPair(identityServiceClient.getPublicKey(),
				identityServiceClient.getPrivateKey());
		AuthIdentityServiceClient authIdentityServiceClient = new AuthIdentityServiceClient();
		KeyPair authKeyPair = new KeyPair(authIdentityServiceClient
				.getPublicKey(), authIdentityServiceClient.getPrivateKey());
		OlasEntity node;
		try {
			node = this.nodeAuthenticationService.getLocalNode();
		} catch (NodeNotFoundException e) {
			ErrorPage.errorPage(e.getMessage(), response);
			return;
		}

		handler.init(this.configParams, keyPair);

		try {
			LOG.debug("initialize registration");
			handler.initialize(request, authIdentityServiceClient
					.getCertificate(), authKeyPair);
		} catch (DeviceInitializationException e) {
			ErrorPage.errorPage(e.getMessage(), response);
			return;
		}

		ProtocolContext protocolContext = ProtocolContext
				.getProtocolContext(request.getSession());
		String deviceName = protocolContext.getDeviceName();
		String userId = (String) request.getSession().getAttribute("username");
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
			protocolContext.setIssuer(node.getName());
		} catch (SubjectNotFoundException e) {
			ErrorPage.errorPage(e.getMessage(), response);
			return;
		} catch (DeviceNotFoundException e) {
			ErrorPage.errorPage(e.getMessage(), response);
			return;
		}

		try {
			handler.finalize(request, response);
		} catch (DeviceFinalizationException e) {
			ErrorPage.errorPage(e.getMessage(), response);
			return;
		}
	}
}
