/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.auth.servlet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.link.safeonline.authentication.exception.ArgumentIntegrityException;
import net.link.safeonline.authentication.exception.DecodingException;
import net.link.safeonline.authentication.exception.ExistingUserException;
import net.link.safeonline.authentication.service.AuthenticationService;
import net.link.safeonline.pkix.exception.TrustDomainNotFoundException;
import net.link.safeonline.shared.SharedConstants;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The identity servlet implementation. This servlet receives its data from the
 * BeID via the IdentityApplet. Via this data web will register the user within
 * SafeOnline.
 * 
 * @author fcorneli
 * 
 */
public class RegistrationServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private static final Log LOG = LogFactory.getLog(RegistrationServlet.class);

	@Override
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		LOG.debug("doPost");
		String contentType = request.getContentType();
		if (false == "application/octet-stream".equals(contentType)) {
			LOG.error("content-type should be application/octet-stream");
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}

		InputStream contentInputStream = request.getInputStream();
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		IOUtils.copy(contentInputStream, outputStream);
		byte[] registrationStatementData = outputStream.toByteArray();

		HttpSession session = request.getSession();
		String sessionId = session.getId();

		String requestedUsername = (String) session
				.getAttribute("requestedUsername");

		AuthenticationService authenticationService = AuthenticationServiceManager
				.getAuthenticationService(session);
		try {
			authenticationService.registerAndAuthenticate(sessionId,
					requestedUsername, registrationStatementData);
			String userId = authenticationService.getUserId();
			response.setStatus(HttpServletResponse.SC_OK);
			/*
			 * Next session attribute is used to communicate the authentication
			 * event to the redirect servlet.
			 */
			session.setAttribute("username", userId);
		} catch (TrustDomainNotFoundException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.setHeader(SharedConstants.SAFE_ONLINE_ERROR_HTTP_HEADER, e
					.getErrorCode());
		} catch (ExistingUserException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		} catch (ArgumentIntegrityException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		} catch (DecodingException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		}
	}
}
