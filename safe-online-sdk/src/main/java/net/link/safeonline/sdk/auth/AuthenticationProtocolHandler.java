/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.auth;

import java.io.IOException;
import java.io.Serializable;
import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Interface for authentication protocol handlers. Protocol handlers are stateful since they must be capable of handling the
 * challenge-response aspect of the authentication protocol. Since protocol handlers are stored in the HTTP session they must be
 * serializable.
 * 
 * @author fcorneli
 * 
 */
public interface AuthenticationProtocolHandler extends Serializable {

    /**
     * Initializes the authentication protocol handler.
     * 
     * @param authnServiceUrl
     *            the URL of the authentication service to be used by the handler.
     * @param applicationName
     *            the application name to be used by the handler.
     * @param applicationFriendlyName
     *            the human readable application name
     * @param applicationKeyPair
     *            the application RSA key pair used to sign the authentication request.
     * @param applicationCertificate
     *            the application certificate used to sign the WS-Security signatures.
     * @param ssoEnabled
     *            whether single sign-on can be used or not
     * @param configParams
     *            additional specific authentication protocol configuration parameters.
     */
    void init(String authnServiceUrl, String applicationName, String applicationFriendlyName, KeyPair applicationKeyPair,
              X509Certificate applicationCertificate, boolean ssoEnabled, Map<String, String> configParams);

    /**
     * Initiates the authentication request towards the SafeOnline authentication web application.
     * 
     * @param httpRequest
     * @param httpResponse
     * @param targetUrl
     *            the optional target URL. If omitted the request URL will be used as target URL.
     * @param language
     *            The locale that represents the language to use in OLAS.
     * @param color
     *            The 24-bit color to base the OLAS color theme on.
     * @param minimal
     *            <code>true</code>: OLAS will make its pages smaller by hiding header/footer images so it is more suitable to be used in an
     *            IFrame, for example.
     * @param session
     *            optional session info used if an application wishes to track the session
     * @throws IOException
     * @throws ServletException
     */
    public void initiateAuthentication(HttpServletRequest httpRequest, HttpServletResponse httpResponse, String targetUrl, Locale language,
                                       Integer color, Boolean minimal, String session)
            throws IOException, ServletException;

    /**
     * Finalize the active authentication process.
     * 
     * @param httpRequest
     * @param httpResponse
     * @return the authenticated user Id or <code>null</code> if the handler thinks the request has nothing to do with authentication.
     * @throws ServletException
     */
    AuthenticationProtocolContext finalizeAuthentication(HttpServletRequest httpRequest, HttpServletResponse httpResponse)
            throws ServletException;

    /**
     * Initiates the logout request towards the SafeOnline authentication web application.
     * 
     * @param request
     * @param response
     * @param targetUrl
     *            the optional target URL. If omitted the request URL will be used as target URL.
     * @param subjectName
     *            the subject ID
     * 
     * @throws IOException
     * @throws ServletException
     */
    void initiateLogout(HttpServletRequest request, HttpServletResponse response, String targetUrl, String subjectName)
            throws IOException, ServletException;

    /**
     * Finalize the logout process.
     * 
     * @return true if all all applications to need to be logged out due to the request were logged out.
     * 
     */
    boolean finalizeLogout(HttpServletRequest request, HttpServletResponse response)
            throws ServletException;

    /**
     * Handle an incoming logout request, sent from the authentication webapp due to a logout request from another application.
     * 
     * @return userId
     * @throws ServletException
     */
    String handleLogoutRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException;

    /**
     * Sends back a logout response towards the SafeOnline authentication web application.
     * 
     * @param userId
     * @param success
     * @param request
     * @param response
     * 
     * @throws IOException
     * @throws ServletException
     */
    void sendLogoutResponse(boolean success, HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException;
}
