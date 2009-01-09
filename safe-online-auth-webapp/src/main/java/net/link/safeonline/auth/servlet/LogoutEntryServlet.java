/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.auth.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.link.safeonline.auth.protocol.AuthenticationServiceManager;
import net.link.safeonline.auth.protocol.ProtocolException;
import net.link.safeonline.auth.protocol.ProtocolHandlerManager;
import net.link.safeonline.authentication.LogoutProtocolContext;
import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.exception.InvalidCookieException;
import net.link.safeonline.authentication.service.AuthenticationService;
import net.link.safeonline.common.SafeOnlineCookies;
import net.link.safeonline.sdk.auth.saml2.HttpServletRequestEndpointWrapper;
import net.link.safeonline.util.servlet.AbstractInjectionServlet;
import net.link.safeonline.util.servlet.ErrorMessage;
import net.link.safeonline.util.servlet.annotation.Init;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Entry point for logout requests send to the authentication web application. This servlet will try to find out which authentication
 * protocol is being used by the client web browser to initiate a single logout procedure. We manage the authentication entry via a
 * bare-bone servlet since we:
 * <ul>
 * <li>need to be able to do some low-level GET or POST parameter parsing and processing.</li>
 * <li>we want the entry point to be UI technology independent.</li>
 * </ul>
 * 
 * <p>
 * The following servlet init parameters are required:
 * </p>
 * <ul>
 * <li><code>ServletEndpointUrl</code>: used if behind proxy, load balancer ... . For example the SAML2 protocol will check the destination
 * field in the SAML logout request against the actual destination of the servlet request.
 * <li><code>UnsupportedProtocolUrl</code>: will be used to redirect to when an unsupported authentication protocol is encountered.</li>
 * <li><code>ProtocolErrorUrl</code>: will be used to redirect to when an authentication protocol error is encountered.</li>
 * </ul>
 * 
 * @author wvdhaute
 * 
 */
public class LogoutEntryServlet extends AbstractInjectionServlet {

    private static final long  serialVersionUID                 = 1L;

    private static final Log   LOG                              = LogFactory.getLog(LogoutEntryServlet.class);

    public static final String PROTOCOL_ERROR_MESSAGE_ATTRIBUTE = "protocolErrorMessage";

    public static final String PROTOCOL_NAME_ATTRIBUTE          = "protocolName";

    @Init(name = "ServletEndpointUrl")
    private String             servletEndpointUrl;

    @Init(name = "LogoutExitUrl")
    private String             logoutExitUrl;

    @Init(name = "UnsupportedProtocolUrl")
    private String             unsupportedProtocolUrl;

    @Init(name = "ProtocolErrorUrl")
    private String             protocolErrorUrl;

    @Init(name = "CookiePath")
    private String             cookiePath;


    @Override
    protected void invokeGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        handleLanding(request, response);
    }

    @Override
    protected void invokePost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        handleLanding(request, response);
    }

    private void handleLanding(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        /**
         * Wrap the request to use the servlet endpoint url. To prevent failure when behind a reverse proxy or loadbalancer when opensaml is
         * checking the destination field.
         */
        HttpServletRequestEndpointWrapper logoutRequestWrapper = new HttpServletRequestEndpointWrapper(request, servletEndpointUrl);

        LogoutProtocolContext logoutProtocolContext;
        try {
            logoutProtocolContext = ProtocolHandlerManager.handleLogoutRequest(logoutRequestWrapper);
        } catch (ProtocolException e) {
            redirectToErrorPage(request, response, protocolErrorUrl, null, new ErrorMessage(PROTOCOL_NAME_ATTRIBUTE,
                    e.getProtocolName()), new ErrorMessage(PROTOCOL_ERROR_MESSAGE_ATTRIBUTE, e.getMessage()));
            return;
        }

        if (null == logoutProtocolContext) {
            response.sendRedirect(unsupportedProtocolUrl);
            return;
        }

        /*
         * Store target to send LogoutResponse to later on
         */
        logoutRequestWrapper.getSession().setAttribute(LogoutExitServlet.LOGOUT_TARGET_ATTRIBUTE, logoutProtocolContext.getTarget());

        /*
         * Check Single Sign-On Cookies and send logout requests to the authenticated applications
         */
        AuthenticationService authenticationService = AuthenticationServiceManager
                                                                                  .getAuthenticationService(logoutRequestWrapper
                                                                                                                                .getSession());
        Cookie[] cookies = logoutRequestWrapper.getCookies();
        if (null != cookies) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().startsWith(SafeOnlineCookies.SINGLE_SIGN_ON_COOKIE_PREFIX)) {
                    try {
                        if (authenticationService.checkSsoCookieForLogout(cookie)) {
                            // If cookie has passed checks for logout, remove it, applications that need to be logged
                            // out are stored in the AuthenticationService
                            removeCookie(cookie.getName(), response);
                        }
                    } catch (ApplicationNotFoundException e) {
                        LOG.debug("Invalid SSO Cookie " + cookie.getName() + ": removing...");
                        removeCookie(cookie.getName(), response);
                    } catch (InvalidCookieException e) {
                        LOG.debug("Invalid SSO Cookie " + cookie.getName() + ": removing...");
                        removeCookie(cookie.getName(), response);
                    }
                }
            }
        }

        response.sendRedirect(logoutExitUrl);
    }

    private void removeCookie(String name, HttpServletResponse response) {

        Cookie cookie = new Cookie(name, "");
        cookie.setMaxAge(0);
        cookie.setPath(cookiePath);
        response.addCookie(cookie);
    }
}
