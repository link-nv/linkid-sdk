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
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Interface for authentication protocol handlers. Protocol handlers are stateful since they must be capable of handling
 * the challenge-response aspect of the authentication protocol. Since protocol handlers are stored in the HTTP session
 * they must be serializable.
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
     * @param configParams
     *            additional specific authentication protocol configuration parameters.
     */
    void init(String authnServiceUrl, String applicationName, String applicationFriendlyName,
            KeyPair applicationKeyPair, X509Certificate applicationCertificate, Map<String, String> configParams);

    /**
     * Initiates the authentication request towards the SafeOnline authentication web application.
     *
     * @param request
     * @param response
     * @param targetUrl
     *            the optional target URL. If omitted the request URL will be used as target URL.
     * @throws IOException
     * @throws ServletException
     */
    void initiateAuthentication(HttpServletRequest request, HttpServletResponse response, String targetUrl)
            throws IOException, ServletException;

    /**
     * Finalize the active authentication process.
     *
     * @param httpRequest
     * @param httpResponse
     * @return the authenticated user Id or <code>null</code> if the handler thinks the request has nothing to do with
     *         authentication.
     * @throws ServletException
     */
    String finalizeAuthentication(HttpServletRequest httpRequest, HttpServletResponse httpResponse)
            throws ServletException;
}
