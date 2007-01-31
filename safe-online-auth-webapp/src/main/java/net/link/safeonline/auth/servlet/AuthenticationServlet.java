/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.auth.servlet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.link.safeonline.authentication.service.AuthenticationService;
import net.link.safeonline.util.ee.EjbUtils;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Authentication Servlet that accepts authentication statements from the
 * client-side browser applet.
 * 
 * @author fcorneli
 * 
 */
public class AuthenticationServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private static final Log LOG = LogFactory
			.getLog(AuthenticationServlet.class);

	private AuthenticationService authenticationService;

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);

		loadAuthenticationService();
	}

	private void loadAuthenticationService() {
		this.authenticationService = EjbUtils.getEJB(
				"SafeOnline/AuthenticationServiceBean/local",
				AuthenticationService.class);
	}

	@Override
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		LOG.debug("doPost");
		// TODO: factor out common code with identity servlet
		String contentType = request.getContentType();
		LOG.debug("content type: " + contentType);
		if (false == "application/octet-stream".equals(contentType)) {
			LOG.error("content-type should be application/octet-stream");
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}

		InputStream contentInputStream = request.getInputStream();
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		IOUtils.copy(contentInputStream, outputStream);
		byte[] authenticationStatementData = outputStream.toByteArray();

		String sessionId = request.getSession().getId();
		LOG.debug("session Id: " + sessionId);

		PrintWriter writer = response.getWriter();
		try {
			String userId = this.authenticationService.authenticate(sessionId,
					authenticationStatementData);
			HttpSession session = request.getSession();
			if (null != userId) {
				response.setStatus(HttpServletResponse.SC_OK);
				session.setAttribute("user", userId);
			} else {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				session.invalidate();
			}
		} catch (Exception e) {
			LOG.error("credential service error: " + e.getMessage(), e);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			writer.println("internal error");
		}
	}
}
