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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.exception.ArgumentIntegrityException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.exception.SubscriptionNotFoundException;
import net.link.safeonline.authentication.exception.TrustDomainNotFoundException;
import net.link.safeonline.authentication.service.AuthenticationService;
import net.link.safeonline.shared.SharedConstants;

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

		HttpSession session = request.getSession();
		String applicationId = (String) session.getAttribute("applicationId");
		if (null == applicationId) {
			throw new ServletException(
					"applicationId session attribute not found");
		}

		String sessionId = session.getId();
		LOG.debug("session Id: " + sessionId);

		AuthenticationService authenticationService = AuthenticationServiceManager
				.getAuthenticationService(session);

		PrintWriter writer = response.getWriter();
		try {
			boolean result = authenticationService.authenticate(sessionId,
					authenticationStatementData);
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
			// TODO: commit later on
			AuthenticationServiceManager.commitAuthentication(session,
					applicationId);
			response.setStatus(HttpServletResponse.SC_OK);
			/*
			 * Next session attribute is used to communicate the authentication
			 * event to the redirect servlet.
			 */
			session.setAttribute("username", userId);
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
		} catch (SubscriptionNotFoundException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.setHeader(SharedConstants.SAFE_ONLINE_ERROR_HTTP_HEADER, e
					.getErrorCode());
			writer.println("Subscription not found");
		} catch (ArgumentIntegrityException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.setHeader(SharedConstants.SAFE_ONLINE_ERROR_HTTP_HEADER, e
					.getErrorCode());
			writer.println("Argument integrity error");
		} catch (ApplicationNotFoundException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.setHeader(SharedConstants.SAFE_ONLINE_ERROR_HTTP_HEADER, e
					.getErrorCode());
			writer.println("Application not found");
		} catch (Exception e) {
			LOG.error("credential service error: " + e.getMessage(), e);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			writer.println("internal error");
		}
	}
}
