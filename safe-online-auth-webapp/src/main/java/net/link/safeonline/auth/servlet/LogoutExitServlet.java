/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.auth.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.link.safeonline.auth.protocol.LogoutServiceManager;
import net.link.safeonline.auth.protocol.ProtocolException;
import net.link.safeonline.auth.protocol.ProtocolHandlerManager;
import net.link.safeonline.auth.webapp.pages.AuthenticationProtocolErrorPage;
import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.service.LogoutService;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.model.node.util.AbstractNodeInjectionServlet;
import net.link.safeonline.util.servlet.ErrorMessage;
import net.link.safeonline.util.servlet.ServletUtils;
import net.link.safeonline.util.servlet.annotation.RequestParameter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Logout exit servlet. Used to send out logout requests and receive logout responses.
 * 
 * @author wvdhaute
 * 
 */
public class LogoutExitServlet extends AbstractNodeInjectionServlet {

    /**
     * The GET parameter that holds the {@link ApplicationEntity} ID of the application that we should generate a logout request for and
     * redirect to.
     */
    public static final String APPLICATION_ID_GET_PARAMETER = "id";

    private static final long  serialVersionUID             = 1L;

    private static final Log   LOG                          = LogFactory.getLog(LogoutExitServlet.class);

    public static final String PATH_CONTEXT_PARAM           = "LogoutExitPath";
    public static final String LOGOUT_TARGET_ATTRIBUTE      = "Logout.target";

    @RequestParameter(APPLICATION_ID_GET_PARAMETER)
    String                     applicationId;


    @Override
    protected void invokeGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // If an applicationId is given as a GET parameter, send logout request to that application.
        if (applicationId != null) {
            try {
                LogoutService logoutService = LogoutServiceManager.getLogoutService(request.getSession());

                synchronized (request.getSession()) {
                    ApplicationEntity application = logoutService.getSsoApplicationToLogout(Long.parseLong(applicationId));
                    logoutApplication(request, response, application);
                }

                return;
            }

            catch (NumberFormatException e) {
                LOG.error("Application ID was not a valid long: " + applicationId, e);
            } catch (ApplicationNotFoundException e) {
                LOG.error("Application for application ID not found: " + applicationId, e);
            }
        }

        // If that failed, or no applicationId was specified, fall back to a sequential logout.
        logoutNextSsoApplication(request, response);
    }

    @Override
    protected void invokePost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        LOG.debug("handle logout response");
        try {
            synchronized (request.getSession()) {
                ProtocolHandlerManager.handleLogoutResponse(request);
            }
        } catch (ProtocolException e) {
            ServletUtils.redirectToErrorPage(request, response, AuthenticationProtocolErrorPage.PATH, null, new ErrorMessage(
                    AuthenticationProtocolErrorPage.PROTOCOL_NAME_ATTRIBUTE, e.getProtocolName()), new ErrorMessage(
                    AuthenticationProtocolErrorPage.PROTOCOL_ERROR_MESSAGE_ATTRIBUTE, e.getMessage()));
            return;
        }

        // TODO: Only continue sequential logout if we're not doing parallel logout.
        // logoutNextSsoApplication(request, response);
    }

    /**
     * Log out next SSO application.
     */
    private void logoutNextSsoApplication(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        LogoutService logoutService = LogoutServiceManager.getLogoutService(request.getSession());
        ApplicationEntity nextApplication = logoutService.findSsoApplicationToLogout();
        if (null == nextApplication) {
            logoutComplete(request, response);

        } else {
            logoutApplication(request, response, nextApplication);

        }

    }

    private void logoutApplication(HttpServletRequest request, HttpServletResponse response, ApplicationEntity application)
            throws IOException {

        LOG.debug("send logout request to: " + application.getName());
        try {
            synchronized (request.getSession()) {
                ProtocolHandlerManager.sendLogoutRequest(application, request.getSession(), response);
            }
        }

        catch (ProtocolException e) {
            ServletUtils.redirectToErrorPage(request, response, AuthenticationProtocolErrorPage.PATH, null, new ErrorMessage(
                    AuthenticationProtocolErrorPage.PROTOCOL_NAME_ATTRIBUTE, e.getProtocolName()), new ErrorMessage(
                    AuthenticationProtocolErrorPage.PROTOCOL_ERROR_MESSAGE_ATTRIBUTE, e.getMessage()));
        }
    }

    /**
     * No SSO applications left to logout: send back a logout response to the requesting application.
     */
    public static void logoutComplete(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        // no more applications to logout: send LogoutResponse back to requesting application
        String target = (String) request.getSession().getAttribute(LOGOUT_TARGET_ATTRIBUTE);
        if (null == target)
            throw new IllegalStateException(LOGOUT_TARGET_ATTRIBUTE + " session attribute not present");

        LOG.debug("send logout response to " + target);

        try {
            synchronized (request.getSession()) {
                ProtocolHandlerManager.sendLogoutResponse(target, request.getSession(), response);
            }
        } catch (ProtocolException e) {
            ServletUtils.redirectToErrorPage(request, response, AuthenticationProtocolErrorPage.PATH, null, new ErrorMessage(
                    AuthenticationProtocolErrorPage.PROTOCOL_NAME_ATTRIBUTE, e.getProtocolName()), new ErrorMessage(
                    AuthenticationProtocolErrorPage.PROTOCOL_ERROR_MESSAGE_ATTRIBUTE, e.getMessage()));
            return;
        }
    }
}
