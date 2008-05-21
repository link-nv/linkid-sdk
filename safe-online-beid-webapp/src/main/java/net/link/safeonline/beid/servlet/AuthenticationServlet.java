/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.beid.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.link.safeonline.authentication.exception.ArgumentIntegrityException;
import net.link.safeonline.authentication.exception.DecodingException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.service.SamlAuthorityService;
import net.link.safeonline.authentication.service.bean.AuthenticationStatement;
import net.link.safeonline.device.sdk.AuthenticationContext;
import net.link.safeonline.model.beid.BeIdConstants;
import net.link.safeonline.model.beid.BeIdDeviceService;
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

	private BeIdDeviceService beIdDeviceService;

	private SamlAuthorityService samlAuthorityService;

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		loadDependencies();
	}

	private void loadDependencies() {
		this.beIdDeviceService = EjbUtils.getEJB(
				"SafeOnline/BeIdDeviceServiceBean/local",
				BeIdDeviceService.class);
		this.samlAuthorityService = EjbUtils.getEJB(
				"SafeOnline/SamlAuthorityServiceBean/local",
				SamlAuthorityService.class);
	}

	@Override
	protected void processStatement(byte[] statementData, HttpSession session,
			HttpServletResponse response) throws ServletException, IOException {
		String sessionId = session.getId();
		LOG.debug("session Id: " + sessionId);

		PrintWriter writer = response.getWriter();
		try {
			AuthenticationStatement authenticationStatement;

			authenticationStatement = new AuthenticationStatement(statementData);
			String deviceUserId = this.beIdDeviceService.authenticate(
					sessionId, authenticationStatement);
			if (null == deviceUserId) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				writer.println("Authentication failed");
				return;
			}

			AuthenticationContext authenticationContext = AuthenticationContext
					.getAuthenticationContext(session);
			authenticationContext.setUserId(deviceUserId);
			authenticationContext.setValidity(this.samlAuthorityService
					.getAuthnAssertionValidity());
			authenticationContext.setIssuer(BeIdConstants.BEID_DEVICE_ID);
			authenticationContext.setUsedDevice(BeIdConstants.BEID_DEVICE_ID);

		} catch (DecodingException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.setHeader(SharedConstants.SAFE_ONLINE_ERROR_HTTP_HEADER, e
					.getErrorCode());
			writer.println("decoding error");
		} catch (TrustDomainNotFoundException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.setHeader(SharedConstants.SAFE_ONLINE_ERROR_HTTP_HEADER, e
					.getErrorCode());
			writer.println("Trust domain not found");
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
		}
	}
}
