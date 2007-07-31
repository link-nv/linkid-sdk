/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.auth;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;


/**
 * Manager class for authentication protocol handlers.
 * 
 * @author fcorneli
 * 
 */
public class AuthenticationProtocolManager {

	private static final Map<AuthenticationProtocol, Class<? extends AuthenticationProtocolHandler>> handlerClasses = new HashMap<AuthenticationProtocol, Class<? extends AuthenticationProtocolHandler>>();

	private AuthenticationProtocolManager() {
		// empty
	}

	static {
		registerProtocolHandler(SimplePlainUrlAuthenticationProtocolHandler.class);
	}

	private static void registerProtocolHandler(
			Class<? extends AuthenticationProtocolHandler> handlerClass) {
		if (null == handlerClass) {
			throw new RuntimeException("null for handler class");
		}
		SupportedAuthenticationProtocol supportedAuthenticationProtocolAnnotation = handlerClass
				.getAnnotation(SupportedAuthenticationProtocol.class);
		if (null == supportedAuthenticationProtocolAnnotation) {
			throw new RuntimeException(
					"missing @SupportedAuthenticationProtocol on protocol handler implementation class");
		}
		AuthenticationProtocol authenticationProtocol = supportedAuthenticationProtocolAnnotation
				.value();
		if (handlerClasses.containsKey(authenticationProtocol)) {
			throw new RuntimeException(
					"already registered a protocol handler for "
							+ authenticationProtocol);
		}
		handlerClasses.put(authenticationProtocol, handlerClass);
	}

	/**
	 * Returns a new authentication protocol handler for the requested
	 * authentication protocol. The returned handler has already been
	 * initialized.
	 * 
	 * @param authenticationProtocol
	 * @param authnServiceUrl
	 * @param applicationName
	 * @return
	 * @throws ServletException
	 */
	public static AuthenticationProtocolHandler getAuthenticationProtocolHandler(
			AuthenticationProtocol authenticationProtocol,
			String authnServiceUrl, String applicationName)
			throws ServletException {
		Class<? extends AuthenticationProtocolHandler> authnProtocolHandlerClass = handlerClasses
				.get(authenticationProtocol);
		if (null == authnProtocolHandlerClass) {
			throw new ServletException(
					"no handler for authentication protocol: "
							+ authenticationProtocol);
		}
		AuthenticationProtocolHandler protocolHandler;
		try {
			protocolHandler = authnProtocolHandlerClass.newInstance();
		} catch (Exception e) {
			throw new ServletException("could not load the protocol handler: "
					+ authnProtocolHandlerClass.getName());
		}
		protocolHandler.init(authnServiceUrl, applicationName);
		return protocolHandler;
	}
}
