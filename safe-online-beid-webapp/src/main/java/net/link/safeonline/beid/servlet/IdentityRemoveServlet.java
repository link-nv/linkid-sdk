/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.beid.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.link.safeonline.authentication.exception.ArgumentIntegrityException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.service.SamlAuthorityService;
import net.link.safeonline.device.sdk.ProtocolContext;
import net.link.safeonline.model.beid.BeIdDeviceService;
import net.link.safeonline.pkix.exception.TrustDomainNotFoundException;
import net.link.safeonline.servlet.AbstractStatementServlet;
import net.link.safeonline.shared.SharedConstants;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The identity servlet implementation. This servlet receives its data from the
 * BeID via the IdentityApplet.
 * 
 * @author wvdhaute
 * 
 */
public class IdentityRemoveServlet extends AbstractStatementServlet {

	private static final long serialVersionUID = 1L;

	private static final Log LOG = LogFactory
			.getLog(IdentityRemoveServlet.class);

	@EJB(mappedName = "SafeOnlineBeid/BeIdDeviceServiceBean/local")
	private BeIdDeviceService beIdDeviceService;

	@EJB(mappedName = "SafeOnline/SamlAuthorityServiceBean/local")
	private SamlAuthorityService samlAuthorityService;

	@Override
	protected void processStatement(byte[] statementData, HttpSession session,
			HttpServletResponse response) throws ServletException, IOException {
		String sessionId = session.getId();
		LOG.debug("session Id: " + sessionId);

		PrintWriter writer = response.getWriter();
		ProtocolContext protocolContext = ProtocolContext
				.getProtocolContext(session);
		try {
			protocolContext.setValidity(this.samlAuthorityService
					.getAuthnAssertionValidity());
			String userId = (String) session.getAttribute("userId");
			String operation = (String) session.getAttribute("operation");
			this.beIdDeviceService.remove(sessionId, userId, operation,
					statementData);
			response.setStatus(HttpServletResponse.SC_OK);
			protocolContext.setSuccess(true);
		} catch (TrustDomainNotFoundException e) {
			LOG.error("trust domain not found: " + e.getMessage(), e);
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.setHeader(SharedConstants.SAFE_ONLINE_ERROR_HTTP_HEADER, e
					.getErrorCode());
			writer.println("trust domain not found");
			protocolContext.setSuccess(false);
		} catch (PermissionDeniedException e) {
			LOG.error("permission denied: " + e.getMessage(), e);
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.setHeader(SharedConstants.SAFE_ONLINE_ERROR_HTTP_HEADER, e
					.getErrorCode());
			writer.println("permission denied");
			protocolContext.setSuccess(false);
		} catch (ArgumentIntegrityException e) {
			LOG.error("integrity error: " + e.getMessage(), e);
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.setHeader(SharedConstants.SAFE_ONLINE_ERROR_HTTP_HEADER, e
					.getErrorCode());
			writer.println("integrity check failed");
			protocolContext.setSuccess(false);
		} catch (Exception e) {
			LOG.error("credential service error: " + e.getMessage(), e);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			writer.println("internal error");

		}
	}

}
