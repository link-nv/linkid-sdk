/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.device.sdk;

import javax.servlet.ServletException;
import javax.servlet.http.HttpSession;

/**
 * Manages the device registration service urls.
 * 
 * @author wvdhaute
 * 
 */
public class RegistrationManager {

	private static final String SAFE_ONLINE_DEVICE_REGISTRATION_SERVICE_URL_ATTRIBUTE = "SafeOnlineRegistrationServiceUrl";

	private static final String SAFE_ONLINE_DEVICE_REGISTRATION_EXIT_SERVICE_URL_ATTRIBUTE = "SafeOnlineRegistrationExitServiceUrl";

	private RegistrationManager() {
		// empty
	}

	public static void setServiceUrls(HttpSession session, String source,
			String safeOnlineHostName, String safeOnlineHostPort,
			String safeOnlineHostPortSsl) throws ServletException {
		if (source.equals("auth")) {
			session.setAttribute(
					SAFE_ONLINE_DEVICE_REGISTRATION_SERVICE_URL_ATTRIBUTE,
					"https://" + safeOnlineHostName + ":"
							+ safeOnlineHostPortSsl
							+ "/olas-auth/device/registration");
			session.setAttribute(
					SAFE_ONLINE_DEVICE_REGISTRATION_EXIT_SERVICE_URL_ATTRIBUTE,
					"https://" + safeOnlineHostName + ":"
							+ safeOnlineHostPortSsl
							+ "/olas-auth/device/registrationexit");
		} else if (source.equals("user")) {
			session.setAttribute(
					SAFE_ONLINE_DEVICE_REGISTRATION_SERVICE_URL_ATTRIBUTE,
					"https://" + safeOnlineHostName + ":"
							+ safeOnlineHostPortSsl
							+ "/olas/device/registration");
			session.setAttribute(
					SAFE_ONLINE_DEVICE_REGISTRATION_EXIT_SERVICE_URL_ATTRIBUTE,
					"http://" + safeOnlineHostName + ":" + safeOnlineHostPort
							+ "/olas/device/registrationexit");
		} else
			throw new ServletException("Unknown source");

	}

	public static String getSafeOnlineRegistrationServiceUrl(HttpSession session) {
		return (String) session
				.getAttribute(SAFE_ONLINE_DEVICE_REGISTRATION_SERVICE_URL_ATTRIBUTE);
	}

	public static String getSafeOnlineRegistrationExitServiceUrl(
			HttpSession session) {
		return (String) session
				.getAttribute(SAFE_ONLINE_DEVICE_REGISTRATION_EXIT_SERVICE_URL_ATTRIBUTE);
	}
}
