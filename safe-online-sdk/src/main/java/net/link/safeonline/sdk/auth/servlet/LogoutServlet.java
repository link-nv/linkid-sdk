/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.auth.servlet;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyPair;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.cert.X509Certificate;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.link.safeonline.sdk.KeyStoreUtils;
import net.link.safeonline.sdk.auth.AuthenticationProtocol;
import net.link.safeonline.sdk.auth.AuthenticationProtocolHandler;
import net.link.safeonline.sdk.auth.AuthenticationProtocolManager;
import net.link.safeonline.sdk.auth.filter.LoginManager;
import net.link.safeonline.sdk.auth.seam.SafeOnlineLoginUtils;
import net.link.safeonline.util.servlet.ErrorMessage;
import net.link.safeonline.util.servlet.SafeOnlineConfig;
import net.link.safeonline.util.servlet.annotation.Context;
import net.link.safeonline.util.servlet.annotation.Init;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Logout Servlet. This servlet contains the landing page to finalize the logout process initiated by the web application. This servlet also
 * removes the <code>userId</code> attribute and redirects to the specified target when the logout request was made.
 * 
 * This servlet also handles a logout request sent by the SafeOnline authentication web application due to a single logout request sent by
 * an OLAS application. After handling the request, it will redirect to <code>LogoutPath</code>. To finalize this, the web application
 * should redirect back to this page using Http GET, which will trigger this landing page to send back a logout response to the SafeOnline
 * authentication web application.
 * 
 * @author wvdhaute
 * 
 */
public class LogoutServlet extends AbstractLandingInjectionServlet {

    private static final long                  serialVersionUID       = 1L;

    private static final Log                   LOG                    = LogFactory.getLog(LogoutServlet.class);

    public static final AuthenticationProtocol DEFAULT_AUTHN_PROTOCOL = AuthenticationProtocol.SAML2_BROWSER_POST;

    public static final String                 INVALIDATE_SESSION     = "OLAS:Invalidated";

    @Init(name = "LogoutPath")
    private String                             logoutPath;

    @Init(name = "ErrorPage", optional = true)
    private String                             errorPage;

    @Context(name = SafeOnlineLoginUtils.LOGOUT_EXIT_SERVICE_PATH_INIT_PARAM)
    private String                             logoutExitServicePath;

    @Context(name = SafeOnlineLoginUtils.APPLICATION_NAME_CONTEXT_PARAM)
    private String                             applicationName;

    @Context(name = SafeOnlineLoginUtils.APPLICATION_FRIENDLY_NAME_INIT_PARAM, optional = true)
    private String                             applicationFriendlyName;

    @Context(name = SafeOnlineLoginUtils.AUTHN_PROTOCOL_CONTEXT_PARAM, optional = true)
    private String                             authenticationProtocolString;

    private AuthenticationProtocol             authenticationProtocol;

    @Context(name = SafeOnlineLoginUtils.KEY_STORE_RESOURCE_CONTEXT_PARAM, optional = true)
    private String                             p12KeyStoreResourceName;

    @Context(name = SafeOnlineLoginUtils.KEY_STORE_FILE_CONTEXT_PARAM, optional = true)
    private String                             p12KeyStoreFileName;

    @Context(name = SafeOnlineLoginUtils.KEY_STORE_PASSWORD_CONTEXT_PARAM)
    private String                             keyStorePassword;

    @Context(name = SafeOnlineLoginUtils.KEY_STORE_TYPE_CONTEXT_PARAM, defaultValue = "pkcs12")
    private String                             keyStoreType;

    private KeyPair                            applicationKeyPair;

    private X509Certificate                    applicationCertificate;


    @Override
    public void init(ServletConfig config)
            throws ServletException {

        super.init(config);
        LOG.debug("init");
        if (null == authenticationProtocolString) {
            authenticationProtocol = DEFAULT_AUTHN_PROTOCOL;
        } else {
            authenticationProtocol = AuthenticationProtocol.toAuthenticationProtocol(authenticationProtocolString);
        }
        LOG.debug("authentication protocol: " + authenticationProtocol);

        InputStream keyStoreInputStream = null;
        if (null != p12KeyStoreResourceName) {
            Thread currentThread = Thread.currentThread();
            ClassLoader classLoader = currentThread.getContextClassLoader();
            LOG.debug("classloader name: " + classLoader.getClass().getName());
            keyStoreInputStream = classLoader.getResourceAsStream(p12KeyStoreResourceName);
            if (null == keyStoreInputStream)
                throw new UnavailableException("PKCS12 keystore resource not found: " + p12KeyStoreResourceName);
        } else if (null != p12KeyStoreFileName) {
            try {
                keyStoreInputStream = new FileInputStream(p12KeyStoreFileName);
            } catch (FileNotFoundException e) {
                throw new UnavailableException("PKCS12 keystore resource not found: " + p12KeyStoreFileName);
            }
        }
        if (null != keyStoreInputStream) {
            PrivateKeyEntry privateKeyEntry = KeyStoreUtils.loadPrivateKeyEntry(keyStoreType, keyStoreInputStream, keyStorePassword,
                    keyStorePassword);
            applicationKeyPair = new KeyPair(privateKeyEntry.getCertificate().getPublicKey(), privateKeyEntry.getPrivateKey());
            applicationCertificate = (X509Certificate) privateKeyEntry.getCertificate();
        }
    }

    @Override
    protected void invokeGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        /*
         * Finalize the logout process for this application following a single logout from another web application.
         */
        try {
            String target = AuthenticationProtocolManager.findTarget(request);
            if (null != target) {
                // this indicates the end of a single logout process, started by this web application
                AuthenticationProtocolManager.cleanupAuthenticationHandler(request);

                LOG.debug("target: " + target);
                response.sendRedirect(target);
            } else {
                AuthenticationProtocolHandler protocolHandler = AuthenticationProtocolManager.findAuthenticationProtocolHandler(request);
                if (null == protocolHandler) {
                    String msg = "no protocol handler active";
                    LOG.error(msg);
                    redirectToErrorPage(request, response, errorPage, null, new ErrorMessage(msg));
                    return;
                }

                protocolHandler.sendLogoutResponse(true, request, response);

                AuthenticationProtocolManager.cleanupAuthenticationHandler(request);
            }
        } finally {
            if (request.getSession().getAttribute(INVALIDATE_SESSION) != null) {
                request.getSession().invalidate();
            }
        }
    }

    @Override
    protected void invokePost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        SafeOnlineConfig safeOnlineConfig = SafeOnlineConfig.load(request);

        AuthenticationProtocolHandler protocolHandler = AuthenticationProtocolManager.findAuthenticationProtocolHandler(request);
        if (null == protocolHandler) {
            /*
             * The landing page is also used to handle logout requests sent out by the authentication webapp following a logout request from
             * another application.
             */
            try {
                protocolHandler = AuthenticationProtocolManager.createAuthenticationProtocolHandler(authenticationProtocol,
                        safeOnlineConfig.authbase() + logoutExitServicePath, applicationName, applicationFriendlyName, applicationKeyPair,
                        applicationCertificate, true, configParams, request);
                LOG.debug("initialized protocol");
            } catch (ServletException e) {
                throw new RuntimeException("could not init authentication protocol handler: " + authenticationProtocol
                        + "; original message: " + e.getMessage(), e);
            }

            String logoutUserId = protocolHandler.handleLogoutRequest(request, response);
            if (null == logoutUserId) {
                String msg = "invalid logout request";
                LOG.error(msg);
                redirectToErrorPage(request, response, errorPage, null, new ErrorMessage(msg));
                return;
            }

            String userId = LoginManager.findUserId(request);
            if (null == userId) {
                LOG.debug("user already logged out in here");
                protocolHandler.sendLogoutResponse(false, request, response);
                return;
            }

            // check if the logout is intended for this user
            if (!logoutUserId.equals(userId)) {
                String msg = "trying to logout a different user";
                LOG.error(msg);
                redirectToErrorPage(request, response, errorPage, null, new ErrorMessage(msg));
                return;
            }

            response.sendRedirect(logoutPath);
            return;
        }

        /*
         * Here we finalize a single logout process initiated by this web application.
         */
        boolean logoutSuccess = protocolHandler.finalizeLogout(request, response);
        if (false == logoutSuccess) {
            String msg = "protocol handler could not finalize";
            LOG.error(msg);
            redirectToErrorPage(request, response, errorPage, null, new ErrorMessage(msg));
            return;
        }

        response.sendRedirect(logoutPath);
    }
}
