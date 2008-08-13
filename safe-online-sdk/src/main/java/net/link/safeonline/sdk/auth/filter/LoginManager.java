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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Login manager for servlet container based web applications. The login status is saved on the HTTP session.
 *
 * <p>
 * Notice that we explicitly disconnected the login manager from the authentication protocol manager. Both store their
 * data into the HTTP session.
 * </p>
 *
 * @author fcorneli
 *
 */
public class LoginManager {

    private static final Log   LOG                        = LogFactory.getLog(LoginManager.class);

    public static final String USERNAME_SESSION_ATTRIBUTE = "username";


    private LoginManager() {

        // empty
    }

    /**
     * Checks whether the user is logged in via the SafeOnline authentication web application or not.
     *
     * @request
     */
    public static boolean isAuthenticated(HttpServletRequest request) {

        return isAuthenticated(request, USERNAME_SESSION_ATTRIBUTE);
    }

    /**
     * Checks whether the user is logged in via the SafeOnline authentication web application or not. It uses the
     * specified parameter in the session.
     *
     * @param request
     * @param paramName
     */
    public static boolean isAuthenticated(HttpServletRequest request, String paramName) {

        String username = findUsername(request, paramName);

        return null != username;
    }

    /**
     * Gives back the SafeOnline authenticated username, or <code>null</code> if the user was not yet authenticated.
     *
     * @param request
     */
    public static String findUsername(HttpServletRequest request) {

        return findUsername(request, USERNAME_SESSION_ATTRIBUTE);
    }

    /**
     * Gives back the SafeOnline authenticated username, or <code>null</code> if the user was not yet authenticated. It
     * uses the specified parameter in the session.
     *
     * @param request
     * @param paramName
     */
    public static String findUsername(HttpServletRequest request, String paramName) {

        if (null == paramName)
            throw new IllegalArgumentException("username session attribute name should not be null");

        HttpSession httpSession = request.getSession();
        String username = (String) httpSession.getAttribute(paramName);
        return username;
    }

    /**
     * Gives back the SafeOnline authenticated username.
     *
     * @param request
     *            the servlet request object.
     * @throws ServletException
     *             if the user was not yet authenticated via SafeOnline.
     */
    public static String getUsername(HttpServletRequest request) throws ServletException {

        return getUsername(request, USERNAME_SESSION_ATTRIBUTE);
    }

    /**
     * Gives back the SafeOnline authenticated username. It uses the specified parameter in the session.
     *
     * @param request
     *            the servlet request object.
     * @param paramName
     *            the parameter name that is used to store login info
     * @throws ServletException
     *             if the user was not yet authenticated via SafeOnline.
     */
    public static String getUsername(HttpServletRequest request, String paramName) throws ServletException {

        String username = findUsername(request, paramName);
        if (null == username)
            throw new ServletException("no user was authenticated");
        return username;
    }

    /**
     * Sets the username. This method should only be invoked after the user has been properly authenticated via the
     * SafeOnline authentication web application.
     *
     * @param username
     *            the username of the SafeOnline authenticated principal.
     * @param httpRequest
     */
    public static void setUsername(String username, HttpServletRequest httpRequest) {

        setUsername(username, httpRequest, USERNAME_SESSION_ATTRIBUTE);
    }

    /**
     * Sets the username. This method should only be invoked after the user has been properly authenticated via the
     * SafeOnline authentication web application.
     *
     * @param username
     *            the username of the SafeOnline authenticated principal.
     * @param httpRequest
     * @param paramName
     */
    public static void setUsername(String username, HttpServletRequest httpRequest, String paramName) {

        LOG.debug("setting username: " + username);
        HttpSession session = httpRequest.getSession();
        session.setAttribute(paramName, username);
    }
}
