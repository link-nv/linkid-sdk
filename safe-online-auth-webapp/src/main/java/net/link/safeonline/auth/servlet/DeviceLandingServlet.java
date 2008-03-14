/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.auth.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Device landing servlet. Landing page to finalize the authentication process
 * between OLAS and a device provider.
 * 
 * @author wvdhaute
 * 
 */
public class DeviceLandingServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private static final Log LOG = LogFactory
			.getLog(DeviceLandingServlet.class);

	private DeviceDAO deviceDAO;

	private DeviceRegistrationService deviceRegistrationService;

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);

		loadDependencies();
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
			String msg = "no protocol handler active";
			LOG.error(msg);
			writeErrorPage(msg, response);
			return;
		}

		AuthIdentityServiceClient authIdentityServiceClient = new AuthIdentityServiceClient();

		String deviceUserId = saml2BrowserPostHandler.handleResponse(request,
				authIdentityServiceClient.getCertificate(),
				authIdentityServiceClient.getPrivateKey());
		if (null == deviceUserId) {
			String msg = "protocol handler could not finalize";
			LOG.error(msg);
			writeErrorPage(msg, response);
			return;
		}
		String deviceName = saml2BrowserPostHandler.getAuthenticationDevice();

		DeviceEntity device;
		try {
			device = this.deviceDAO.getDevice(deviceName);
		} catch (DeviceNotFoundException e) {
			String msg = "device not found: " + deviceName;
			LOG.error(msg);
			writeErrorPage(msg, response);
			return;
		}

		DeviceRegistrationEntity registeredDevice = this.deviceRegistrationService
				.getDeviceRegistration(deviceUserId);
		if (null == registeredDevice) {
			String msg = "device registration not found";
			LOG.error(msg + " : " + deviceUserId);
			writeErrorPage(msg, response);
			return;
		}

		/*
		 * Authenticate
		 */
		LOG.debug("login: " + registeredDevice.getSubject().getUserId()
				+ " device=" + device.getName());
		LoginManager.login(request.getSession(), registeredDevice.getSubject()
				.getUserId(), device);
		AuthenticationService authenticationService = AuthenticationServiceManager
				.getAuthenticationService(request.getSession());
		try {
			authenticationService.authenticate(registeredDevice.getSubject()
					.getUserId(), device);
		} catch (SubjectNotFoundException e) {
			String msg = "authentication failed for subject: "
					+ registeredDevice.getSubject().getUserId() + ", device: "
					+ device.getName();
			LOG.error(msg);
			writeErrorPage(msg, response);
			return;
		}
		response.sendRedirect("../login");
	}

	private void writeErrorPage(String message, HttpServletResponse response)
			throws IOException {
		response.setContentType("text/html");
		response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		PrintWriter out = response.getWriter();
		out.println("<html>");
		{
			out.println("<head><title>Error</title></head>");
			out.println("<body>");
			{
				out.println("<h1>Error</h1>");
				out.println("<p>");
				{
					out.println(message);
				}
				out.println("</p>");
			}
			out.println("</body>");
		}
		out.println("</html>");
		out.close();
	}
}
