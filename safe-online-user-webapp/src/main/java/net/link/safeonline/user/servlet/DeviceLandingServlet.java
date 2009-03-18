/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.user.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.link.safeonline.authentication.exception.DeviceNotFoundException;
import net.link.safeonline.authentication.exception.NodeMappingNotFoundException;
import net.link.safeonline.authentication.exception.NodeNotFoundException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.service.DeviceOperationService;
import net.link.safeonline.util.servlet.AbstractInjectionServlet;
import net.link.safeonline.util.servlet.ErrorMessage;
import net.link.safeonline.util.servlet.annotation.Init;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Device registration landing page.
 * 
 * This landing servlet handles the SAML requests sent out by an external device provider, and sends back a response containing the UUID for
 * the registrating OLAS subject for this device. This landing is used for registration, updating and removal.
 * 
 * @author wvdhaute
 * 
 */
public class DeviceLandingServlet extends AbstractInjectionServlet {

    private static final long  serialVersionUID               = 1L;

    private static final Log   LOG                            = LogFactory.getLog(DeviceLandingServlet.class);

    public static final String DEVICE_ERROR_MESSAGE_ATTRIBUTE = "deviceErrorMessage";

    @Init(name = "DevicesPage")
    private String             devicesPage;

    @Init(name = "ErrorPage", optional = true)
    private String             errorPage;

    @Init(name = "ResourceBundle", optional = true)
    private String             resourceBundleName;


    @Override
    protected void invokePost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        LOG.debug("doPost");

        DeviceOperationService deviceOperationService = (DeviceOperationService) request.getSession().getAttribute(
                DeviceOperationService.DEVICE_OPERATION_SERVICE_ATTRIBUTE);
        if (null == deviceOperationService) {
            redirectToErrorPage(request, response, errorPage, resourceBundleName, new ErrorMessage(DEVICE_ERROR_MESSAGE_ATTRIBUTE,
                    "errorProtocolHandlerFinalization"));
            return;
        }

        try {
            deviceOperationService.finalize(request);
        } catch (NodeNotFoundException e) {
            redirectToErrorPage(request, response, errorPage, resourceBundleName, new ErrorMessage(DEVICE_ERROR_MESSAGE_ATTRIBUTE,
                    "errorProtocolHandlerFinalization"));
            return;
        } catch (NodeMappingNotFoundException e) {
            redirectToErrorPage(request, response, errorPage, resourceBundleName, new ErrorMessage(DEVICE_ERROR_MESSAGE_ATTRIBUTE,
                    "errorDeviceRegistrationNotFound"));
            return;
        } catch (DeviceNotFoundException e) {
            redirectToErrorPage(request, response, errorPage, resourceBundleName, new ErrorMessage(DEVICE_ERROR_MESSAGE_ATTRIBUTE,
                    "errorProtocolHandlerFinalization"));
            return;
        } catch (SubjectNotFoundException e) {
            redirectToErrorPage(request, response, errorPage, resourceBundleName, new ErrorMessage(DEVICE_ERROR_MESSAGE_ATTRIBUTE,
                    "errorProtocolHandlerFinalization"));
            return;
        }

        // remove the device operation service from the HttpSession
        request.getSession().removeAttribute(DeviceOperationService.DEVICE_OPERATION_SERVICE_ATTRIBUTE);

        response.sendRedirect(devicesPage);
    }
}
