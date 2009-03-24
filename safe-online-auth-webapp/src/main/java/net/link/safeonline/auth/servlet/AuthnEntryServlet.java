/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.auth.servlet;

import java.io.IOException;
import java.util.Locale;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.link.safeonline.auth.LoginManager;
import net.link.safeonline.auth.protocol.AuthenticationServiceManager;
import net.link.safeonline.auth.protocol.ProtocolException;
import net.link.safeonline.auth.protocol.ProtocolHandlerManager;
import net.link.safeonline.auth.webapp.pages.AuthenticationProtocolErrorPage;
import net.link.safeonline.auth.webapp.pages.FirstTimePage;
import net.link.safeonline.auth.webapp.pages.MainPage;
import net.link.safeonline.auth.webapp.pages.UnsupportedProtocolPage;
import net.link.safeonline.authentication.ProtocolContext;
import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.exception.DevicePolicyException;
import net.link.safeonline.authentication.exception.EmptyDevicePolicyException;
import net.link.safeonline.authentication.exception.InvalidCookieException;
import net.link.safeonline.authentication.service.AuthenticationService;
import net.link.safeonline.common.SafeOnlineAppConstants;
import net.link.safeonline.common.SafeOnlineCookies;
import net.link.safeonline.helpdesk.HelpdeskLogger;
import net.link.safeonline.model.node.util.AbstractNodeInjectionServlet;
import net.link.safeonline.util.servlet.ErrorMessage;
import net.link.safeonline.util.servlet.annotation.Init;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Generic entry point for the authentication web application. This servlet will try to find out which authentication protocol is being used
 * by the client web browser to initiate an authentication procedure. We manage the authentication entry via a bare-bone servlet since we:
 * <ul>
 * <li>need to be able to do some low-level GET or POST parameter parsing and processing.</li>
 * <li>we want the entry point to be UI technology independent.</li>
 * </ul>
 * 
 * <p>
 * The following servlet init parameters are required:
 * </p>
 * <ul>
 * <li><code>StartUrl</code>: points to the relative/absolute URL to which this servlet will redirect after successful authentication
 * protocol entry.</li>
 * <li><code>FirstTimeUrl</code>: points to the relative/absolute URL to which this servlet will redirect after first visit and successful
 * authentication protocol entry.</li>
 * <li><code>UnsupportedProtocolUrl</code>: will be used to redirect to when an unsupported authentication protocol is encountered.</li>
 * <li><code>ProtocolErrorUrl</code>: will be used to redirect to when an authentication protocol error is encountered.</li>
 * </ul>
 * 
 * @author fcorneli
 * 
 */
public class AuthnEntryServlet extends AbstractNodeInjectionServlet {

    public static final String LOGIN_PATH       = "LoginPath";
    public static final String COOKIE_PATH      = "CookiePath";
    public static final String SERVLET_PATH     = "entry";

    private static final long  serialVersionUID = 1L;

    private static final Log   LOG              = LogFactory.getLog(AuthnEntryServlet.class);

    @Init(name = LOGIN_PATH)
    private String             loginPath;

    @Init(name = COOKIE_PATH)
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
            throws ServletException, IOException {

        // Create a new session (invalidate an old one, if there is one).
        HttpSession session = restartSession(request);

        ProtocolContext protocolContext;
        try {
            protocolContext = ProtocolHandlerManager.handleRequest(request);
        } catch (ProtocolException e) {
            redirectToErrorPage(request, response, AuthenticationProtocolErrorPage.PATH, null, new ErrorMessage(
                    AuthenticationProtocolErrorPage.PROTOCOL_NAME_ATTRIBUTE, e.getProtocolName()), new ErrorMessage(
                    AuthenticationProtocolErrorPage.PROTOCOL_ERROR_MESSAGE_ATTRIBUTE, e.getMessage()));
            return;
        }

        if (null == protocolContext) {
            response.sendRedirect(UnsupportedProtocolPage.PATH);
            return;
        }

        /*
         * Set the language cookie if language was specified in the browser post
         */
        Locale language = protocolContext.getLanguage();
        Integer color = protocolContext.getColor();
        Boolean minimal = protocolContext.getMinimal();

        if (null != language) {
            Cookie authLanguageCookie = new Cookie(SafeOnlineCookies.AUTH_LANGUAGE_COOKIE, language.getLanguage());
            authLanguageCookie.setPath(cookiePath);
            authLanguageCookie.setMaxAge(60 * 60 * 24 * 30 * 6);
            response.addCookie(authLanguageCookie);
        }
        if (null != minimal && minimal) {
            session.setAttribute(SafeOnlineAppConstants.COLOR_SESSION_ATTRIBUTE, color);
            session.setAttribute(SafeOnlineAppConstants.MINIMAL_SESSION_ATTRIBUTE, minimal);
        }

        /*
         * We save the result of the protocol handler into the HTTP session.
         */
        LoginManager.setApplicationId(session, protocolContext.getApplicationId());
        LoginManager.setApplicationFriendlyName(session, protocolContext.getApplicationFriendlyName());
        LoginManager.setTarget(session, protocolContext.getTarget());
        LoginManager.setRequiredDevices(session, protocolContext.getRequiredDevices());

        ProtocolContext.setProtocolContext(protocolContext, session);

        /*
         * Set application cookie in case of a timeout to know where to redirect to
         */
        Cookie applicationCookie = new Cookie(SafeOnlineCookies.APPLICATION_COOKIE, new Long(protocolContext.getApplicationId()).toString());
        applicationCookie.setPath(cookiePath);
        response.addCookie(applicationCookie);

        /*
         * create new helpdesk volatile context
         */
        HelpdeskLogger.clear(session);

        if (isFirstTime(request, response)) {
            response.sendRedirect(FirstTimePage.PATH);
            return;
        }

        /*
         * Check Single Sign-On
         */
        AuthenticationService authenticationService = AuthenticationServiceManager.getAuthenticationService(request.getSession());
        Cookie[] cookies = request.getCookies();
        boolean validSso = false;
        if (null != cookies) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().startsWith(SafeOnlineCookies.SINGLE_SIGN_ON_COOKIE_PREFIX)) {
                    try {
                        if (authenticationService.checkSsoCookie(cookie)) {
                            // Valid Single Sign-On, cookie needs update as this application is added to list of single
                            // sign-on applications
                            validSso = true;
                            cookie.setValue(authenticationService.getSsoCookie().getValue());
                            cookie.setPath(cookiePath);
                            response.addCookie(cookie);
                        }
                    } catch (ApplicationNotFoundException e) {
                        LOG.debug("Invalid SSO Cookie " + cookie.getName() + ": removing...");
                        removeCookie(cookie.getName(), response);
                    } catch (InvalidCookieException e) {
                        LOG.debug("Invalid SSO Cookie " + cookie.getName() + ": removing...");
                        removeCookie(cookie.getName(), response);
                    } catch (EmptyDevicePolicyException e) {
                        LOG.debug("Invalid SSO Cookie " + cookie.getName() + ": removing...");
                        removeCookie(cookie.getName(), response);
                    } catch (DevicePolicyException e) {
                        LOG.debug("Invalid SSO Cookie " + cookie.getName() + ": removing...");
                        removeCookie(cookie.getName(), response);
                    }
                }
            }
        }
        if (validSso) {
            LoginManager.login(request.getSession(), authenticationService.getUserId(), authenticationService.getAuthenticationDevice());
            response.sendRedirect(loginPath);
            return;
        }

        response.sendRedirect(MainPage.PATH);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected HttpSession restartSession(HttpServletRequest request)
            throws ServletException {

        HttpSession session = super.restartSession(request);
        AuthenticationServiceManager.bindAuthenticationService(session);

        return session;
    }

    private void removeCookie(String name, HttpServletResponse response) {

        Cookie cookie = new Cookie(name, "");
        cookie.setMaxAge(0);
        cookie.setPath(cookiePath);
        response.addCookie(cookie);
    }

    private boolean isFirstTime(HttpServletRequest request, HttpServletResponse response) {

        Cookie[] cookies = request.getCookies();
        if (null == cookies) {
            setDefloweredCookie(response);
            return true;
        }
        Cookie defloweredCookie = findDefloweredCookie(cookies);
        if (null == defloweredCookie) {
            setDefloweredCookie(response);
            return true;
        }
        return false;
    }

    private Cookie findDefloweredCookie(Cookie[] cookies) {

        for (Cookie cookie : cookies) {
            if (SafeOnlineCookies.DEFLOWERED_COOKIE.equals(cookie.getName()))
                return cookie;
        }
        return null;
    }

    private void setDefloweredCookie(HttpServletResponse response) {

        Cookie defloweredCookie = new Cookie(SafeOnlineCookies.DEFLOWERED_COOKIE, "true");
        defloweredCookie.setMaxAge(60 * 60 * 24 * 30 * 6);
        defloweredCookie.setPath(cookiePath);
        response.addCookie(defloweredCookie);
    }
}
