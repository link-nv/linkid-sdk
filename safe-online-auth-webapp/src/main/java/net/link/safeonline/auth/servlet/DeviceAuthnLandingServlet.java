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

import net.link.safeonline.auth.AuthenticationUtils;
import net.link.safeonline.auth.LoginManager;
import net.link.safeonline.auth.protocol.AuthenticationServiceManager;
import net.link.safeonline.auth.protocol.ProtocolException;
import net.link.safeonline.auth.protocol.ProtocolHandlerManager;
import net.link.safeonline.authentication.service.AuthenticationService;
import net.link.safeonline.authentication.service.AuthenticationState;
import net.link.safeonline.helpdesk.HelpdeskLogger;
import net.link.safeonline.model.node.util.AbstractNodeInjectionServlet;
import net.link.safeonline.shared.helpdesk.LogLevelType;
import net.link.safeonline.util.servlet.ErrorMessage;
import net.link.safeonline.util.servlet.annotation.Init;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Device landing servlet. Landing page to finalize the authentication process between OLAS and a device provider.
 * 
 * @author wvdhaute
 * 
 */
public class DeviceAuthnLandingServlet extends AbstractNodeInjectionServlet {

    private static final Log   LOG                            = LogFactory.getLog(DeviceAuthnLandingServlet.class);
    private static final long  serialVersionUID               = 1L;

    /**
     * PATH within the authentication web application where the authentication pipeline continues after the user has been successfully
     * logged in. <i>[required]</i>
     */
    public static final String LOGIN_PATH                     = "LoginPath";

    /**
     * PATH within the authentication web application where user can choose another device to authenticate with. <i>[required]</i>
     */
    public static final String TRY_ANOTHER_DEVICE_PATH        = "TryAnotherDevicePath";

    /**
     * PATH within the authentication web application to redirect to when a protocol error occurs. <i>[required]</i>
     */
    public static final String DEVICE_ERROR_PATH              = "DeviceErrorPath";

    public static final String RESOURCE_BASE                  = "messages.webapp";

    public static final String DEVICE_ERROR_MESSAGE_ATTRIBUTE = "deviceErrorMessage";

    @Init(name = LOGIN_PATH)
    private String             loginPath;

    @Init(name = TRY_ANOTHER_DEVICE_PATH)
    private String             tryAnotherDevicePath;

    @Init(name = DEVICE_ERROR_PATH)
    private String             deviceErrorPath;


    @Override
    protected void invokePost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        /**
         * Authenticate
         */
        String userId;
        try {
            userId = ProtocolHandlerManager.handleDeviceAuthnResponse(request);
        } catch (ProtocolException e) {
            redirectToErrorPage(request, response, deviceErrorPath, RESOURCE_BASE, new ErrorMessage(DEVICE_ERROR_MESSAGE_ATTRIBUTE,
                    e.getMessage()));
            return;
        }

        AuthenticationService authenticationService = AuthenticationServiceManager.getAuthenticationService(request.getSession());
        if (null == userId && authenticationService.getAuthenticationState().equals(AuthenticationState.REDIRECTED)) {
            /*
             * Authentication failed but user requested to try another device
             */
            HelpdeskLogger.add(request.getSession(), "authentication failed, request to try another device", LogLevelType.ERROR);

            response.sendRedirect(tryAnotherDevicePath);
        } else if (null == userId) {
            /*
             * Authentication failed, redirect to start page
             */
            HelpdeskLogger.add(request.getSession(), "authentication failed", LogLevelType.ERROR);
            String requestUrl = (String) request.getSession().getAttribute(AuthenticationUtils.REQUEST_URL_SESSION_ATTRIBUTE);
            LOG.debug("requestUrl: " + requestUrl);
            response.sendRedirect(requestUrl);
        } else {
            /*
             * Authentication success, redirect to login servlet
             */
            LoginManager.login(request.getSession(), userId, authenticationService.getAuthenticationDevice());

            HelpdeskLogger.add(request.getSession(), "logged in successfully with device: "
                    + authenticationService.getAuthenticationDevice().getName(), LogLevelType.INFO);

            /*
             * Set SSO Cookie
             */
            if (null != authenticationService.getSsoCookie()) {
                response.addCookie(authenticationService.getSsoCookie());
            }

            response.sendRedirect(loginPath);
        }
    }
}
