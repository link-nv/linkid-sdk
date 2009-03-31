/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.device.sdk.operation.saml2;

import java.util.List;

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
 * Device Operation Manager for servlet container based web applications. The device operation request info is saved on the HTTP session.
 * </p>
 * 
 * <p>
 * <i>Oct 22, 2008</i>
 * </p>
 * 
 * @author wvdhaute
 */
public class DeviceOperationManager {

    private static final Log   LOG                                     = LogFactory.getLog(DeviceOperationManager.class);

    public static final String USERID_SESSION_ATTRIBUTE                = "userId";

    public static final String DEVICE_OPERATION_SESSION_ATTRIBUTE      = "operation";

    public static final String AUTHENTICATED_DEVICES_SESSION_ATTRIBUTE = "authenticatedDevices";

    public static final String DEVICE_ATTRIBUTE_ID_SESSION_ATTRIBUTE   = "attributeId";


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
    public static String getUserId(HttpSession session)
            throws ServletException {

        String userId = (String) session.getAttribute(USERID_SESSION_ATTRIBUTE);
        if (null == userId)
            throw new ServletException("userId attribute not found");

        return userId;

    }

    /**
     * Sets the authenticatedDevices.
     * 
     * @param authenticatedDevices
     * @param httpRequest
     */
    public static void setAuthenticatedDevices(List<String> authenticatedDevices, HttpServletRequest httpRequest) {

        LOG.debug("setting authenticated devices: " + authenticatedDevices);
        HttpSession session = httpRequest.getSession();
        session.setAttribute(AUTHENTICATED_DEVICES_SESSION_ATTRIBUTE, authenticatedDevices);

    }

    /**
     * Gets the authenticatedDevices
     * 
     * @param session
     * @throws ServletException
     */
    @SuppressWarnings("unchecked")
    public static List<String> getAuthenticatedDevice(HttpSession session)
            throws ServletException {

        List<String> authenticatedDevices = (List<String>) session.getAttribute(AUTHENTICATED_DEVICES_SESSION_ATTRIBUTE);
        if (null == authenticatedDevices)
            throw new ServletException("authenticatedDevices attribute not found");

        return authenticatedDevices;

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
    public static String getOperation(HttpSession session)
            throws ServletException {

        String operation = (String) session.getAttribute(DEVICE_OPERATION_SESSION_ATTRIBUTE);
        if (null == operation)
            throw new ServletException("device operation attribute not found");

        return operation;

    }

    /**
     * Sets the device attribute id.
     * 
     * @param attributeId
     * @param httpRequest
     */
    public static void setAttributeId(String attributeId, HttpServletRequest httpRequest) {

        LOG.debug("setting device attribute id: " + attributeId);
        HttpSession session = httpRequest.getSession();
        session.setAttribute(DEVICE_ATTRIBUTE_ID_SESSION_ATTRIBUTE, attributeId);

    }

    /**
     * Gets the device attribute id.
     * 
     * @param session
     * @throws ServletException
     */
    public static String getAttributeId(HttpSession session)
            throws ServletException {

        String attribute = findAttributeId(session);
        if (null == attribute)
            throw new ServletException("device attribute " + DEVICE_ATTRIBUTE_ID_SESSION_ATTRIBUTE + " not found");

        return attribute;

    }

    /**
     * Finds the device attribute id. If not present returns null.
     * 
     * @param session
     */
    public static String findAttributeId(HttpSession session) {

        return (String) session.getAttribute(DEVICE_ATTRIBUTE_ID_SESSION_ATTRIBUTE);
    }

}
