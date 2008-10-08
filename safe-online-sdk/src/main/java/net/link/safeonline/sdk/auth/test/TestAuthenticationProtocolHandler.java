/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.sdk.auth.test;

import java.io.IOException;
import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.link.safeonline.sdk.auth.AuthenticationProtocol;
import net.link.safeonline.sdk.auth.AuthenticationProtocolHandler;
import net.link.safeonline.sdk.auth.SupportedAuthenticationProtocol;
import net.link.safeonline.sdk.auth.servlet.LoginServlet;
import net.link.safeonline.sdk.auth.servlet.LogoutServlet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * <h2>{@link TestAuthenticationProtocolHandler}<br>
 * <sub>[in short] (TODO).</sub></h2>
 * 
 * <p>
 * [description / usage].
 * </p>
 * 
 * <p>
 * <i>Oct 8, 2008</i>
 * </p>
 * 
 * @author lhunath
 */
@SupportedAuthenticationProtocol(AuthenticationProtocol.UNIT_TEST)
public class TestAuthenticationProtocolHandler implements AuthenticationProtocolHandler {

    private static final long serialVersionUID = 1L;
    private static final Log  LOG              = LogFactory.getLog(TestAuthenticationProtocolHandler.class);

    private static String     authenticatedUserId;


    public static void setAuthenticatingUser(String userId) {

        authenticatedUserId = userId;
    }

    /**
     * {@inheritDoc}
     */
    public void init(String authnServiceUrl, String applicationName, String applicationFriendlyName,
            KeyPair applicationKeyPair, X509Certificate applicationCertificate, boolean ssoEnabled,
            Map<String, String> configParams) {

        /* Nothing to initialize. */
    }

    /**
     * {@inheritDoc}
     */
    public void initiateAuthentication(HttpServletRequest request, HttpServletResponse response, String targetUrl)
            throws IOException, ServletException {

        LOG.info("Initiated Authentication; invoking LoginServlet");
        
        new LoginServlet().service(new MockHttpServletRequest(request, targetUrl), response);
    }

    /**
     * {@inheritDoc}
     */
    public String finalizeAuthentication(HttpServletRequest httpRequest, HttpServletResponse httpResponse)
            throws ServletException {

        LOG.info("Finalizing Authentication for userId: " + authenticatedUserId);
        
        return authenticatedUserId;
    }

    /**
     * {@inheritDoc}
     */
    public void initiateLogout(HttpServletRequest request, HttpServletResponse response, String targetUrl,
            String subjectName) throws IOException, ServletException {

        LOG.info("Initiated Logout; invoking LogoutServlet");

        new LogoutServlet().service(new MockHttpServletRequest(request, targetUrl), response);
    }

    /**
     * {@inheritDoc}
     */
    public boolean finalizeLogout(HttpServletRequest request, HttpServletResponse response) throws ServletException {

        LOG.info("Finalizing Logout as successful");

        return true;
    }

    /**
     * {@inheritDoc}
     */
    public String handleLogoutRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException {

        throw new IllegalStateException("Unsupported by this authentication handler!");
    }

    /**
     * {@inheritDoc}
     */
    public void sendLogoutResponse(boolean success, HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        throw new IllegalStateException("Unsupported by this authentication handler!");
    }

}
