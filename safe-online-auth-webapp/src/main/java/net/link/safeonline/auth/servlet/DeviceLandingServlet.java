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

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.link.safeonline.auth.LoginManager;
import net.link.safeonline.authentication.exception.DeviceNotFoundException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.service.AuthenticationService;
import net.link.safeonline.dao.DeviceDAO;
import net.link.safeonline.device.sdk.auth.saml2.Saml2BrowserPostHandler;
import net.link.safeonline.entity.DeviceEntity;
import net.link.safeonline.entity.DeviceRegistrationEntity;
import net.link.safeonline.service.DeviceRegistrationService;
import net.link.safeonline.util.ee.AuthIdentityServiceClient;
import net.link.safeonline.util.ee.EjbUtils;

/**
 * Device landing servlet. Landing page to finalize the authentication process
 * between OLAS and a device provider.
 * 
 * @author wvdhaute
 * 
 */
public class DeviceLandingServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	public static final String RESOURCE_BASE = "messages.webapp";

	public static final String DEVICE_ERROR_URL = "DeviceErrorUrl";

	public static final String DEVICE_ERROR_MESSAGE_ATTRIBUTE = "deviceErrorMessage";

	private DeviceDAO deviceDAO;

	private DeviceRegistrationService deviceRegistrationService;

	private String deviceErrorUrl;

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		loadDependencies();
		this.deviceErrorUrl = getInitParameter(config, DEVICE_ERROR_URL);
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

	private void loadDependencies() {
		this.deviceDAO = EjbUtils.getEJB("SafeOnline/DeviceDAOBean/local",
				DeviceDAO.class);
		this.deviceRegistrationService = EjbUtils.getEJB(
				"SafeOnline/DeviceRegistrationServiceBean/local",
				DeviceRegistrationService.class);
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

	private void handleLanding(HttpServletRequest request,
			HttpServletResponse response) throws IOException, ServletException {
		Saml2BrowserPostHandler saml2BrowserPostHandler = Saml2BrowserPostHandler
				.getSaml2BrowserPostHandler(request);
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

		DeviceRegistrationEntity registeredDevice = this.deviceRegistrationService
				.getDeviceRegistration(deviceUserId);
		if (null == registeredDevice) {
			redirectToDeviceErrorPage(request, response,
					"errorDeviceRegistrationNotFound");
			return;
		}

		/*
		 * Authenticate
		 */
		LoginManager.login(request.getSession(), registeredDevice.getSubject()
				.getUserId(), device);
		AuthenticationService authenticationService = AuthenticationServiceManager
				.getAuthenticationService(request.getSession());
		try {
			authenticationService.authenticate(registeredDevice.getSubject()
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
