/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.auth.servlet;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.link.safeonline.auth.LoginManager;
import net.link.safeonline.auth.protocol.AuthenticationServiceManager;
import net.link.safeonline.authentication.exception.DeviceMappingNotFoundException;
import net.link.safeonline.authentication.exception.NodeNotFoundException;
import net.link.safeonline.authentication.service.AuthenticationService;
import net.link.safeonline.entity.DeviceMappingEntity;
import net.link.safeonline.sdk.servlet.AbstractInjectionServlet;
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

	@Init(name = "DeviceErrorUrl")
	private String deviceErrorUrl;

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
			redirectToDeviceErrorPage(request, response,
					"errorProtocolHandlerFinalization");
			return;
		} catch (DeviceMappingNotFoundException e) {
			redirectToDeviceErrorPage(request, response,
					"errorDeviceRegistrationNotFound");
			return;
		}
		LoginManager.login(request.getSession(), deviceMapping.getSubject()
				.getUserId(), deviceMapping.getDevice());

		response.sendRedirect("../login");
	}

	private void redirectToDeviceErrorPage(HttpServletRequest request,
			HttpServletResponse response, String errorMessage)
			throws IOException {
		HttpSession session = request.getSession();
		Locale locale = request.getLocale();
		ResourceBundle resourceBundle = ResourceBundle.getBundle(RESOURCE_BASE,
				locale);
		String errorMessageString = resourceBundle.getString(errorMessage);
		session
				.setAttribute(DEVICE_ERROR_MESSAGE_ATTRIBUTE,
						errorMessageString);
		response.sendRedirect(this.deviceErrorUrl);
	}
}
