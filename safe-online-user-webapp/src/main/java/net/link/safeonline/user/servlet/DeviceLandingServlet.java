/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.user.servlet;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.link.safeonline.authentication.exception.DeviceMappingNotFoundException;
import net.link.safeonline.authentication.exception.NodeNotFoundException;
import net.link.safeonline.authentication.service.DeviceOperationService;
import net.link.safeonline.sdk.servlet.AbstractInjectionServlet;
import net.link.safeonline.sdk.servlet.annotation.Init;

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

	public static final String RESOURCE_BASE = "messages.webapp";

	public static final String DEVICE_ERROR_MESSAGE_ATTRIBUTE = "deviceErrorMessage";

	@Init(name = "DeviceErrorUrl")
	private String deviceErrorUrl;

	@Override
	protected void invokePost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		LOG.debug("doPost");

		DeviceOperationService deviceOperationService = (DeviceOperationService) request
				.getSession()
				.getAttribute(
						DeviceOperationService.DEVICE_OPERATION_SERVICE_ATTRIBUTE);
		if (null == deviceOperationService) {
			redirectToDeviceErrorPage(request, response,
					"errorProtocolHandlerFinalization");
			return;
		}

		try {
			deviceOperationService.finalize(request);
		} catch (NodeNotFoundException e) {
			redirectToDeviceErrorPage(request, response,
					"errorProtocolHandlerFinalization");
			return;
		} catch (DeviceMappingNotFoundException e) {
			redirectToDeviceErrorPage(request, response,
					"errorDeviceRegistrationNotFound");
			return;
		}

		// remove the device operation service from the HttpSession
		request.getSession().removeAttribute(
				DeviceOperationService.DEVICE_OPERATION_SERVICE_ATTRIBUTE);

		response.sendRedirect("./devices.seam");
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
