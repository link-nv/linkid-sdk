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

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.link.safeonline.annotation.Init;
import net.link.safeonline.auth.LoginManager;
import net.link.safeonline.authentication.exception.DeviceNotFoundException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.service.AuthenticationService;
import net.link.safeonline.dao.DeviceDAO;
import net.link.safeonline.device.sdk.auth.saml2.Saml2BrowserPostHandler;
import net.link.safeonline.entity.DeviceEntity;
import net.link.safeonline.entity.DeviceMappingEntity;
import net.link.safeonline.service.DeviceMappingService;
import net.link.safeonline.servlet.AbstractInjectionServlet;
import net.link.safeonline.util.ee.AuthIdentityServiceClient;

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

	@EJB(mappedName = "SafeOnline/DeviceDAOBean/local")
	private DeviceDAO deviceDAO;

	@EJB(mappedName = "SafeOnline/DeviceMappingServiceBean/local")
	private DeviceMappingService deviceMappingService;

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
		Saml2BrowserPostHandler saml2BrowserPostHandler = Saml2BrowserPostHandler
				.findSaml2BrowserPostHandler(request);
		if (null == saml2BrowserPostHandler) {
			/*
			 * The landing page can only be used for finalizing an ongoing
			 * authentication process. If no protocol handler is active then
			 * something must be going wrong here.
			 */
			redirectToDeviceErrorPage(request, response,
					"errorNoProtocolHandlerActive");
			return;
		}

		AuthIdentityServiceClient authIdentityServiceClient = new AuthIdentityServiceClient();

		String deviceUserId = saml2BrowserPostHandler.handleResponse(request,
				authIdentityServiceClient.getCertificate(),
				authIdentityServiceClient.getPrivateKey());
		if (null == deviceUserId) {
			redirectToDeviceErrorPage(request, response,
					"errorProtocolHandlerFinalization");
			return;
		}
		String deviceName = saml2BrowserPostHandler.getAuthenticationDevice();

		DeviceEntity device;
		try {
			device = this.deviceDAO.getDevice(deviceName);
		} catch (DeviceNotFoundException e) {
			redirectToDeviceErrorPage(request, response, "errorDeviceNotFound");
			return;
		}

		DeviceMappingEntity deviceMapping = this.deviceMappingService
				.getDeviceMapping(deviceUserId);
		if (null == deviceMapping) {
			redirectToDeviceErrorPage(request, response,
					"errorDeviceRegistrationNotFound");
			return;
		}

		/*
		 * Authenticate
		 */
		LoginManager.login(request.getSession(), deviceMapping.getSubject()
				.getUserId(), device);
		AuthenticationService authenticationService = AuthenticationServiceManager
				.getAuthenticationService(request.getSession());
		try {
			authenticationService.authenticate(deviceMapping.getSubject()
					.getUserId(), device);
		} catch (SubjectNotFoundException e) {
			redirectToDeviceErrorPage(request, response, "errorSubjectNotFound");
			return;
		}
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
