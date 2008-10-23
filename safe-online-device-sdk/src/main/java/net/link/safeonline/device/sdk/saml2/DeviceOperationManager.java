/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.device.sdk.saml2;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * <h2>{@link DeviceOperationManager}<br>
 * <sub>Device Operation Manager for servlet container based web applications.</sub></h2>
 * 
 * <p>
 * Device Operation Manager for servlet container based web applications. The device operation request info is saved on
 * the HTTP session.
 * </p>
 * 
 * <p>
 * <i>Oct 22, 2008</i>
 * </p>
 * 
 * @author wvdhaute
 */
public class DeviceOperationManager {

    private static final Log   LOG                                    = LogFactory.getLog(DeviceOperationManager.class);

    public static final String USERID_SESSION_ATTRIBUTE               = "userId";

    public static final String DEVICE_OPERATION_SESSION_ATTRIBUTE     = "operation";

    public static final String AUTHENTICATED_DEVICE_SESSION_ATTRIBUTE = "authenticatedDevice";

    public static final String DEVICE_ATTRIBUTE_SESSION_ATTRIBUTE     = "attribute";


    private DeviceOperationManager() {

        // empty
    }

    /**
     * Sets the userId.
     * 
     * @param userId
     * @param httpRequest
     */
    public static void setUserId(String userId, HttpServletRequest httpRequest) {

        LOG.debug("setting userId: " + userId);
        HttpSession session = httpRequest.getSession();
        session.setAttribute(USERID_SESSION_ATTRIBUTE, userId);

    }

    /**
     * Gets the userId
     * 
     * @param session
     * @throws ServletException
     */
    public static String getUserId(HttpSession session) throws ServletException {

        String userId = (String) session.getAttribute(USERID_SESSION_ATTRIBUTE);
        if (null == userId)
            throw new ServletException("userId attribute not found");

        return userId;

    }

    /**
     * Sets the authenticatedDevice.
     * 
     * @param authenticatedDevice
     * @param httpRequest
     */
    public static void setAuthenticatedDevice(String authenticatedDevice, HttpServletRequest httpRequest) {

        LOG.debug("setting authenticated device: " + authenticatedDevice);
        HttpSession session = httpRequest.getSession();
        session.setAttribute(AUTHENTICATED_DEVICE_SESSION_ATTRIBUTE, authenticatedDevice);

    }

    /**
     * Gets the authenticatedDevice
     * 
     * @param session
     * @throws ServletException
     */
    public static String getAuthenticatedDevice(HttpSession session) throws ServletException {

        String authenticatedDevice = (String) session.getAttribute(AUTHENTICATED_DEVICE_SESSION_ATTRIBUTE);
        if (null == authenticatedDevice)
            throw new ServletException("authenticatedDevice attribute not found");

        return authenticatedDevice;

    }

    /**
     * Sets the device operation.
     * 
     * @param operation
     * @param httpRequest
     */
    public static void setOperation(String operation, HttpServletRequest httpRequest) {

        LOG.debug("setting device operation: " + operation);
        HttpSession session = httpRequest.getSession();
        session.setAttribute(DEVICE_OPERATION_SESSION_ATTRIBUTE, operation);

    }

    /**
     * Gets the device operation
     * 
     * @param session
     * @throws ServletException
     */
    public static String getOperation(HttpSession session) throws ServletException {

        String operation = (String) session.getAttribute(DEVICE_OPERATION_SESSION_ATTRIBUTE);
        if (null == operation)
            throw new ServletException("device operation attribute not found");

        return operation;

    }

    /**
     * Sets the device attribute.
     * 
     * @param attribute
     * @param httpRequest
     */
    public static void setAttribute(String attribute, HttpServletRequest httpRequest) {

        LOG.debug("setting device attribute: " + attribute);
        HttpSession session = httpRequest.getSession();
        session.setAttribute(DEVICE_ATTRIBUTE_SESSION_ATTRIBUTE, attribute);

    }

    /**
     * Gets the device attribute
     * 
     * @param session
     * @throws ServletException
     */
    public static String getAttribute(HttpSession session) throws ServletException {

        String attribute = findAttribute(session);
        if (null == attribute)
            throw new ServletException("device attribute attribute not found");

        return attribute;

    }

    /**
     * Finds the device attribute. If not present returns null.
     * 
     * @param session
     */
    public static String findAttribute(HttpSession session) {

        return (String) session.getAttribute(DEVICE_ATTRIBUTE_SESSION_ATTRIBUTE);
    }

}
