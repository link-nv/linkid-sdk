/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.auth;

import java.io.IOException;
import java.net.URI;
import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.link.safeonline.common.SafeOnlineConfig;
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

    private static final Log                                                                         LOG                            = LogFactory
                                                                                                                                                .getLog(AuthenticationProtocolManager.class);

    public static final String                                                                       PROTOCOL_HANDLER_ATTRIBUTE     = AuthenticationProtocolManager.class
                                                                                                                                                                         .getName()
                                                                                                                                            + ".PROTOCOL_HANDLER";

    public static final String                                                                       TARGET_ATTRIBUTE               = AuthenticationProtocolManager.class
                                                                                                                                                                         .getName()
                                                                                                                                            + ".TARGET";

    public static final String                                                                       LANDING_PAGE_INIT_PARAM        = "LandingPage";

    public static final String                                                                       LOGOUT_LANDING_PAGE_INIT_PARAM = "LogoutLandingPage";

    private static final Map<AuthenticationProtocol, Class<? extends AuthenticationProtocolHandler>> handlerClasses                 = new HashMap<AuthenticationProtocol, Class<? extends AuthenticationProtocolHandler>>();


    private AuthenticationProtocolManager() {

        // empty
    }


    static {
        registerProtocolHandler(Saml2BrowserPostAuthenticationProtocolHandler.class);
    }


    public static void registerProtocolHandler(Class<? extends AuthenticationProtocolHandler> handlerClass) {

        if (null == handlerClass)
            throw new RuntimeException("null for handler class");
        if (isProtocolHandlerRegistered(handlerClass))
            throw new RuntimeException("already registered a protocol handler for " + handlerClass);

        SupportedAuthenticationProtocol supportedAuthenticationProtocolAnnotation = handlerClass
                                                                                                .getAnnotation(SupportedAuthenticationProtocol.class);
        if (null == supportedAuthenticationProtocolAnnotation)
            throw new RuntimeException("missing @SupportedAuthenticationProtocol on protocol handler implementation class");
        AuthenticationProtocol authenticationProtocol = supportedAuthenticationProtocolAnnotation.value();
        handlerClasses.put(authenticationProtocol, handlerClass);
    }

    /**
     * @return <code>true</code> if the given {@link AuthenticationProtocolHandler} is already registered.
     */
    public static boolean isProtocolHandlerRegistered(Class<? extends AuthenticationProtocolHandler> handlerClass) {

        SupportedAuthenticationProtocol supportedAuthenticationProtocolAnnotation = handlerClass
                                                                                                .getAnnotation(SupportedAuthenticationProtocol.class);

        return handlerClasses.containsKey(supportedAuthenticationProtocolAnnotation.value());
    }

    /**
     * Initiates the authentication.
     * 
     * <p>
     * NOTE: This method uses the request URL as the target URL, doesn't set any language/color/minimal preferences, and continues with
     * {@link #initiateAuthentication(HttpServletRequest, HttpServletResponse, String, boolean, Locale, Integer, Boolean)}.
     * </p>
     * 
     * @param request
     * @param response
     * @throws IOException
     * @throws ServletException
     */
    public static void initiateAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        String target = request.getRequestURL().toString();
        initiateAuthentication(request, response, target, false, null, null, null);
    }

    /**
     * Initiates the authentication.
     * 
     * @param target
     *            The URL where the user will be sent to after authentication has completed. If this is not an absolute URI, it will be made
     *            absolute using the web application's base URL. If <code>null</code>, the web application's base URL will be used.
     * 
     * @see AuthenticationProtocolHandler#initiateAuthentication(HttpServletRequest, HttpServletResponse, String, Locale, Integer, Boolean)
     */
    public static void initiateAuthentication(HttpServletRequest request, HttpServletResponse response, String target,
                                              boolean skipLandingPage, Locale language, Integer color, Boolean minimal)
            throws IOException, ServletException {

        SafeOnlineConfig safeOnlineConfig = SafeOnlineConfig.load(request);

        // Figure out the target and landing page URLs.
        String targetUrl = target;
        if (null == targetUrl) {
            targetUrl = safeOnlineConfig.endpointFor(request);
        } else if (!URI.create(targetUrl).isAbsolute()) {
            targetUrl = safeOnlineConfig.absoluteUrlFromPath(request, targetUrl);
        }
        String landingPage = safeOnlineConfig.absoluteUrlFromParam(request, LANDING_PAGE_INIT_PARAM);

        // Delegate the authentication initiation to the relevant protocol handler.
        AuthenticationProtocolHandler protocolHandler = findAuthenticationProtocolHandler(request);
        if (null == protocolHandler)
            throw new IllegalStateException("no active protocol handler found");

        if (null != landingPage && !skipLandingPage) {
            LOG.debug("using landing page: " + landingPage);
            storeTarget(targetUrl, request);
            protocolHandler.initiateAuthentication(request, response, landingPage, language, color, minimal);
        } else {
            clearTarget(request);
            protocolHandler.initiateAuthentication(request, response, targetUrl, language, color, minimal);
        }
    }

    private static void storeTarget(String target, HttpServletRequest request) {

        LOG.debug("storing target: " + target);
        HttpSession session = request.getSession();
        session.setAttribute(TARGET_ATTRIBUTE, target);
    }

    private static void clearTarget(HttpServletRequest request) {

        LOG.debug("clearing target:");
        HttpSession session = request.getSession();
        session.removeAttribute(TARGET_ATTRIBUTE);
    }

    /**
     * Gives back the previously stored target attribute value.
     * 
     * @param request
     */
    public static String getTarget(HttpServletRequest request) {

        String target = findTarget(request);
        if (null == target)
            throw new IllegalStateException("target attribute is null");

        return target;
    }

    /**
     * Gives back the possible stored target attribute value.
     * 
     * Can return <code>null</code> in case this protocol manager was created due to an incoming logout request in a Single Logout process.
     * 
     */
    public static String findTarget(HttpServletRequest request) {

        HttpSession session = request.getSession();
        String target = (String) session.getAttribute(TARGET_ATTRIBUTE);

        LOG.debug("found target: " + target);
        return target;
    }

    /**
     * Initiates a logout request.
     * 
     * @param target
     *            The URL where the user will be sent to after authentication has completed. If this is not an absolute URI, it will be made
     *            absolute using the web application's base URL. If <code>null</code>, the web application's base URL will be used.
     * 
     * @param request
     * @param response
     * @param subjectName
     * @throws IOException
     * @throws ServletException
     */
    public static void initiateLogout(HttpServletRequest request, HttpServletResponse response, String target, String subjectName)
            throws IOException, ServletException {

        SafeOnlineConfig safeOnlineConfig = SafeOnlineConfig.load(request);

        // Figure out the target and landing page URLs.
        String targetUrl = target;
        if (null == targetUrl) {
            targetUrl = safeOnlineConfig.endpointFor(request);
        } else if (!URI.create(targetUrl).isAbsolute()) {
            targetUrl = safeOnlineConfig.absoluteUrlFromPath(request, targetUrl);
        }
        String landingPage = safeOnlineConfig.absoluteUrlFromParam(request, LOGOUT_LANDING_PAGE_INIT_PARAM);

        // Delegate the authentication initiation to the relevant protocol handler.
        AuthenticationProtocolHandler protocolHandler = findAuthenticationProtocolHandler(request);
        if (null == protocolHandler)
            throw new IllegalStateException("no active protocol handler found");

        if (null != landingPage) {
            LOG.debug("using landing page: " + landingPage);
            storeTarget(target, request);
            protocolHandler.initiateLogout(request, response, landingPage, subjectName);
        } else {
            clearTarget(request);
            protocolHandler.initiateLogout(request, response, target, subjectName);
        }
    }

    /**
     * Returns a new authentication protocol handler for the requested authentication protocol. The returned handler has already been
     * initialized. This method will fail if a previous protocol handler was already bound to the HTTP session corresponding with the given
     * HTTP servlet request.
     * 
     * @param authenticationProtocol
     * @param authnServiceUrl
     * @param applicationName
     * @param applicationFriendlyName
     * @param applicationKeyPair
     * @param applicationCertificate
     * @param httpRequest
     * @throws ServletException
     */
    public static AuthenticationProtocolHandler createAuthenticationProtocolHandler(AuthenticationProtocol authenticationProtocol,
                                                                                    String authnServiceUrl, String applicationName,
                                                                                    String applicationFriendlyName,
                                                                                    KeyPair applicationKeyPair,
                                                                                    X509Certificate applicationCertificate,
                                                                                    boolean ssoEnabled, Map<String, String> inConfigParams,
                                                                                    HttpServletRequest httpRequest)
            throws ServletException {

        HttpSession session = httpRequest.getSession();
        if (null != session.getAttribute(PROTOCOL_HANDLER_ATTRIBUTE)) {
            LOG.error("a previous protocol handler already attached to session");
        }

        Class<? extends AuthenticationProtocolHandler> authnProtocolHandlerClass = handlerClasses.get(authenticationProtocol);
        if (null == authnProtocolHandlerClass)
            throw new ServletException("no handler for authentication protocol: " + authenticationProtocol);
        if (null == authnServiceUrl)
            throw new ServletException("authenication service URL cannot be null");
        if (null == applicationName)
            throw new ServletException("application name cannot be null");
        Map<String, String> configParams = inConfigParams;
        if (null == configParams) {
            /*
             * While optional for the authentication protocol manager, the configuration parameters are mandatory for the authentication
             * protocol handlers.
             */
            configParams = new HashMap<String, String>();
        }
        AuthenticationProtocolHandler protocolHandler;
        try {
            protocolHandler = authnProtocolHandlerClass.newInstance();
        } catch (Exception e) {
            throw new ServletException("could not load the protocol handler: " + authnProtocolHandlerClass.getName());
        }
        protocolHandler.init(authnServiceUrl, applicationName, applicationFriendlyName, applicationKeyPair, applicationCertificate,
                ssoEnabled, configParams);

        /*
         * We save the stateful protocol handler into the HTTP session as attribute.
         */
        session.setAttribute(PROTOCOL_HANDLER_ATTRIBUTE, protocolHandler);

        return protocolHandler;
    }

    /**
     * Gives back the authentication protocol handler instance bound to the HTTP session corresponding with the given HTTP servlet request.
     * In case there is no authentication protocol handler bound to the current HTTP session <code>null</code> will be returned.
     * 
     * @param httpRequest
     */
    public static AuthenticationProtocolHandler findAuthenticationProtocolHandler(HttpServletRequest httpRequest) {

        HttpSession session = httpRequest.getSession();
        AuthenticationProtocolHandler protocolHandler = (AuthenticationProtocolHandler) session.getAttribute(PROTOCOL_HANDLER_ATTRIBUTE);
        return protocolHandler;
    }

    /**
     * Cleanup the authentication handler currently attached to the HTTP session.
     * 
     * @param httpRequest
     * @throws ServletException
     */
    public static void cleanupAuthenticationHandler(HttpServletRequest httpRequest)
            throws ServletException {

        HttpSession session = httpRequest.getSession();
        AuthenticationProtocolHandler protocolHandler = (AuthenticationProtocolHandler) session.getAttribute(PROTOCOL_HANDLER_ATTRIBUTE);
        if (null == protocolHandler)
            throw new ServletException("no protocol handler to cleanup");
        LOG.debug("cleanup authentication handler");
        session.removeAttribute(PROTOCOL_HANDLER_ATTRIBUTE);

        session.removeAttribute(TARGET_ATTRIBUTE);
    }
}
