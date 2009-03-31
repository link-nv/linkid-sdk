/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.auth;

import java.util.Set;

import javax.servlet.http.HttpSession;

import net.link.safeonline.authentication.service.AuthenticationAssertion;
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

    public static final String AUTHENTICATION_ASSERTION_ATTRIBUTE  = "LoginManager.authenticationAssertion";

    public static final String LOGIN_ATTRIBUTE                     = "LoginManager.loginName";

    public static final String REQUIRED_DEVICES_ATTRIBUTE          = "LoginManager.requiredDevices";

    public static final String TARGET_ATTRIBUTE                    = "LoginManager.target";

    public static final String APPLICATION_ID_ATTRIBUTE            = "LoginManager.applicationId";

    public static final String APPLICATION_FRIENDLY_NAME_ATTRIBUTE = "applicationName";


    private LoginManager() {

        // empty
    }

    public static void login(HttpSession session, AuthenticationAssertion authenticationAssertion) {

        if (null == authenticationAssertion)
            throw new IllegalArgumentException("authentication assertion is null");
        /**
         * set user ID for {@link JAASLoginFilter}
         */
        session.setAttribute(USERID_ATTRIBUTE, authenticationAssertion.getSubject().getUserId());
        session.setAttribute(AUTHENTICATION_ASSERTION_ATTRIBUTE, authenticationAssertion);
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

    public static boolean isLoggedIn(HttpSession session) {

        String userId = findUserId(session);
        return null != userId;
    }

    public static String findUserId(HttpSession session) {

        String userId = (String) session.getAttribute(USERID_ATTRIBUTE);
        return userId;
    }

    public static void setLogin(HttpSession session, String loginName) {

        LOG.debug("set loginName: " + loginName);
        session.setAttribute(LOGIN_ATTRIBUTE, loginName);
    }

    public static String getLogin(HttpSession session) {

        String loginName = (String) session.getAttribute(LOGIN_ATTRIBUTE);
        if (null == loginName)
            throw new IllegalStateException("loginName session attribute is not present");
        return loginName;
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
