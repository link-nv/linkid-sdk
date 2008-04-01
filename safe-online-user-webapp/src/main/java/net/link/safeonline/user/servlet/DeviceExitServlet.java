/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.user.servlet;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.DeviceNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.service.ProxyAttributeService;
import net.link.safeonline.device.sdk.ProtocolContext;
import net.link.safeonline.entity.DeviceRegistrationEntity;
import net.link.safeonline.service.DeviceRegistrationService;
import net.link.safeonline.util.ee.EjbUtils;

/**
 * Device registration exit page.
 * 
 * This is the servlet to which to external device provider returns after
 * registration/update/removal has finished. It redirects back to the devices
 * page. Before redirection, it polls the device issuer to update if needed the
 * registered devices.
 * 
 * @author wvdhaute
 * 
 */
public class DeviceExitServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	public static final String RESOURCE_BASE = "messages.webapp";

	public static final String DEVICE_ERROR_URL = "DeviceErrorUrl";

	public static final String DEVICE_ERROR_MESSAGE_ATTRIBUTE = "deviceErrorMessage";

	private String deviceErrorUrl;

	private DeviceRegistrationService deviceRegistrationService;

	private ProxyAttributeService proxyAttributeService;

	@SuppressWarnings("unchecked")
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		loadDependencies();
		this.deviceErrorUrl = getInitParameter(config, DEVICE_ERROR_URL);
	}

	private void loadDependencies() {
		this.deviceRegistrationService = EjbUtils.getEJB(
				"SafeOnline/DeviceRegistrationServiceBean/local",
				DeviceRegistrationService.class);
		this.proxyAttributeService = EjbUtils.getEJB(
				"SafeOnline/ProxyAttributeServiceBean/local",
				ProxyAttributeService.class);
	}

	public String getInitParameter(ServletConfig config,
			String initParameterName) throws UnavailableException {
		String paramValue = config.getInitParameter(initParameterName);
		if (null == paramValue) {
			throw new UnavailableException("missing init parameter: "
					+ initParameterName);
		}
		return paramValue;
	}

	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		handleLanding(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		handleLanding(request, response);
	}

	private void handleLanding(@SuppressWarnings("unused")
	HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		try {
			updateRegisteredDevices(request);
		} catch (SubjectNotFoundException e) {
			redirectToDeviceErrorPage(request, response, "errorSubjectNotFound");
			return;
		} catch (AttributeTypeNotFoundException e) {
			redirectToDeviceErrorPage(request, response,
					"errorAttributeTypeNotFound");
			return;
		} catch (PermissionDeniedException e) {
			redirectToDeviceErrorPage(request, response,
					"errorPermissionDenied");
			return;
		} catch (DeviceNotFoundException e) {
			redirectToDeviceErrorPage(request, response, "errorDeviceNotFound");
			return;
		}
		response.sendRedirect("./devices.seam");
	}

	/**
	 * Polls the current device issuer for an update of the device
	 * registrations.
	 * 
	 * @param request
	 * @throws AttributeTypeNotFoundException
	 * @throws PermissionDeniedException
	 * @throws SubjectNotFoundException
	 * @throws DeviceNotFoundException
	 */
	private void updateRegisteredDevices(HttpServletRequest request)
			throws AttributeTypeNotFoundException, PermissionDeniedException,
			SubjectNotFoundException, DeviceNotFoundException {
		ProtocolContext protocolContext = ProtocolContext
				.getProtocolContext(request.getSession());
		String deviceName = protocolContext.getDeviceName();
		String userId = (String) request.getSession().getAttribute("username");
		String registrationId = null;
		if (null != request.getSession().getAttribute("registrationId"))
			registrationId = (String) request.getSession().getAttribute(
					"registrationId");
		if (null != registrationId) {
			DeviceRegistrationEntity deviceRegistration = this.deviceRegistrationService
					.getDeviceRegistration(registrationId);
			if (null == pollDeviceRegistration(deviceRegistration))
				this.deviceRegistrationService
						.removeDeviceRegistration(registrationId);
		} else {
			List<DeviceRegistrationEntity> deviceRegistrations = this.deviceRegistrationService
					.listDeviceRegistrations(userId, deviceName);
			for (DeviceRegistrationEntity deviceRegistration : deviceRegistrations) {
				if (null == pollDeviceRegistration(deviceRegistration))
					this.deviceRegistrationService
							.removeDeviceRegistration(deviceRegistration
									.getId());
			}
		}
	}

	private Object pollDeviceRegistration(
			DeviceRegistrationEntity deviceRegistration)
			throws AttributeTypeNotFoundException, PermissionDeniedException {
		return this.proxyAttributeService.findDeviceAttributeValue(
				deviceRegistration.getId(), deviceRegistration.getDevice()
						.getAttributeType().getName());
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
