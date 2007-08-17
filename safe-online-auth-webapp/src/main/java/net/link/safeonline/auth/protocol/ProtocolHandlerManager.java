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

import net.link.safeonline.auth.protocol.saml2.Saml2PostProtocolHandler;

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

	private static final Log LOG = LogFactory
			.getLog(ProtocolHandlerManager.class);

	private ProtocolHandlerManager() {
		// empty
	}

	private static final List<ProtocolHandler> protocolHandlers = new LinkedList<ProtocolHandler>();

	private static final Map<String, ProtocolHandler> protocolHandlerMap = new HashMap<String, ProtocolHandler>();

	static {
		registerProtocolHandler(SimpleProtocolHandler.class);
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
							+ protocolHandlerClass.getName());
		}
	}

	/**
	 * Handles the authentication protocol request. This method return a
	 * protocol context in case of a successful initiation of the authentication
	 * procedure. The method returns <code>null</code> is no appropriate
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

		String username = (String) session.getAttribute("username");
		if (null == username) {
			throw new ProtocolException(
					"incorrect authentication state (missing username)");
		}

		String target = (String) session.getAttribute("target");
		if (null == target) {
			throw new ProtocolException(
					"incorrect authentication state (missing target)");
		}

		try {
			protocolHandler.authnResponse(session, response);
		} catch (ProtocolException e) {
			String protocolName = protocolHandler.getName();
			e.setProtocolName(protocolName);
			throw e;
		}
	}
}
