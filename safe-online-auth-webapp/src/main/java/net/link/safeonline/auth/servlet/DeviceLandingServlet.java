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

import net.link.safeonline.auth.LoginManager;
import net.link.safeonline.auth.protocol.AuthenticationServiceManager;
import net.link.safeonline.authentication.exception.DeviceNotFoundException;
import net.link.safeonline.authentication.exception.NodeMappingNotFoundException;
import net.link.safeonline.authentication.exception.NodeNotFoundException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.service.AuthenticationService;
import net.link.safeonline.authentication.service.AuthenticationState;
import net.link.safeonline.helpdesk.HelpdeskLogger;
import net.link.safeonline.sdk.auth.saml2.HttpServletRequestEndpointWrapper;
import net.link.safeonline.shared.helpdesk.LogLevelType;
import net.link.safeonline.util.servlet.AbstractInjectionServlet;
import net.link.safeonline.util.servlet.ErrorMessage;
import net.link.safeonline.util.servlet.annotation.Init;


/**
 * Device landing servlet. Landing page to finalize the authentication process between OLAS and a device provider.
 * 
 * @author wvdhaute
 * 
 */
public class DeviceLandingServlet extends AbstractInjectionServlet {

    private static final long  serialVersionUID               = 1L;

    public static final String RESOURCE_BASE                  = "messages.webapp";

    public static final String DEVICE_ERROR_MESSAGE_ATTRIBUTE = "deviceErrorMessage";

    @Init(name = "LoginUrl")
    private String             loginUrl;

    @Init(name = "StartUrl")
    private String             startUrl;

    @Init(name = "TryAnotherDeviceUrl")
    private String             tryAnotherDeviceUrl;

    @Init(name = "ServletEndpointUrl")
    private String             servletEndpointUrl;

    @Init(name = "DeviceErrorUrl")
    private String             deviceErrorUrl;


    @Override
    protected void invokePost(HttpServletRequest request, HttpServletResponse response) throws ServletException,
            IOException {

        /**
         * Wrap the request to use the servlet endpoint url. To prevent failure when behind a reverse proxy or
         * loadbalancer when opensaml is checking the destination field.
         */
        HttpServletRequestEndpointWrapper requestWrapper = new HttpServletRequestEndpointWrapper(request,
                this.servletEndpointUrl);

        /*
         * Authenticate
         */
        AuthenticationService authenticationService = AuthenticationServiceManager
                .getAuthenticationService(requestWrapper.getSession());
        String userId;
        try {
            userId = authenticationService.authenticate(requestWrapper);
        } catch (NodeNotFoundException e) {
            redirectToErrorPage(requestWrapper, response, this.deviceErrorUrl, RESOURCE_BASE, new ErrorMessage(
                    DEVICE_ERROR_MESSAGE_ATTRIBUTE, "errorProtocolHandlerFinalization"));
            return;
        } catch (NodeMappingNotFoundException e) {
            redirectToErrorPage(requestWrapper, response, this.deviceErrorUrl, RESOURCE_BASE, new ErrorMessage(
                    DEVICE_ERROR_MESSAGE_ATTRIBUTE, "errorDeviceRegistrationNotFound"));
            return;
        } catch (DeviceNotFoundException e) {
            redirectToErrorPage(requestWrapper, response, this.deviceErrorUrl, RESOURCE_BASE, new ErrorMessage(
                    DEVICE_ERROR_MESSAGE_ATTRIBUTE, "errorProtocolHandlerFinalization"));
            return;
        } catch (SubjectNotFoundException e) {
            redirectToErrorPage(requestWrapper, response, this.deviceErrorUrl, RESOURCE_BASE, new ErrorMessage(
                    DEVICE_ERROR_MESSAGE_ATTRIBUTE, "errorDeviceRegistrationNotFound"));
            return;
        }
        if (null == userId && authenticationService.getAuthenticationState().equals(AuthenticationState.REDIRECTED)) {
            /*
             * Authentication failed but user requested to try another device
             */
            HelpdeskLogger.add(requestWrapper.getSession(), "authentication failed, request to try another device",
                    LogLevelType.ERROR);

            response.sendRedirect(this.tryAnotherDeviceUrl);
        } else if (null == userId) {
            /*
             * Authentication failed, redirect to start page
             */
            HelpdeskLogger.add(requestWrapper.getSession(), "authentication failed", LogLevelType.ERROR);
            response.sendRedirect(this.startUrl);
        } else {
            /*
             * Authentication success, redirect to login servlet
             */
            LoginManager.login(requestWrapper.getSession(), userId, authenticationService.getAuthenticationDevice());

            HelpdeskLogger.add(requestWrapper.getSession(), "logged in successfully with device: "
                    + authenticationService.getAuthenticationDevice().getName(), LogLevelType.INFO);

            response.sendRedirect(this.loginUrl);
        }
    }
}
