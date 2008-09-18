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
import net.link.safeonline.authentication.service.AuthenticationService;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.sdk.auth.saml2.HttpServletRequestEndpointWrapper;
import net.link.safeonline.util.servlet.AbstractInjectionServlet;
import net.link.safeonline.util.servlet.ErrorMessage;
import net.link.safeonline.util.servlet.annotation.Init;


/**
 * Logout exit servlet. Used to send out logout requests and receive logout responses.
 * 
 * @author wvdhaute
 * 
 */
public class LogoutExitServlet extends AbstractInjectionServlet {

    private static final long  serialVersionUID                 = 1L;

    public static final String PROTOCOL_ERROR_MESSAGE_ATTRIBUTE = "protocolErrorMessage";

    public static final String PROTOCOL_NAME_ATTRIBUTE          = "protocolName";

    @Init(name = "ServletEndpointUrl")
    private String             servletEndpointUrl;

    @Init(name = "ProtocolErrorUrl")
    private String             protocolErrorUrl;


    @Override
    protected void invokeGet(HttpServletRequest request, HttpServletResponse response) throws ServletException,
            IOException {

        logoutNextSsoApplication(request, response);

    }

    @Override
    protected void invokePost(HttpServletRequest request, HttpServletResponse response) throws ServletException,
            IOException {

        /**
         * Wrap the request to use the servlet endpoint url. To prevent failure when behind a reverse proxy or
         * loadbalancer when opensaml is checking the destination field.
         */
        HttpServletRequestEndpointWrapper logoutRequestWrapper = new HttpServletRequestEndpointWrapper(request,
                this.servletEndpointUrl);

        String loggedOutApplication;
        try {
            loggedOutApplication = ProtocolHandlerManager.handleLogoutResponse(logoutRequestWrapper);
        } catch (ProtocolException e) {
            redirectToErrorPage(request, response, this.protocolErrorUrl, null, new ErrorMessage(
                    PROTOCOL_NAME_ATTRIBUTE, e.getProtocolName()), new ErrorMessage(PROTOCOL_ERROR_MESSAGE_ATTRIBUTE, e
                    .getMessage()));
            return;
        }

        /*
         * Remove application from list of applications to logout from
         */
        if (null == loggedOutApplication) {
            // TODO: keep somewhere that not all apps were logged out ok so we can send a partial-logout response back
        }

        logoutNextSsoApplication(logoutRequestWrapper, response);
    }

    /**
     * Log out next sso application. If none left: send back a logout response to the requesting application.
     * 
     * @throws IOException
     */
    private void logoutNextSsoApplication(HttpServletRequest request, HttpServletResponse response) throws IOException {

        AuthenticationService authenticationService = AuthenticationServiceManager.getAuthenticationService(request
                .getSession());
        ApplicationEntity application = authenticationService.findSsoApplicationToLogout();
        if (null == application) {

            // no more apps to logout: send LogoutResponse back to requesting app

        } else {

            try {
                ProtocolHandlerManager.logoutRequest(application, request.getSession(), response);
            } catch (ProtocolException e) {
                redirectToErrorPage(request, response, this.protocolErrorUrl, null, new ErrorMessage(
                        PROTOCOL_NAME_ATTRIBUTE, e.getProtocolName()), new ErrorMessage(
                        PROTOCOL_ERROR_MESSAGE_ATTRIBUTE, e.getMessage()));
                return;

            }

        }

    }
}
