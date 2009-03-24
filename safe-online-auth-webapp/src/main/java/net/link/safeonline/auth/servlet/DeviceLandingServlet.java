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
import net.link.safeonline.sdk.auth.saml2.HttpServletRequestEndpointWrapper;
import net.link.safeonline.shared.helpdesk.LogLevelType;
import net.link.safeonline.util.servlet.AbstractInjectionServlet;
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
public class DeviceLandingServlet extends AbstractInjectionServlet {

    private static final long  serialVersionUID               = 1L;

    private static final Log   LOG                            = LogFactory.getLog(DeviceLandingServlet.class);

    public static final String RESOURCE_BASE                  = "messages.webapp";

    public static final String DEVICE_ERROR_MESSAGE_ATTRIBUTE = "deviceErrorMessage";

    @Init(name = "LoginUrl")
    private String             loginUrl;

    @Init(name = "TryAnotherDeviceUrl")
    private String             tryAnotherDeviceUrl;

    @Init(name = "ServletEndpointUrl")
    private String             servletEndpointUrl;

    @Init(name = "DeviceErrorUrl")
    private String             deviceErrorUrl;


    @Override
    protected void invokePost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        /**
         * Wrap the request to use the servlet endpoint url. To prevent failure when behind a reverse proxy or loadbalancer when opensaml is
         * checking the destination field.
         */
        HttpServletRequestEndpointWrapper requestWrapper = new HttpServletRequestEndpointWrapper(request, servletEndpointUrl);

        /**
         * Authenticate
         */
        String userId;
        try {
            userId = ProtocolHandlerManager.handleDeviceAuthnResponse(requestWrapper);
        } catch (ProtocolException e) {
            redirectToErrorPage(requestWrapper, response, deviceErrorUrl, RESOURCE_BASE, new ErrorMessage(DEVICE_ERROR_MESSAGE_ATTRIBUTE,
                    e.getMessage()));
            return;
        }

        AuthenticationService authenticationService = AuthenticationServiceManager.getAuthenticationService(requestWrapper.getSession());
        if (null == userId && authenticationService.getAuthenticationState().equals(AuthenticationState.REDIRECTED)) {
            /*
             * Authentication failed but user requested to try another device
             */
            HelpdeskLogger.add(requestWrapper.getSession(), "authentication failed, request to try another device", LogLevelType.ERROR);

            response.sendRedirect(tryAnotherDeviceUrl);
        } else if (null == userId) {
            /*
             * Authentication failed, redirect to start page
             */
            HelpdeskLogger.add(requestWrapper.getSession(), "authentication failed", LogLevelType.ERROR);
            String requestUrl = (String) requestWrapper.getSession().getAttribute(AuthenticationUtils.REQUEST_URL_INIT_PARAM);
            LOG.debug("requestUrl: " + requestUrl);
            response.sendRedirect(requestUrl);
        } else {
            /*
             * Authentication success, redirect to login servlet
             */
            LoginManager.login(requestWrapper.getSession(), userId, authenticationService.getAuthenticationDevice());

            HelpdeskLogger.add(requestWrapper.getSession(), "logged in successfully with device: "
                    + authenticationService.getAuthenticationDevice().getName(), LogLevelType.INFO);

            /*
             * Set SSO Cookie
             */
            if (null != authenticationService.getSsoCookie()) {
                response.addCookie(authenticationService.getSsoCookie());
            }

            response.sendRedirect(loginUrl);
        }
    }
}
