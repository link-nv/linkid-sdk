/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.auth.protocol;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.link.safeonline.auth.LoginManager;
import net.link.safeonline.auth.protocol.saml2.Saml2PostProtocolHandler;
import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.exception.SubscriptionNotFoundException;
import net.link.safeonline.authentication.service.UserIdMappingService;
import net.link.safeonline.entity.DeviceEntity;
import net.link.safeonline.util.ee.EjbUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Manager class for the protocol handlers registered within the authentication
 * web application.
 * 
 * @author fcorneli
 * 
 */
public class ProtocolHandlerManager {

	public static final String PROTOCOL_HANDLER_ID_ATTRIBUTE = ProtocolHandlerManager.class
			.getName()
			+ ".ProtocolHandlerName";

	public static final String PROTOCOL_DONT_INVALIDATE_SESSION_ATTRIBUTE = ProtocolHandlerManager.class
			.getName()
			+ ".DontInvalidateSession";

	private static final Log LOG = LogFactory
			.getLog(ProtocolHandlerManager.class);

	private ProtocolHandlerManager() {
		// empty
	}

	private static final List<ProtocolHandler> protocolHandlers = new LinkedList<ProtocolHandler>();

	private static final Map<String, ProtocolHandler> protocolHandlerMap = new HashMap<String, ProtocolHandler>();

	static {
		registerProtocolHandler(Saml2PostProtocolHandler.class);
	}

	private static void registerProtocolHandler(
			Class<? extends ProtocolHandler> protocolHandlerClass) {
		try {
			ProtocolHandler protocolHandler = protocolHandlerClass
					.newInstance();
			String protocolId = protocolHandlerClass.getName();
			if (protocolHandlerMap.containsKey(protocolId)) {
				throw new RuntimeException(
						"protocol handler already registered for Id: "
								+ protocolId);
			}
			protocolHandlerMap.put(protocolId, protocolHandler);
			protocolHandlers.add(protocolHandler);
		} catch (Exception e) {
			throw new RuntimeException(
					"could not initialize protocol handler: "
							+ protocolHandlerClass.getName() + "; message: "
							+ e.getMessage(), e);
		}
	}

	/**
	 * Handles the authentication protocol request. This method return a
	 * protocol context in case of a successful initiation of the authentication
	 * procedure. The method returns <code>null</code> if no appropriate
	 * authentication protocol handler has been found.
	 * 
	 * @param request
	 * @return a protocol context or <code>null</code>.
	 * @throws ProtocolException
	 *             in case of a protocol error.
	 */
	public static ProtocolContext handleRequest(HttpServletRequest request)
			throws ProtocolException {
		for (ProtocolHandler protocolHandler : protocolHandlers) {
			LOG.debug("trying protocol handler: "
					+ protocolHandler.getClass().getSimpleName());
			ProtocolContext protocolContext;
			try {
				protocolContext = protocolHandler.handleRequest(request);
			} catch (ProtocolException e) {
				// TODO: yield audit event
				String protocolName = protocolHandler.getName();
				e.setProtocolName(protocolName);
				throw e;
			}
			if (null != protocolContext) {
				HttpSession session = request.getSession();
				String protocolId = protocolHandler.getClass().getName();
				session.setAttribute(PROTOCOL_HANDLER_ID_ATTRIBUTE, protocolId);
				return protocolContext;
			}
		}
		return null;
	}

	/**
	 * Handles the authentication response according to the authentication
	 * protocol by which the current authentication procedure was initiated.
	 * 
	 * @param session
	 * @param response
	 * @throws ProtocolException
	 */
	public static void authnResponse(HttpSession session,
			HttpServletResponse response) throws ProtocolException {
		String protocolId = (String) session
				.getAttribute(PROTOCOL_HANDLER_ID_ATTRIBUTE);
		if (null == protocolId) {
			throw new ProtocolException("incorrect request handling detected");
		}
		ProtocolHandler protocolHandler = protocolHandlerMap.get(protocolId);
		if (null == protocolHandler) {
			throw new ProtocolException(
					"unsupported protocol for protocol Id: " + protocolId);
		}

		String username = LoginManager.findUsername(session);
		if (null == username) {
			throw new ProtocolException(
					"incorrect authentication state (missing username)");
		}

		String target = (String) session.getAttribute("target");
		if (null == target) {
			throw new ProtocolException(
					"incorrect authentication state (missing target)");
		}

		DeviceEntity device = LoginManager.findAuthenticationDevice(session);
		if (null == device) {
			throw new ProtocolException("missing device session attribute");
		}

		String applicationName = LoginManager.findApplication(session);
		if (null == applicationName) {
			throw new ProtocolException(
					"incorrect authentication state (missing application name)");
		}

		/*
		 * Retrieve the wanted user id for this application's id scope.
		 */
		String userId = null;
		try {
			userId = getUserId(applicationName, username);
		} catch (SubscriptionNotFoundException e) {
			throw new ProtocolException(
					"unable to retrieve user id (subscription not found)");
		} catch (ApplicationNotFoundException e) {
			throw new ProtocolException(
					"unable to retrieve user id (application not found)");
		}
		if (null == userId) {
			throw new ProtocolException("unable to retrieve user id");
		}
		session.setAttribute(LoginManager.USERNAME_ATTRIBUTE, userId);

		try {
			protocolHandler.authnResponse(session, response);
		} catch (ProtocolException e) {
			String protocolName = protocolHandler.getName();
			e.setProtocolName(protocolName);
			throw e;
		}

		/*
		 * It's important to invalidate the session here. Else we spill
		 * resources and we prevent a user to login twice since the
		 * authentication service instance was already removed from the session
		 * context.
		 * 
		 */
		session.invalidate();
	}

	private static String getUserId(String applicationName, String username)
			throws SubscriptionNotFoundException, ApplicationNotFoundException {
		UserIdMappingService userIdMappingService = EjbUtils.getEJB(
				"SafeOnline/UserIdMappingServiceBean/local",
				UserIdMappingService.class);
		return userIdMappingService.getApplicationUserId(applicationName,
				username);
	}
}
