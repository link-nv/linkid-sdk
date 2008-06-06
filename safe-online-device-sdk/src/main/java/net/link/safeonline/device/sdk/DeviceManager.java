/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.device.sdk;

import javax.servlet.ServletException;
import javax.servlet.http.HttpSession;

import net.link.safeonline.authentication.exception.NodeNotFoundException;
import net.link.safeonline.authentication.service.NodeAuthenticationService;
import net.link.safeonline.entity.OlasEntity;
import net.link.safeonline.util.ee.EjbUtils;

/**
 * Manages the device service urls.
 * 
 * @author wvdhaute
 * 
 */
public class DeviceManager {

	private static final String SAFE_ONLINE_DEVICE_LANDING_SERVICE_URL_ATTRIBUTE = "SafeOnlineDeviceLandingServiceUrl";

	private static final String SAFE_ONLINE_DEVICE_EXIT_SERVICE_URL_ATTRIBUTE = "SafeOnlineDeviceExitServiceUrl";

	private static final String SAFE_ONLINE_DEVICE_WS_LOCATION = "SafeOnlineDeviceWsLocation";

	private DeviceManager() {
		// empty
	}

	private static OlasEntity getNode(String nodeName) throws ServletException {
		NodeAuthenticationService nodeAuthenticationService = EjbUtils.getEJB(
				"SafeOnline/NodeAuthenticationServiceBean/local",
				NodeAuthenticationService.class);
		OlasEntity node;
		try {
			node = nodeAuthenticationService.getNode(nodeName);
		} catch (NodeNotFoundException e) {
			throw new ServletException("Unknown Olas Node");
		}
		return node;
	}

	public static void setAuthServiceUrls(HttpSession session, String nodeName)
			throws ServletException {
		OlasEntity node = getNode(nodeName);
		session.setAttribute(SAFE_ONLINE_DEVICE_EXIT_SERVICE_URL_ATTRIBUTE,
				node.getLocation() + "/olas-auth/main.seam");
	}

	public static void setServiceUrls(HttpSession session, String nodeName,
			String source) throws ServletException {
		OlasEntity node = getNode(nodeName);

		session.setAttribute(SAFE_ONLINE_DEVICE_WS_LOCATION, node.getLocation()
				.replaceFirst(".*://", ""));

		if (source.equals("auth")) {
			session.setAttribute(
					SAFE_ONLINE_DEVICE_LANDING_SERVICE_URL_ATTRIBUTE, node
							.getLocation()
							+ "/olas-auth/device/landing");
			session.setAttribute(SAFE_ONLINE_DEVICE_EXIT_SERVICE_URL_ATTRIBUTE,
					node.getLocation() + "/olas-auth/device/exit");
		} else if (source.equals("user")) {
			session.setAttribute(
					SAFE_ONLINE_DEVICE_LANDING_SERVICE_URL_ATTRIBUTE, node
							.getLocation()
							+ "/olas/device/landing");
			session.setAttribute(SAFE_ONLINE_DEVICE_EXIT_SERVICE_URL_ATTRIBUTE,
					node.getHTTPLocation() + "/olas/device/exit");
		} else {
			throw new ServletException("Unknown source");
		}

	}

	public static String getSafeOnlineDeviceLandingServiceUrl(
			HttpSession session) {
		return (String) session
				.getAttribute(SAFE_ONLINE_DEVICE_LANDING_SERVICE_URL_ATTRIBUTE);
	}

	public static String getSafeOnlineDeviceExitServiceUrl(HttpSession session) {
		return (String) session
				.getAttribute(SAFE_ONLINE_DEVICE_EXIT_SERVICE_URL_ATTRIBUTE);
	}

	public static String getWsLocation(HttpSession session) {
		return (String) session.getAttribute(SAFE_ONLINE_DEVICE_WS_LOCATION);
	}
}
