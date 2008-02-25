/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.device.sdk;

import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpSession;

/**
 * Manages the device service urls.
 * 
 * @author wvdhaute
 * 
 */
public class DeviceManager {

	private static final String SAFE_ONLINE_DEVICE_LANDING_SERVICE_URL_ATTRIBUTE = "SafeOnlineDeviceLandingServiceUrl";

	private static final String SAFE_ONLINE_DEVICE_EXIT_SERVICE_URL_ATTRIBUTE = "SafeOnlineDeviceExitServiceUrl";

	private DeviceManager() {
		// empty
	}

	public static void setServiceUrls(HttpSession session, String source,
			Map<String, String> configParams) throws ServletException {
		String safeOnlineHostName = configParams.get("SafeOnlineHostName");
		String safeOnlineHostPort = configParams.get("SafeOnlineHostPort");
		String safeOnlineHostPortSsl = configParams
				.get("SafeOnlineHostPortSsl");

		if (source.equals("auth")) {
			session.setAttribute(
					SAFE_ONLINE_DEVICE_LANDING_SERVICE_URL_ATTRIBUTE,
					"https://" + safeOnlineHostName + ":"
							+ safeOnlineHostPortSsl
							+ "/olas-auth/device/landing");
			session.setAttribute(SAFE_ONLINE_DEVICE_EXIT_SERVICE_URL_ATTRIBUTE,
					"https://" + safeOnlineHostName + ":"
							+ safeOnlineHostPortSsl + "/olas-auth/device/exit");
		} else if (source.equals("user")) {
			session.setAttribute(
					SAFE_ONLINE_DEVICE_LANDING_SERVICE_URL_ATTRIBUTE,
					"https://" + safeOnlineHostName + ":"
							+ safeOnlineHostPortSsl + "/olas/device/landing");
			session.setAttribute(SAFE_ONLINE_DEVICE_EXIT_SERVICE_URL_ATTRIBUTE,
					"http://" + safeOnlineHostName + ":" + safeOnlineHostPort
							+ "/olas/device/exit");
		} else
			throw new ServletException("Unknown source");

	}

	public static String getSafeOnlineDeviceLandingServiceUrl(HttpSession session) {
		return (String) session
				.getAttribute(SAFE_ONLINE_DEVICE_LANDING_SERVICE_URL_ATTRIBUTE);
	}

	public static String getSafeOnlineDeviceExitServiceUrl(HttpSession session) {
		return (String) session
				.getAttribute(SAFE_ONLINE_DEVICE_EXIT_SERVICE_URL_ATTRIBUTE);
	}
}
