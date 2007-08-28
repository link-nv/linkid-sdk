/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.auth;

import java.security.KeyPair;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import net.link.safeonline.sdk.auth.saml2.Saml2BrowserPostAuthenticationProtocolHandler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Manager class for the stateful authentication protocol handlers.
 * 
 * <p>
 * The state is preserved using the HTTP session.
 * </p>
 * 
 * @author fcorneli
 * 
 */
public class AuthenticationProtocolManager {

	private static final Log LOG = LogFactory
			.getLog(AuthenticationProtocolManager.class);

	public static final String PROTOCOL_HANDLER_ATTRIBUTE = AuthenticationProtocolManager.class
			.getName()
			+ ".PROTOCOL_HANDLER";

	private static final Map<AuthenticationProtocol, Class<? extends AuthenticationProtocolHandler>> handlerClasses = new HashMap<AuthenticationProtocol, Class<? extends AuthenticationProtocolHandler>>();

	private AuthenticationProtocolManager() {
		// empty
	}

	static {
		registerProtocolHandler(SimplePlainUrlAuthenticationProtocolHandler.class);
		registerProtocolHandler(Saml2BrowserPostAuthenticationProtocolHandler.class);
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
	 * initialized. This method will fail if a previous protocol handler was
	 * already bound to the HTTP session corresponding with the given HTTP
	 * servlet request.
	 * 
	 * @param authenticationProtocol
	 * @param authnServiceUrl
	 * @param applicationName
	 * @param applicationKeyPair
	 * @param configParams
	 *            the optional protocol handler configuration parameters.
	 * @param httpRequest
	 * @return
	 * @throws ServletException
	 */
	public static AuthenticationProtocolHandler createAuthenticationProtocolHandler(
			AuthenticationProtocol authenticationProtocol,
			String authnServiceUrl, String applicationName,
			KeyPair applicationKeyPair, Map<String, String> configParams,
			HttpServletRequest httpRequest) throws ServletException {
		HttpSession session = httpRequest.getSession();
		if (null != session.getAttribute(PROTOCOL_HANDLER_ATTRIBUTE)) {
			throw new ServletException(
					"a previous protocol handler already attached to session");
		}

		Class<? extends AuthenticationProtocolHandler> authnProtocolHandlerClass = handlerClasses
				.get(authenticationProtocol);
		if (null == authnProtocolHandlerClass) {
			throw new ServletException(
					"no handler for authentication protocol: "
							+ authenticationProtocol);
		}
		if (null == authnServiceUrl) {
			throw new ServletException(
					"authenication service URL cannot be null");
		}
		if (null == applicationName) {
			throw new ServletException("application name cannot be null");
		}
		if (null == configParams) {
			/*
			 * While optional for the authentication protocol manager, the
			 * configuration parameters are mandatory for the authentication
			 * protocol handlers.
			 */
			configParams = new HashMap<String, String>();
		}
		AuthenticationProtocolHandler protocolHandler;
		try {
			protocolHandler = authnProtocolHandlerClass.newInstance();
		} catch (Exception e) {
			throw new ServletException("could not load the protocol handler: "
					+ authnProtocolHandlerClass.getName());
		}
		protocolHandler.init(authnServiceUrl, applicationName,
				applicationKeyPair, configParams);

		/*
		 * We save the stateful protocol handler into the HTTP session as
		 * attribute.
		 */
		session.setAttribute(PROTOCOL_HANDLER_ATTRIBUTE, protocolHandler);

		return protocolHandler;
	}

	/**
	 * Gives back the authentication protocol handler instance bound to the HTTP
	 * session corresponding with the given HTTP servlet request. In case there
	 * is no authentication protocol handler bound to the current HTTP session
	 * <code>null</code> will be returned.
	 * 
	 * @param httpRequest
	 * @return
	 * @throws ServletException
	 */
	public static AuthenticationProtocolHandler findAuthenticationProtocolHandler(
			HttpServletRequest httpRequest) throws ServletException {
		HttpSession session = httpRequest.getSession();
		AuthenticationProtocolHandler protocolHandler = (AuthenticationProtocolHandler) session
				.getAttribute(PROTOCOL_HANDLER_ATTRIBUTE);
		return protocolHandler;
	}

	/**
	 * Cleanup the authentication handler currently attached to the HTTP
	 * session.
	 * 
	 * @param httpRequest
	 * @throws ServletException
	 */
	public static void cleanupAuthenticationHandler(
			HttpServletRequest httpRequest) throws ServletException {
		HttpSession session = httpRequest.getSession();
		AuthenticationProtocolHandler protocolHandler = (AuthenticationProtocolHandler) session
				.getAttribute(PROTOCOL_HANDLER_ATTRIBUTE);
		if (null == protocolHandler) {
			throw new ServletException("no protocol handler to cleanup");
		}
		LOG.debug("cleanup authentication handler");
		session.removeAttribute(PROTOCOL_HANDLER_ATTRIBUTE);
	}
}
