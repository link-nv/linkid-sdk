/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.auth.filter;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Login manager for servlet container based web applications. The login status
 * is saved on the HTTP session.
 * 
 * @author fcorneli
 * 
 */
public class LoginManager {

	private static final Log LOG = LogFactory.getLog(LoginManager.class);

	public static final String USERNAME_SESSION_ATTRIBUTE = "username";

	private LoginManager() {
		// empty
	}

	/**
	 * Checks whether the user is logged in via the SafeOnline authentication
	 * web application or not.
	 * 
	 * @param request
	 * @return
	 */
	public static boolean isAuthenticated(ServletRequest request) {
		String username = findUsername(request);

		return null != username;
	}

	/**
	 * Gives back the SafeOnline authenticated username, or <code>null</code>
	 * if the user was not yet authenticated.
	 * 
	 * @param request
	 * @return
	 */
	public static String findUsername(ServletRequest request) {
		HttpServletRequest httpServletRequest = (HttpServletRequest) request;
		HttpSession httpSession = httpServletRequest.getSession();

		String username = (String) httpSession
				.getAttribute(USERNAME_SESSION_ATTRIBUTE);
		return username;
	}

	/**
	 * Gives back the SafeOnline authenticated username.
	 * 
	 * @param request
	 *            the servlet request object.
	 * @return
	 * @throws ServletException
	 *             if the user was not yet authenticated via SafeOnline.
	 */
	public static String getUsername(ServletRequest request)
			throws ServletException {
		String username = findUsername(request);
		if (null == username) {
			throw new ServletException("no user was authenticated");
		}
		return username;
	}

	/**
	 * Sets the username. This method should only be invoked after the user has
	 * been properly authenticated via the SafeOnline authentication web
	 * application.
	 * 
	 * @param username
	 *            the username of the SafeOnline authenticated principal.
	 * @param request
	 */
	public static void setUsername(String username, ServletRequest request) {
		LOG.debug("setting username: " + username);
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpSession session = httpRequest.getSession();
		session.setAttribute(USERNAME_SESSION_ATTRIBUTE, username);
	}
}
