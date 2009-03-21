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

import net.link.safeonline.auth.protocol.AuthenticationServiceManager;
import net.link.safeonline.auth.protocol.ProtocolException;
import net.link.safeonline.auth.protocol.ProtocolHandlerManager;
import net.link.safeonline.auth.webapp.AuthenticationProtocolErrorPage;
import net.link.safeonline.authentication.service.AuthenticationService;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.model.node.util.AbstractNodeInjectionServlet;
import net.link.safeonline.util.servlet.ErrorMessage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Logout exit servlet. Used to send out logout requests and receive logout responses.
 * 
 * @author wvdhaute
 * 
 */
public class LogoutExitServlet extends AbstractNodeInjectionServlet {

    private static final long  serialVersionUID         = 1L;

    private static final Log   LOG                      = LogFactory.getLog(LogoutExitServlet.class);

    public static final String LOGOUT_PARTIAL_ATTRIBUTE = "Logout.partial";

    public static final String LOGOUT_TARGET_ATTRIBUTE  = "Logout.target";


    @Override
    protected void invokeGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        logoutNextSsoApplication(request, response);

    }

    @Override
    protected void invokePost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        LOG.debug("handle logout response");
        String loggedOutApplication;
        try {
            loggedOutApplication = ProtocolHandlerManager.handleLogoutResponse(request);
        } catch (ProtocolException e) {
            redirectToErrorPage(request, response, AuthenticationProtocolErrorPage.PATH, null, new ErrorMessage(
                    AuthenticationProtocolErrorPage.PROTOCOL_NAME_ATTRIBUTE, e.getProtocolName()), new ErrorMessage(
                    AuthenticationProtocolErrorPage.PROTOCOL_ERROR_MESSAGE_ATTRIBUTE, e.getMessage()));
            return;
        }

        if (null == loggedOutApplication) {
            request.getSession().setAttribute(LOGOUT_PARTIAL_ATTRIBUTE, "true");
        }

        logoutNextSsoApplication(request, response);
    }

    /**
     * Log out next sso application. If none left: send back a logout response to the requesting application.
     * 
     * @throws IOException
     */
    private void logoutNextSsoApplication(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        AuthenticationService authenticationService = AuthenticationServiceManager.getAuthenticationService(request.getSession());
        ApplicationEntity application = authenticationService.findSsoApplicationToLogout();
        if (null == application) {

            // no more applications to logout: send LogoutResponse back to requesting application
            boolean partialLogout = false;
            if (null != request.getSession().getAttribute(LOGOUT_PARTIAL_ATTRIBUTE)) {
                partialLogout = true;
            }
            String target = (String) request.getSession().getAttribute(LOGOUT_TARGET_ATTRIBUTE);
            if (null == target)
                throw new IllegalStateException(LOGOUT_TARGET_ATTRIBUTE + " session attribute not present");

            LOG.debug("send logout response to " + target + " (partialLogout=" + partialLogout + ")");

            try {
                ProtocolHandlerManager.logoutResponse(partialLogout, target, request.getSession(), response);
            } catch (ProtocolException e) {
                redirectToErrorPage(request, response, AuthenticationProtocolErrorPage.PATH, null, new ErrorMessage(
                        AuthenticationProtocolErrorPage.PROTOCOL_NAME_ATTRIBUTE, e.getProtocolName()), new ErrorMessage(
                        AuthenticationProtocolErrorPage.PROTOCOL_ERROR_MESSAGE_ATTRIBUTE, e.getMessage()));
                return;
            }

        } else {

            LOG.debug("send logout request to: " + application.getName());
            try {
                ProtocolHandlerManager.logoutRequest(application, request.getSession(), response);
            } catch (ProtocolException e) {
                redirectToErrorPage(request, response, AuthenticationProtocolErrorPage.PATH, null, new ErrorMessage(
                        AuthenticationProtocolErrorPage.PROTOCOL_NAME_ATTRIBUTE, e.getProtocolName()), new ErrorMessage(
                        AuthenticationProtocolErrorPage.PROTOCOL_ERROR_MESSAGE_ATTRIBUTE, e.getMessage()));
                return;
            }

        }

    }
}
