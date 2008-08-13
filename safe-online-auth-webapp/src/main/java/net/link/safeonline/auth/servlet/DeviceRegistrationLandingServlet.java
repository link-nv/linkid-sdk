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
import net.link.safeonline.authentication.exception.DeviceMappingNotFoundException;
import net.link.safeonline.authentication.exception.NodeNotFoundException;
import net.link.safeonline.authentication.service.AuthenticationService;
import net.link.safeonline.authentication.service.AuthenticationState;
import net.link.safeonline.entity.DeviceMappingEntity;
import net.link.safeonline.helpdesk.HelpdeskLogger;
import net.link.safeonline.sdk.auth.saml2.HttpServletRequestEndpointWrapper;
import net.link.safeonline.shared.helpdesk.LogLevelType;
import net.link.safeonline.util.servlet.AbstractInjectionServlet;
import net.link.safeonline.util.servlet.ErrorMessage;
import net.link.safeonline.util.servlet.annotation.Init;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Device registration landing page.
 *
 * This landing page handles the SAML response returned by the remote device issuer to notify the success of the
 * registration.
 *
 * @author wvdhaute
 *
 */
public class DeviceRegistrationLandingServlet extends AbstractInjectionServlet {

    private static final long  serialVersionUID               = 1L;

    private static final Log   LOG                            = LogFactory
                                                                      .getLog(DeviceRegistrationLandingServlet.class);

    public static final String RESOURCE_BASE                  = "messages.webapp";

    public static final String DEVICE_ERROR_MESSAGE_ATTRIBUTE = "deviceErrorMessage";

    @Init(name = "LoginUrl")
    private String             loginUrl;

    @Init(name = "RegisterDeviceUrl")
    private String             registerDeviceUrl;

    @Init(name = "NewUserDeviceUrl")
    private String             newUserDeviceUrl;

    @Init(name = "ServletEndpointUrl")
    private String             servletEndpointUrl;

    @Init(name = "DeviceErrorUrl")
    private String             deviceErrorUrl;


    @Override
    protected void invokePost(HttpServletRequest request, HttpServletResponse response) throws ServletException,
            IOException {

        LOG.debug("doPost");

        /**
         * Wrap the request to use the servlet endpoint url. To prevent failure when behind a reverse proxy or
         * loadbalancer when opensaml is checking the destination field.
         */
        HttpServletRequestEndpointWrapper requestWrapper = new HttpServletRequestEndpointWrapper(request,
                this.servletEndpointUrl);

        AuthenticationService authenticationService = AuthenticationServiceManager
                .getAuthenticationService(requestWrapper.getSession());
        DeviceMappingEntity deviceMapping;
        try {
            deviceMapping = authenticationService.register(requestWrapper);
        } catch (NodeNotFoundException e) {
            redirectToErrorPage(requestWrapper, response, this.deviceErrorUrl, RESOURCE_BASE, new ErrorMessage(
                    DEVICE_ERROR_MESSAGE_ATTRIBUTE, "errorProtocolHandlerFinalization"));
            return;
        } catch (DeviceMappingNotFoundException e) {
            redirectToErrorPage(requestWrapper, response, this.deviceErrorUrl, RESOURCE_BASE, new ErrorMessage(
                    DEVICE_ERROR_MESSAGE_ATTRIBUTE, "errorDeviceRegistrationNotFound"));
            return;
        }
        if (null == deviceMapping) {
            /*
             * Registration failed, redirect to register-device or new-user-device
             */
            HelpdeskLogger.add(requestWrapper.getSession(), "registration failed", LogLevelType.ERROR);
            if (authenticationService.getAuthenticationState().equals(AuthenticationState.USER_AUTHENTICATED)) {
                response.sendRedirect(this.registerDeviceUrl);
            } else {
                response.sendRedirect(this.newUserDeviceUrl);
            }

        } else {
            /*
             * Registration ok, redirect to login servlet
             */
            LoginManager.relogin(requestWrapper.getSession(), deviceMapping.getDevice());
            HelpdeskLogger.add(requestWrapper.getSession(), "successfully registered device: "
                    + deviceMapping.getDevice(), LogLevelType.INFO);

            response.sendRedirect(this.loginUrl);
        }
    }
}
