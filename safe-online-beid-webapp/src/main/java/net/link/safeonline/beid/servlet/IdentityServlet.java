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
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.link.safeonline.authentication.exception.AlreadyRegisteredException;
import net.link.safeonline.authentication.exception.ArgumentIntegrityException;
import net.link.safeonline.authentication.exception.AttributeNotFoundException;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.DeviceNotFoundException;
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
 * @author fcorneli
 * 
 */
public class IdentityServlet extends AbstractStatementServlet {

	private static final long serialVersionUID = 1L;

	private static final Log LOG = LogFactory.getLog(IdentityServlet.class);

	@EJB(mappedName = "SafeOnlineBeid/BeIdDeviceServiceBean/local")
	private BeIdDeviceService beIdDeviceService;

	@EJB(mappedName = "SafeOnline/SamlAuthorityServiceBean/local")
	private SamlAuthorityService samlAuthorityService;

	@Override
	protected void processStatement(byte[] statementData, HttpSession session,
			HttpServletResponse response) throws IOException {
		String sessionId = session.getId();
		LOG.debug("session Id: " + sessionId);

		ProtocolContext protocolContext = ProtocolContext
				.getProtocolContext(session);
		PrintWriter writer = response.getWriter();
		try {
			protocolContext.setValidity(this.samlAuthorityService
					.getAuthnAssertionValidity());

			String userId = (String) session.getAttribute("userId");
			String operation = (String) session.getAttribute("operation");
			this.beIdDeviceService.register(sessionId, userId, operation,
					statementData);
			response.setStatus(HttpServletResponse.SC_OK);
			// notify that registration was successful.
			protocolContext.setSuccess(true);
		} catch (TrustDomainNotFoundException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.setHeader(SharedConstants.SAFE_ONLINE_ERROR_HTTP_HEADER, e
					.getErrorCode());
			writer.println("Trust domain not found");
			protocolContext.setSuccess(false);
		} catch (PermissionDeniedException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.setHeader(SharedConstants.SAFE_ONLINE_ERROR_HTTP_HEADER, e
					.getErrorCode());
			writer.println("Permission denied error");
			protocolContext.setSuccess(false);
		} catch (ArgumentIntegrityException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.setHeader(SharedConstants.SAFE_ONLINE_ERROR_HTTP_HEADER, e
					.getErrorCode());
			writer.println("Argument integrity error");
			protocolContext.setSuccess(false);
		} catch (AttributeTypeNotFoundException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.setHeader(SharedConstants.SAFE_ONLINE_ERROR_HTTP_HEADER, e
					.getErrorCode());
			writer.println("Attribute type not found error");
			protocolContext.setSuccess(false);
		} catch (DeviceNotFoundException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.setHeader(SharedConstants.SAFE_ONLINE_ERROR_HTTP_HEADER, e
					.getErrorCode());
			writer.println("Device not found error");
			protocolContext.setSuccess(false);
		} catch (AttributeNotFoundException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.setHeader(SharedConstants.SAFE_ONLINE_ERROR_HTTP_HEADER, e
					.getErrorCode());
			protocolContext.setSuccess(false);
			writer.println("Attribute not found error");
		} catch (AlreadyRegisteredException e) {
			LOG.debug("device already registered");
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.setHeader(SharedConstants.SAFE_ONLINE_ERROR_HTTP_HEADER, e
					.getErrorCode());
			protocolContext.setSuccess(false);
			writer.println("Already registered error");
		}
	}
}
