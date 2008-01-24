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
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.auth.LoginManager;
import net.link.safeonline.authentication.exception.ArgumentIntegrityException;
import net.link.safeonline.authentication.exception.DecodingException;
import net.link.safeonline.authentication.exception.DeviceNotFoundException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.service.AuthenticationService;
import net.link.safeonline.dao.DeviceDAO;
import net.link.safeonline.entity.DeviceEntity;
import net.link.safeonline.pkix.exception.TrustDomainNotFoundException;
import net.link.safeonline.servlet.AbstractStatementServlet;
import net.link.safeonline.shared.SharedConstants;
import net.link.safeonline.util.ee.EjbUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Authentication Servlet that accepts authentication statements from the
 * client-side browser applet.
 * 
 * @author fcorneli
 * 
 */
public class AuthenticationServlet extends AbstractStatementServlet {

	private static final long serialVersionUID = 1L;

	private static final Log LOG = LogFactory
			.getLog(AuthenticationServlet.class);

	private DeviceDAO deviceDAO;

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		this.deviceDAO = EjbUtils.getEJB("SafeOnline/DeviceDAOBean/local",
				DeviceDAO.class);
	}

	@Override
	protected void processStatement(byte[] statementData, HttpSession session,
			HttpServletResponse response) throws ServletException, IOException {
		AuthenticationService authenticationService = AuthenticationServiceManager
				.getAuthenticationService(session);

		String applicationId = LoginManager.findApplication(session);
		if (null == applicationId) {
			throw new ServletException(
					"applicationId session attribute not found");
		}

		String sessionId = session.getId();
		LOG.debug("session Id: " + sessionId);

		PrintWriter writer = response.getWriter();
		try {
			boolean result = authenticationService.authenticate(sessionId,
					statementData);
			if (result == false) {
				/*
				 * Abort will be handled by the authentication service manager.
				 * That way we allow the user to retry the initial
				 * authentication step.
				 */
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				return;
			}
			String userId = authenticationService.getUserId();
			response.setStatus(HttpServletResponse.SC_OK);
			DeviceEntity beidDevice = this.deviceDAO
					.getDevice(SafeOnlineConstants.BEID_DEVICE_ID);
			LoginManager.login(session, userId, beidDevice);
		} catch (TrustDomainNotFoundException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			/*
			 * The status is used to mark success or error.
			 */
			response.setHeader(SharedConstants.SAFE_ONLINE_ERROR_HTTP_HEADER, e
					.getErrorCode());
			/*
			 * The error http header is used to allow machine processing of the
			 * error at the client side.
			 */
			writer.println("Trust domain not found");
			/*
			 * The error message is meant for human consumption.
			 */
		} catch (SubjectNotFoundException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.setHeader(SharedConstants.SAFE_ONLINE_ERROR_HTTP_HEADER, e
					.getErrorCode());
			writer.println("Subject not found");
		} catch (ArgumentIntegrityException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.setHeader(SharedConstants.SAFE_ONLINE_ERROR_HTTP_HEADER, e
					.getErrorCode());
			writer.println("Argument integrity error");
		} catch (DecodingException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.setHeader(SharedConstants.SAFE_ONLINE_ERROR_HTTP_HEADER, e
					.getErrorCode());
			writer.println("decoding error");
		} catch (DeviceNotFoundException e) {
			LOG.error("device not found: " + e.getMessage(), e);
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.setHeader(SharedConstants.SAFE_ONLINE_ERROR_HTTP_HEADER, e
					.getErrorCode());
			writer.println("device not found");
		} catch (Exception e) {
			LOG.error("credential service error: " + e.getMessage(), e);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			writer.println("internal error");
		}
	}
}
