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

import net.link.safeonline.auth.LoginManager;
import net.link.safeonline.auth.protocol.AuthenticationServiceManager;
import net.link.safeonline.authentication.exception.DeviceNotFoundException;
import net.link.safeonline.authentication.exception.NodeMappingNotFoundException;
import net.link.safeonline.authentication.exception.NodeNotFoundException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
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
 * Device registration landing page.
 * 
 * This landing page handles the SAML response returned by the remote device issuer to notify the success of the registration.
 * 
 * @author wvdhaute
 * 
 */
public class DeviceRegistrationLandingServlet extends AbstractNodeInjectionServlet {

    /**
     * PATH within the authentication web application where the authentication pipeline continues after the user has been successfully
     * logged in. <i>[required]</i>
     */
    public static final String LOGIN_PATH                     = "LoginPath";

    /**
     * 
     */
    public static final String REGISTER_DEVICE_PATH           = "RegisterDevicePath";

    /**
     * 
     */
    public static final String NEW_USER_DEVICE_PATH           = "NewUserDevicePath";

    /**
     * PATH within the authentication web application to redirect to when a protocol error occurs. <i>[required]</i>
     */
    public static final String DEVICE_ERROR_PATH              = "DeviceErrorPath";

    private static final long  serialVersionUID               = 1L;

    private static final Log   LOG                            = LogFactory.getLog(DeviceRegistrationLandingServlet.class);

    public static final String RESOURCE_BASE                  = "messages.webapp";

    public static final String DEVICE_ERROR_MESSAGE_ATTRIBUTE = "deviceErrorMessage";

    @Init(name = LOGIN_PATH)
    private String             loginPath;

    @Init(name = REGISTER_DEVICE_PATH)
    private String             registerDevicePath;

    @Init(name = NEW_USER_DEVICE_PATH)
    private String             newUserDevicePath;

    @Init(name = DEVICE_ERROR_PATH)
    private String             deviceErrorPath;


    @Override
    protected void invokePost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        LOG.debug("doPost");

        AuthenticationService authenticationService = AuthenticationServiceManager.getAuthenticationService(request.getSession());
        String userId;
        try {
            userId = authenticationService.register(request);
        } catch (NodeNotFoundException e) {
            redirectToErrorPage(request, response, deviceErrorPath, RESOURCE_BASE, new ErrorMessage(DEVICE_ERROR_MESSAGE_ATTRIBUTE,
                    "errorProtocolHandlerFinalization"));
            return;
        } catch (NodeMappingNotFoundException e) {
            redirectToErrorPage(request, response, deviceErrorPath, RESOURCE_BASE, new ErrorMessage(DEVICE_ERROR_MESSAGE_ATTRIBUTE,
                    "errorDeviceRegistrationNotFound"));
            return;
        } catch (DeviceNotFoundException e) {
            redirectToErrorPage(request, response, deviceErrorPath, RESOURCE_BASE, new ErrorMessage(DEVICE_ERROR_MESSAGE_ATTRIBUTE,
                    "errorProtocolHandlerFinalization"));
            return;
        } catch (SubjectNotFoundException e) {
            redirectToErrorPage(request, response, deviceErrorPath, RESOURCE_BASE, new ErrorMessage(DEVICE_ERROR_MESSAGE_ATTRIBUTE,
                    "errorProtocolHandlerFinalization"));
            return;
        }
        if (null == userId) {
            /* Registration failed, redirect to register-device or new-user-device */
            HelpdeskLogger.add(request.getSession(), "registration failed", LogLevelType.ERROR);
            if (authenticationService.getAuthenticationState().equals(AuthenticationState.USER_AUTHENTICATED)) {
                response.sendRedirect(registerDevicePath);
            } else {
                response.sendRedirect(newUserDevicePath);
            }

        } else {
            /* Registration OK, redirect to login servlet */
            LoginManager.relogin(request.getSession(), authenticationService.getAuthenticationDevice());
            HelpdeskLogger.add(request.getSession(), "successfully registered device: " + authenticationService.getAuthenticationDevice(),
                    LogLevelType.INFO);

            /* Set SSO Cookie */
            Cookie ssoCookie = authenticationService.getSsoCookie();
            if (null != ssoCookie) {
                response.addCookie(ssoCookie);
            }

            response.sendRedirect(loginPath);
        }
    }
}
