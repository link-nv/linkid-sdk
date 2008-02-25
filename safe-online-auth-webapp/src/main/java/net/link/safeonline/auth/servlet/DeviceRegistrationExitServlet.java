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
import net.link.safeonline.device.sdk.ProtocolContext;
import net.link.safeonline.entity.DeviceEntity;
import net.link.safeonline.entity.RegisteredDeviceEntity;
import net.link.safeonline.service.RegisteredDeviceService;
import net.link.safeonline.util.ee.EjbUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Device registration exit page.
 * 
 * This is the servlet to which to external device provider returns after
 * registration has finished. It redirects to the login servlet.
 * 
 * @author wvdhaute
 * 
 */
public class DeviceRegistrationExitServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private static final Log LOG = LogFactory
			.getLog(DeviceRegistrationExitServlet.class);

	private DeviceDAO deviceDAO;

	private RegisteredDeviceService registeredDeviceService;

	@SuppressWarnings("unchecked")
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		loadDependencies();
	}

	private void loadDependencies() {
		this.deviceDAO = EjbUtils.getEJB("SafeOnline/DeviceDAOBean/local",
				DeviceDAO.class);
		this.registeredDeviceService = EjbUtils.getEJB(
				"SafeOnline/RegisteredDeviceServiceBean/local",
				RegisteredDeviceService.class);
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
			HttpServletResponse response) throws IOException {
		ProtocolContext protocolContext = ProtocolContext
				.getProtocolContext(request.getSession());
		DeviceEntity device;
		try {
			device = this.deviceDAO.getDevice(protocolContext
					.getRegisteredDevice());
		} catch (DeviceNotFoundException e) {
			String msg = "device not found: "
					+ protocolContext.getRegisteredDevice();
			LOG.error(msg);
			writeErrorPage(msg, response);
			return;
		}
		RegisteredDeviceEntity registeredDevice = this.registeredDeviceService
				.getRegisteredDevice(protocolContext.getUserId());
		if (null == registeredDevice) {
			String msg = "device registration not found";
			LOG.error(msg);
			writeErrorPage(msg, response);
			return;
		}

		LoginManager.relogin(request.getSession(), device);
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
