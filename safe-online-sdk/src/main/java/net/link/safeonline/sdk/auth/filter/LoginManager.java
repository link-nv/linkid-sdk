/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.auth.filter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import net.link.safeonline.sdk.auth.servlet.LogoutServlet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Login manager for servlet container based web applications. The login status is saved on the HTTP session.
 * 
 * <p>
 * Notice that we explicitly disconnected the login manager from the authentication protocol manager. Both store their data into the HTTP
 * session.
 * </p>
 * 
 * @author fcorneli
 * 
 */
public class LoginManager {

    private static final Log   LOG                                    = LogFactory.getLog(LoginManager.class);

    public static final String USERID_SESSION_ATTRIBUTE               = "userId";

    public static final String AUTHENTICATED_DEVICE_SESSION_ATTRIBUTE = "authenticatedDevice";


    private LoginManager() {

        // empty
    }

    /**
     * Checks whether the user is logged in via the SafeOnline authentication web application or not.
     * 
     * @request
     */
    public static boolean isAuthenticated(HttpServletRequest request) {

        return isAuthenticated(request, USERID_SESSION_ATTRIBUTE);
    }

    /**
     * Checks whether the user is logged in via the SafeOnline authentication web application or not. It uses the specified parameter in the
     * session.
     * 
     * @param request
     * @param paramName
     */
    public static boolean isAuthenticated(HttpServletRequest request, String paramName) {

        String userId = findUserId(request, paramName);

        return null != userId;
    }

    /**
     * Gives back the SafeOnline authenticated userId, or <code>null</code> if the user was not yet authenticated.
     * 
     * @param request
     */
    public static String findUserId(HttpServletRequest request) {

        return findUserId(request, USERID_SESSION_ATTRIBUTE);
    }

    /**
     * Gives back the SafeOnline authenticated userId, or <code>null</code> if the user was not yet authenticated. It uses the specified
     * parameter in the session.
     * 
     * @param request
     * @param paramName
     */
    public static String findUserId(HttpServletRequest request, String paramName) {

        if (null == paramName)
            throw new IllegalArgumentException("userId session attribute name should not be null");

        HttpSession httpSession = request.getSession();
        String userId = (String) httpSession.getAttribute(paramName);
        return userId;
    }

    /**
     * Gives back the SafeOnline authenticated userId.
     * 
     * @param request
     *            the servlet request object.
     * @throws ServletException
     *             if the user was not yet authenticated via SafeOnline.
     */
    public static String getUserId(HttpServletRequest request)
            throws ServletException {

        return getUserId(request, USERID_SESSION_ATTRIBUTE);
    }

    /**
     * Gives back the SafeOnline authenticated userId. It uses the specified parameter in the session.
     * 
     * @param request
     *            the servlet request object.
     * @param paramName
     *            the parameter name that is used to store login info
     * @throws ServletException
     *             if the user was not yet authenticated via SafeOnline.
     */
    public static String getUserId(HttpServletRequest request, String paramName)
            throws ServletException {

        String userId = findUserId(request, paramName);
        if (null == userId)
            throw new ServletException("no user was authenticated");

        return userId;
    }

    /**
     * Sets the userId. This method should only be invoked after the user has been properly authenticated via the SafeOnline authentication
     * web application.
     * 
     * @param userId
     *            the userId of the SafeOnline authenticated principal.
     * @param httpRequest
     */
    public static void setUserId(String userId, HttpServletRequest httpRequest) {

        setUserId(userId, httpRequest, USERID_SESSION_ATTRIBUTE);
    }

    /**
     * Sets the userId. This method should only be invoked after the user has been properly authenticated via the SafeOnline authentication
     * web application.
     * 
     * @param userId
     *            the userId of the SafeOnline authenticated principal.
     * @param httpRequest
     * @param paramName
     */
    public static void setUserId(String userId, HttpServletRequest httpRequest, String paramName) {

        LOG.debug("setting userId: " + userId);
        HttpSession session = httpRequest.getSession();
        session.setAttribute(paramName, userId);
    }

    /**
     * Removes the userId.
     * 
     * @param httpRequest
     * 
     * @throws ServletException
     */
    public static void removeUserId(HttpServletRequest httpRequest)
            throws ServletException {

        removeUserId(USERID_SESSION_ATTRIBUTE, httpRequest);
    }

    /**
     * Removes the userId.
     * 
     * @param paramName
     * @param httpRequest
     * 
     * @throws ServletException
     */
    public static void removeUserId(String paramName, HttpServletRequest httpRequest)
            throws ServletException {

        String userId = findUserId(httpRequest, paramName);
        if (null == userId)
            throw new ServletException("no user was authenticated");
        LOG.debug("removing userId: " + userId);
        HttpSession session = httpRequest.getSession();
        session.removeAttribute(paramName);
    }

    /**
     * Gives back the SafeOnline authenticated device, or <code>null</code> if the user was not yet authenticated.
     * 
     * @param request
     */
    public static String findAuthenticatedDevice(HttpServletRequest request) {

        return findAuthenticatedDevice(request, AUTHENTICATED_DEVICE_SESSION_ATTRIBUTE);
    }

    /**
     * Gives back the SafeOnline authenticated device, or <code>null</code> if the user was not yet authenticated. It uses the specified
     * parameter in the session.
     * 
     * @param request
     * @param paramName
     */
    public static String findAuthenticatedDevice(HttpServletRequest request, String paramName) {

        if (null == paramName)
            throw new IllegalArgumentException("authenticatedDevice session attribute name should not be null");

        HttpSession httpSession = request.getSession();
        String userId = (String) httpSession.getAttribute(paramName);
        return userId;
    }

    /**
     * Gives back the SafeOnline authenticated device.
     * 
     * @param request
     *            the servlet request object.
     * @throws ServletException
     *             if the user was not yet authenticated via SafeOnline.
     */
    public static String getAuthenticatedDevice(HttpServletRequest request)
            throws ServletException {

        return getAuthenticatedDevice(request, AUTHENTICATED_DEVICE_SESSION_ATTRIBUTE);
    }

    /**
     * Gives back the SafeOnline authenticated device. It uses the specified parameter in the session.
     * 
     * @param request
     *            the servlet request object.
     * @param paramName
     *            the parameter name that is used to store login info
     * @throws ServletException
     *             if the user was not yet authenticated via SafeOnline.
     */
    public static String getAuthenticatedDevice(HttpServletRequest request, String paramName)
            throws ServletException {

        String userId = findAuthenticatedDevice(request, paramName);
        if (null == userId)
            throw new ServletException("no user was authenticated");

        return userId;
    }

    /**
     * Sets the authenticatedDevice. This method should only be invoked after the user has been properly authenticated via the SafeOnline
     * authentication web application.
     * 
     * @param authenticatedDevice
     *            the device the SafeOnline authenticated principal used to authenticate with.
     * @param httpRequest
     */
    public static void setAuthenticatedDevice(String authenticatedDevice, HttpServletRequest httpRequest) {

        setAuthenticatedDevice(authenticatedDevice, httpRequest, AUTHENTICATED_DEVICE_SESSION_ATTRIBUTE);
    }

    /**
     * Sets the userId. This method should only be invoked after the user has been properly authenticated via the SafeOnline authentication
     * web application.
     * 
     * @param authenticatedDevice
     *            the device the SafeOnline authenticated principal used to authenticate with.
     * @param httpRequest
     * @param paramName
     */
    public static void setAuthenticatedDevice(String authenticatedDevice, HttpServletRequest httpRequest, String paramName) {

        LOG.debug("setting authenticatedDevice: " + authenticatedDevice);
        HttpSession session = httpRequest.getSession();
        session.setAttribute(paramName, authenticatedDevice);
    }

    /**
     * Removes the authenticatedDevice.
     * 
     * @param httpRequest
     * 
     * @throws ServletException
     */
    public static void removeAuthenticatedDevice(HttpServletRequest httpRequest)
            throws ServletException {

        removeAuthenticatedDevice(AUTHENTICATED_DEVICE_SESSION_ATTRIBUTE, httpRequest);
    }

    /**
     * Removes the authenticatedDevice.
     * 
     * @param paramName
     * @param httpRequest
     * 
     * @throws ServletException
     */
    public static void removeAuthenticatedDevice(String paramName, HttpServletRequest httpRequest)
            throws ServletException {

        String authenticatedDevice = findAuthenticatedDevice(httpRequest, paramName);
        if (null == authenticatedDevice)
            throw new ServletException("no user was authenticated");
        LOG.debug("removing authenticatedDevice: " + authenticatedDevice);
        HttpSession session = httpRequest.getSession();
        session.removeAttribute(paramName);
    }

    /**
     * Request this session be invalidated.<br>
     * <br>
     * This method does not invalidate the session immediately. It sets a parameter on the session that will be used by the SDK's
     * LogoutServlet to invalidate the session at the end of the logout cycle.
     */
    public static void invalidateSession(HttpServletRequest request) {

        request.getSession().setAttribute(LogoutServlet.INVALIDATE_SESSION, true);
    }
}
