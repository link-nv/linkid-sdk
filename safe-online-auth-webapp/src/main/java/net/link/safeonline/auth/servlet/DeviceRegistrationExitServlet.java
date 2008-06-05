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

import net.link.safeonline.auth.LoginManager;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.DeviceNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.service.AuthenticationService;
import net.link.safeonline.authentication.service.ProxyAttributeService;
import net.link.safeonline.dao.DeviceDAO;
import net.link.safeonline.device.sdk.ProtocolContext;
import net.link.safeonline.entity.DeviceEntity;
import net.link.safeonline.entity.DeviceMappingEntity;
import net.link.safeonline.sdk.servlet.AbstractInjectionServlet;
import net.link.safeonline.sdk.servlet.annotation.Init;
import net.link.safeonline.service.DeviceMappingService;

/**
 * Device registration exit page.
 * 
 * This is the servlet to which to external device provider returns after
 * registration has finished. It redirects to the login servlet after first
 * polling if registration was successfull.
 * 
 * @author wvdhaute
 * 
 */
public class DeviceRegistrationExitServlet extends AbstractInjectionServlet {

	private static final long serialVersionUID = 1L;

	public static final String RESOURCE_BASE = "messages.webapp";

	public static final String DEVICE_ERROR_MESSAGE_ATTRIBUTE = "deviceErrorMessage";

	@EJB(mappedName = "SafeOnline/DeviceDAOBean/local")
	private DeviceDAO deviceDAO;

	@EJB(mappedName = "SafeOnline/DeviceMappingServiceBean/local")
	private DeviceMappingService deviceMappingService;

	@EJB(mappedName = "SafeOnline/ProxyAttributeServiceBean/local")
	private ProxyAttributeService proxyAttributeService;

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
			HttpServletResponse response) throws IOException {
		ProtocolContext protocolContext = ProtocolContext
				.getProtocolContext(request.getSession());
		DeviceEntity device;
		try {
			device = this.deviceDAO.getDevice(protocolContext.getDeviceName());
		} catch (DeviceNotFoundException e) {
			redirectToDeviceErrorPage(request, response, "errorDeviceNotFound");
			return;
		}
		DeviceMappingEntity deviceMapping = this.deviceMappingService
				.getDeviceMapping(protocolContext.getMappingId());
		if (null == deviceMapping) {
			redirectToDeviceErrorPage(request, response,
					"errorDeviceRegistrationNotFound");
			return;
		}

		// Poll the device issuer if registration actually was successful.
		Object deviceAttribute;
		try {
			deviceAttribute = this.proxyAttributeService
					.findDeviceAttributeValue(protocolContext.getMappingId(),
							device.getAttributeType().getName());
		} catch (PermissionDeniedException e) {
			redirectToDeviceErrorPage(request, response,
					"errorPermissionDenied");
			return;
		} catch (AttributeTypeNotFoundException e) {
			redirectToDeviceErrorPage(request, response,
					"errorAttributeTypeNotFound");
			return;
		}
		if (null == deviceAttribute) {
			redirectToDeviceErrorPage(request, response,
					"errorDeviceRegistrationNotFound");
			return;
		}

		LoginManager.relogin(request.getSession(), device);
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
