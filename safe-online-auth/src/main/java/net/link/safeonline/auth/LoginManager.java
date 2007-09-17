/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.auth;

import java.util.Set;

import javax.servlet.http.HttpSession;

import net.link.safeonline.auth.bean.AbstractLoginBean;
import net.link.safeonline.authentication.service.AuthenticationDevice;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The login manager makes sure that both the 'username' and the
 * 'authenticationDevice' are set at the same time to have a consistent login
 * approach. For Seam login components you can use {@link AbstractLoginBean}.
 * 
 * @author fcorneli
 * 
 */
public class LoginManager {

	private static final Log LOG = LogFactory.getLog(LoginManager.class);

	public static final String USERNAME_ATTRIBUTE = "username";

	public static final String AUTHENTICATION_DEVICE_ATTRIBUTE = "authenticationDevice";

	public static final String REQUIRED_DEVICES_ATTRIBUTE = "requiredDevices";

	public static final String TARGET_ATTRIBUTE = "target";

	public static final String APPLICATION_ID_ATTRIBUTE = "applicationId";

	private LoginManager() {
		// empty
	}

	public static void login(HttpSession session, String username,
			AuthenticationDevice device) {
		if (null == username) {
			throw new IllegalArgumentException("username is null");
		}
		if (null == device) {
			throw new IllegalArgumentException("device is null");
		}
		session.setAttribute(USERNAME_ATTRIBUTE, username);
		setAuthenticationDevice(session, device);
	}

	private static void setAuthenticationDevice(HttpSession session,
			AuthenticationDevice device) {
		session.setAttribute(AUTHENTICATION_DEVICE_ATTRIBUTE, device);
	}

	public static void relogin(HttpSession session, AuthenticationDevice device) {
		String username = getUsername(session);
		AuthenticationDevice currentDevice = getAuthenticationDevice(session);
		LOG.debug("relogin for " + username + " from device " + currentDevice
				+ " to device " + device);
		setAuthenticationDevice(session, device);
	}

	public static String getUsername(HttpSession session) {
		String username = findUsername(session);
		if (null == username) {
			throw new IllegalStateException(
					"username session attribute is not present");
		}
		return username;
	}

	public static AuthenticationDevice getAuthenticationDevice(
			HttpSession session) {
		AuthenticationDevice authenticationDevice = findAuthenticationDevice(session);
		if (null == authenticationDevice) {
			throw new IllegalStateException(
					"authenticationDevice session attribute is not present");
		}
		return authenticationDevice;
	}

	public static AuthenticationDevice findAuthenticationDevice(
			HttpSession session) {
		AuthenticationDevice authenticationDevice = (AuthenticationDevice) session
				.getAttribute(AUTHENTICATION_DEVICE_ATTRIBUTE);
		return authenticationDevice;
	}

	public static boolean isLoggedIn(HttpSession session) {
		String username = findUsername(session);
		return null != username;
	}

	public static String findUsername(HttpSession session) {
		String username = (String) session.getAttribute(USERNAME_ATTRIBUTE);
		return username;
	}

	public static void setApplication(HttpSession session, String applicationId) {
		if (null == applicationId) {
			throw new IllegalArgumentException("application is null");
		}
		session.setAttribute(APPLICATION_ID_ATTRIBUTE, applicationId);
	}

	public static void setTarget(HttpSession session, String target) {
		if (null == target) {
			throw new IllegalArgumentException("target is null");
		}
		session.setAttribute(TARGET_ATTRIBUTE, target);
	}

	/**
	 * Sets the required devices within the session. The set of required devices
	 * can be null.
	 * 
	 * @param session
	 * @param requiredDevices
	 */
	public static void setRequiredDevices(HttpSession session,
			Set<AuthenticationDevice> requiredDevices) {
		if (null == requiredDevices) {
			return;
		}
		session.setAttribute(REQUIRED_DEVICES_ATTRIBUTE, requiredDevices);
	}

	/**
	 * Gives back the set of required devices. The value returned can be null.
	 * 
	 * @param session
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static Set<AuthenticationDevice> getRequiredDevices(
			HttpSession session) {
		Set<AuthenticationDevice> requiredDevices = (Set<AuthenticationDevice>) session
				.getAttribute(REQUIRED_DEVICES_ATTRIBUTE);
		return requiredDevices;
	}

	public static String findApplication(HttpSession session) {
		String application = (String) session
				.getAttribute(APPLICATION_ID_ATTRIBUTE);
		return application;
	}

	public static String getApplication(HttpSession session) {
		String application = findApplication(session);
		if (null == application) {
			throw new IllegalStateException(
					"applicationId session attribute not set");
		}
		return null;
	}
}
