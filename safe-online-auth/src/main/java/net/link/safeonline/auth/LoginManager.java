/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.auth;

import java.util.Set;

import javax.servlet.http.HttpSession;

import net.link.safeonline.entity.DeviceEntity;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * The login manager makes sure that both the 'userId' and the 'authenticationDevice' are set at the same time to have a consistent login
 * approach.
 * 
 * @author fcorneli
 * 
 */
public class LoginManager {

    private static final Log   LOG                                 = LogFactory.getLog(LoginManager.class);

    public static final String USERID_ATTRIBUTE                    = "userId";

    public static final String AUTHENTICATION_DEVICE_ATTRIBUTE     = "LoginManager.authenticationDevice";

    public static final String REQUIRED_DEVICES_ATTRIBUTE          = "LoginManager.requiredDevices";

    public static final String TARGET_ATTRIBUTE                    = "LoginManager.target";

    public static final String APPLICATION_ID_ATTRIBUTE            = "LoginManager.applicationId";

    public static final String APPLICATION_FRIENDLY_NAME_ATTRIBUTE = "applicationName";


    private LoginManager() {

        // empty
    }

    public static void login(HttpSession session, String userId, DeviceEntity device) {

        if (null == userId)
            throw new IllegalArgumentException("userId is null");
        if (null == device)
            throw new IllegalArgumentException("device is null");
        session.setAttribute(USERID_ATTRIBUTE, userId);
        setAuthenticationDevice(session, device);
    }

    private static void setAuthenticationDevice(HttpSession session, DeviceEntity device) {

        session.setAttribute(AUTHENTICATION_DEVICE_ATTRIBUTE, device);
    }

    public static void relogin(HttpSession session, DeviceEntity device) {

        String userId = getUserId(session);
        // can be null, in case the device registration was combined with an
        // olas user registration
        DeviceEntity currentDevice = findAuthenticationDevice(session);
        if (null == currentDevice) {
            LOG.debug("login for " + userId + " with device " + device.getName());
        } else {
            LOG.debug("relogin for " + userId + " from device " + currentDevice.getName() + " to device " + device.getName());
        }
        setAuthenticationDevice(session, device);
    }

    public static void setUserId(HttpSession session, String userId) {

        LOG.debug("set userId: " + userId);
        session.setAttribute(USERID_ATTRIBUTE, userId);
    }

    public static String getUserId(HttpSession session) {

        String userId = findUserId(session);
        if (null == userId)
            throw new IllegalStateException("userId session attribute is not present");
        return userId;
    }

    public static DeviceEntity getAuthenticationDevice(HttpSession session) {

        DeviceEntity authenticationDevice = findAuthenticationDevice(session);
        if (null == authenticationDevice)
            throw new IllegalStateException("authenticationDevice session attribute is not present");
        return authenticationDevice;
    }

    public static DeviceEntity findAuthenticationDevice(HttpSession session) {

        DeviceEntity authenticationDevice = (DeviceEntity) session.getAttribute(AUTHENTICATION_DEVICE_ATTRIBUTE);
        return authenticationDevice;
    }

    public static boolean isLoggedIn(HttpSession session) {

        String userId = findUserId(session);
        return null != userId;
    }

    public static String findUserId(HttpSession session) {

        String userId = (String) session.getAttribute(USERID_ATTRIBUTE);
        return userId;
    }

    public static void setApplicationId(HttpSession session, long applicationId) {

        session.setAttribute(APPLICATION_ID_ATTRIBUTE, applicationId);
    }

    public static Long findApplication(HttpSession session) {

        Long applicationId = (Long) session.getAttribute(APPLICATION_ID_ATTRIBUTE);
        return applicationId;
    }

    public static void setApplicationFriendlyName(HttpSession session, String applicationFriendlyName) {

        if (null == applicationFriendlyName)
            throw new IllegalArgumentException("applicationFriendlyName is null");
        session.setAttribute(APPLICATION_FRIENDLY_NAME_ATTRIBUTE, applicationFriendlyName);
    }

    public static void setTarget(HttpSession session, String target) {

        if (null == target)
            throw new IllegalArgumentException("target is null");
        session.setAttribute(TARGET_ATTRIBUTE, target);
    }

    /**
     * Sets the required devices within the session. The set of required devices can be null.
     * 
     * @param session
     * @param requiredDevices
     */
    public static void setRequiredDevices(HttpSession session, Set<DeviceEntity> requiredDevices) {

        if (null == requiredDevices)
            return;
        session.setAttribute(REQUIRED_DEVICES_ATTRIBUTE, requiredDevices);
    }

    /**
     * Gives back the set of required devices. The value returned can be null.
     * 
     * @param session
     */
    @SuppressWarnings("unchecked")
    public static Set<DeviceEntity> getRequiredDevices(HttpSession session) {

        Set<DeviceEntity> requiredDevices = (Set<DeviceEntity>) session.getAttribute(REQUIRED_DEVICES_ATTRIBUTE);
        return requiredDevices;
    }

    public static String getTarget(HttpSession session) {

        String target = (String) session.getAttribute(TARGET_ATTRIBUTE);
        if (null == target)
            throw new IllegalStateException(TARGET_ATTRIBUTE + " session attribute not present");
        return target;
    }
}
