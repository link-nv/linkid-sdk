/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.user.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Device registration exit page.
 * 
 * This is the servlet to which to external device provider returns after
 * registration/update/removal has finished. It redirects back to the devices
 * page.
 * 
 * @author wvdhaute
 * 
 */
public class DeviceExitServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private static final Log LOG = LogFactory.getLog(DeviceExitServlet.class);

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

	private void handleLanding(
			@SuppressWarnings("unused") HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		LOG.debug("handle landing");
		response.sendRedirect("./devices.seam");
	}
}
