/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.auth.servlet;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.auth.LoginManager;
import net.link.safeonline.authentication.exception.ArgumentIntegrityException;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.DecodingException;
import net.link.safeonline.authentication.exception.DeviceNotFoundException;
import net.link.safeonline.authentication.exception.ExistingUserException;
import net.link.safeonline.authentication.service.AuthenticationService;
import net.link.safeonline.dao.DeviceDAO;
import net.link.safeonline.entity.DeviceEntity;
import net.link.safeonline.pkix.exception.TrustDomainNotFoundException;
import net.link.safeonline.servlet.AbstractStatementServlet;
import net.link.safeonline.shared.SharedConstants;
import net.link.safeonline.util.ee.EjbUtils;

/**
 * The identity servlet implementation. This servlet receives its data from the
 * BeID via the IdentityApplet. Via this data web will register the user within
 * SafeOnline.
 * 
 * @author fcorneli
 * 
 */
public class RegistrationServlet extends AbstractStatementServlet {

	private static final long serialVersionUID = 1L;

	private DeviceDAO deviceDAO;

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		this.deviceDAO = EjbUtils.getEJB("SafeOnline/DeviceDAOBean/local",
				DeviceDAO.class);
	}

	@Override
	protected void processStatement(byte[] statementData, HttpSession session,
			HttpServletResponse response) {
		String requestedUsername = (String) session
				.getAttribute("requestedUsername");

		AuthenticationService authenticationService = AuthenticationServiceManager
				.getAuthenticationService(session);
		String sessionId = session.getId();
		try {
			authenticationService.registerAndAuthenticate(sessionId,
					requestedUsername, statementData);
			String userId = authenticationService.getUserId();
			response.setStatus(HttpServletResponse.SC_OK);
			DeviceEntity beidDevice = this.deviceDAO
					.getDevice(SafeOnlineConstants.BEID_DEVICE_ID);
			LoginManager.login(session, userId, beidDevice);
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
		} catch (AttributeTypeNotFoundException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		} catch (DeviceNotFoundException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		}
	}
}
