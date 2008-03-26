/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.jsfunit.servlet;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.auth.LoginManager;
import net.link.safeonline.auth.protocol.ProtocolContext;
import net.link.safeonline.auth.protocol.ProtocolException;
import net.link.safeonline.auth.protocol.ProtocolHandlerManager;
import net.link.safeonline.authentication.exception.DeviceNotFoundException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.dao.DeviceDAO;
import net.link.safeonline.entity.DeviceEntity;
import net.link.safeonline.service.SubjectService;
import net.link.safeonline.util.ee.EjbUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * JSFUnit Test Entry Servlet. This servlet acts as entry and exit point in what
 * is the authentication webapp in the normal flow. It will accept the saml
 * request, authenticate the user and send back the saml response.
 * 
 * @author wvdhaute
 * 
 */
public class EntryServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private static final Log LOG = LogFactory.getLog(EntryServlet.class);

	private String username;

	private SubjectService subjectService;

	private DeviceDAO deviceDAO;

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		this.username = getInitParameter("username");
		loadDependencies();
	}

	private void loadDependencies() {
		this.deviceDAO = EjbUtils.getEJB("SafeOnline/DeviceDAOBean/local",
				DeviceDAO.class);
		this.subjectService = EjbUtils.getEJB(
				"SafeOnline/SubjectServiceBean/local", SubjectService.class);
	}

	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		handleInvocation(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		handleInvocation(request, response);
	}

	private void handleInvocation(HttpServletRequest request,
			HttpServletResponse response) throws ServletException {
		// Handle the incoming SAML request
		handleRequest(request, response);

		// authenticate
		authenticate(request, response);

		// Send out the SAML response
		sendResponse(request, response);
	}

	private void handleRequest(HttpServletRequest request,
			@SuppressWarnings("unused")
			HttpServletResponse response) throws ServletException {
		LOG.debug("handle SAML request");
		ProtocolContext protocolContext;
		try {
			protocolContext = ProtocolHandlerManager.handleRequest(request);
		} catch (ProtocolException e) {
			LOG.debug("Exception: " + e.getMessage());
			throw new ServletException(e.getMessage());
		}

		if (null == protocolContext) {
			LOG.debug("Unsupported protocol");
			throw new ServletException("Unsupported protocol");
		}

		/*
		 * We save the result of the protocol handler into the HTTP session.
		 */
		HttpSession session = request.getSession();
		LoginManager
				.setApplication(session, protocolContext.getApplicationId());
		LoginManager.setTarget(session, protocolContext.getTarget());
		LoginManager.setRequiredDevices(session, protocolContext
				.getRequiredDevices());

	}

	private void authenticate(HttpServletRequest request,
			@SuppressWarnings("unused")
			HttpServletResponse response) throws ServletException {
		LOG.debug("authenticate: " + this.username);
		HttpSession session = request.getSession();

		String userId;
		try {
			userId = this.subjectService.getSubjectFromUserName(this.username)
					.getUserId();
		} catch (SubjectNotFoundException e) {
			LOG.debug("subject not found");
			throw new ServletException("subject not found");
		}

		LoginManager.setUsername(session, this.username);
		DeviceEntity device;
		try {
			device = this.deviceDAO
					.getDevice(SafeOnlineConstants.USERNAME_PASSWORD_DEVICE_ID);
		} catch (DeviceNotFoundException e) {
			LOG.debug("device not found");
			throw new ServletException("device not found");
		}
		LoginManager.login(session, userId, device);
	}

	private void sendResponse(HttpServletRequest request,
			HttpServletResponse response) throws ServletException {
		LOG.debug("send SAML response");
		HttpSession session = request.getSession();
		LOG.debug("session: " + session.getId());
		/*
		 * Set this to prevent from the protocol handler manager to invalidate
		 * our session
		 */
		session
				.setAttribute(
						ProtocolHandlerManager.PROTOCOL_DONT_INVALIDATE_SESSION_ATTRIBUTE,
						"true");
		try {
			ProtocolHandlerManager.authnResponse(session, response);
		} catch (ProtocolException e) {
			LOG.debug("Exception: " + e.getMessage());
			throw new ServletException(e.getMessage());
		}
		LOG.debug("sent SAML response");
	}
}
