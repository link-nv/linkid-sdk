/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.demo.wicket.test;

import java.io.IOException;
import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.link.safeonline.demo.wicket.test.MockHttpServletRequest.Method;
import net.link.safeonline.sdk.auth.AuthenticationProtocol;
import net.link.safeonline.sdk.auth.AuthenticationProtocolHandler;
import net.link.safeonline.sdk.auth.SupportedAuthenticationProtocol;
import net.link.safeonline.sdk.auth.servlet.LoginServlet;
import net.link.safeonline.sdk.auth.servlet.LogoutServlet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.Session;


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

    private static final long           serialVersionUID = 1L;
    static final Log                    LOG              = LogFactory.getLog(TestAuthenticationProtocolHandler.class);

    private static String               authenticatedUserId;
    static Class<? extends HttpServlet> applicationLogoutServlet;


    /**
     * @param userId
     *            The userId returned by and used for the dummy OLAS services.
     */
    public static void setAuthenticatingUser(String userId) {

        authenticatedUserId = userId;
    }

    /**
     * @param logoutServlet
     *            The servlet that will handle the application-specific logout process.
     */
    public static void setLogoutServlet(Class<? extends HttpServlet> logoutServlet) {

        applicationLogoutServlet = logoutServlet;
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

        new LoginServlet().service(new MockHttpServletRequest(request, targetUrl, Method.POST), response);
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

        // OLAS -> SDK logout servlet.
        new LogoutServlet() {

            private static final long serialVersionUID = 1L;


            @Override
            protected void invokePost(HttpServletRequest sdkLogoutRequest, HttpServletResponse sdkLogoutResponse)
                    throws ServletException, IOException {

                // SDK logout servlet.
                super.invokePost(sdkLogoutRequest, sdkLogoutResponse);

                // SDK logout servlet -> App Logout servlet.
                try {
                    applicationLogoutServlet.newInstance().service(
                            new MockHttpServletRequest(sdkLogoutRequest, Method.GET), sdkLogoutResponse);

                    // App Logout servlet -> SDK logout servlet.
                    new LogoutServlet() {

                        private static final long serialVersionUID = 1L;


                        @Override
                        protected void invokeGet(HttpServletRequest sdkLogoutExitRequest,
                                HttpServletResponse sdkLogoutExitResponse) throws ServletException, IOException {

                            // This invalidates the HTTP session if requested by the app logout servlet.
                            // But Wicket's Mock servlet container doesn't properly clean up the session in this case.
                            // So we invalidate the Wicket session ourselves manually.
                            boolean sessionInvalidated = sdkLogoutExitRequest.getSession().getAttribute(
                                    INVALIDATE_SESSION) != null;

                            try {
                                super.invokeGet(sdkLogoutExitRequest, sdkLogoutExitResponse);
                            }

                            finally {
                                if (sessionInvalidated && Session.exists()) {
                                    Session.get().invalidateNow();
                                }
                            }
                        }
                    }.service(new MockHttpServletRequest(sdkLogoutRequest, Method.GET), sdkLogoutResponse);
                }

                catch (InstantiationException e) {
                    LOG.error("Couldn't instanciate the application-specific logout servlet ("
                            + applicationLogoutServlet + ")", e);
                    throw new RuntimeException(e);
                } catch (IllegalAccessException e) {
                    LOG.error("Had no access to construct or service the application-specific logout servlet ("
                            + applicationLogoutServlet + ")", e);
                    throw new RuntimeException(e);
                }
            }
        }.service(new MockHttpServletRequest(request, targetUrl, Method.POST), response);
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
