/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.auth.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.link.safeonline.auth.LoginManager;
import net.link.safeonline.auth.protocol.AuthenticationServiceManager;
import net.link.safeonline.authentication.exception.DeviceMappingNotFoundException;
import net.link.safeonline.authentication.exception.NodeNotFoundException;
import net.link.safeonline.authentication.service.AuthenticationService;
import net.link.safeonline.entity.DeviceMappingEntity;
import net.link.safeonline.sdk.servlet.AbstractInjectionServlet;
import net.link.safeonline.sdk.servlet.ErrorMessage;
import net.link.safeonline.sdk.servlet.annotation.Init;

/**
 * Device landing servlet. Landing page to finalize the authentication process
 * between OLAS and a device provider.
 * 
 * @author wvdhaute
 * 
 */
public class DeviceLandingServlet extends AbstractInjectionServlet {

	private static final long serialVersionUID = 1L;

	public static final String RESOURCE_BASE = "messages.webapp";

	public static final String DEVICE_ERROR_MESSAGE_ATTRIBUTE = "deviceErrorMessage";

	@Init(name = "LoginUrl")
	private String loginUrl;

	@Init(name = "StartUrl")
	private String startUrl;

	@Init(name = "DeviceErrorUrl")
	private String deviceErrorUrl;

	@Override
	protected void invokePost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		/*
		 * Authenticate
		 */
		AuthenticationService authenticationService = AuthenticationServiceManager
				.getAuthenticationService(request.getSession());
		DeviceMappingEntity deviceMapping;
		try {
			deviceMapping = authenticationService.authenticate(request);
		} catch (NodeNotFoundException e) {
			redirectToErrorPage(request, response, this.deviceErrorUrl,
					RESOURCE_BASE, new ErrorMessage(
							DEVICE_ERROR_MESSAGE_ATTRIBUTE,
							"errorProtocolHandlerFinalization"));
			return;
		} catch (DeviceMappingNotFoundException e) {
			redirectToErrorPage(request, response, this.deviceErrorUrl,
					RESOURCE_BASE, new ErrorMessage(
							DEVICE_ERROR_MESSAGE_ATTRIBUTE,
							"errorDeviceRegistrationNotFound"));
			return;
		}
		if (null == deviceMapping) {
			/*
			 * Authentication failed, redirect to start page
			 */
			response.sendRedirect(this.startUrl);
		} else {
			/*
			 * Authentication success, redirect to login servlet
			 */
			LoginManager.login(request.getSession(), deviceMapping.getSubject()
					.getUserId(), deviceMapping.getDevice());

			response.sendRedirect(this.loginUrl);
		}
	}
}
