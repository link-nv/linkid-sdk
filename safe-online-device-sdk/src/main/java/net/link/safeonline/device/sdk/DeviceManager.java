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

	/**
	 * Url to which a remote device issuer should send the initial
	 * authentication request to when registrating, updating or removing a
	 * device.
	 */
	private static final String DEVICE_LANDING_SERVICE_URL_ATTRIBUTE = "DeviceLandingServiceUrl";

	/**
	 * Url to which a remote device issuer should redirect after completing the
	 * registration, update or removal.
	 */
	private static final String DEVICE_EXIT_SERVICE_URL_ATTRIBUTE = "DeviceExitServiceUrl";

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
		session.setAttribute(DEVICE_EXIT_SERVICE_URL_ATTRIBUTE, node
				.getLocation()
				+ "/olas-auth/main.seam");
	}

	public static void setServiceUrls(HttpSession session, String nodeName,
			String source) throws ServletException {
		OlasEntity node = getNode(nodeName);

		if (source.equals("auth")) {
			session.setAttribute(DEVICE_LANDING_SERVICE_URL_ATTRIBUTE, node
					.getLocation()
					+ "/olas-auth/device/landing");
			session.setAttribute(DEVICE_EXIT_SERVICE_URL_ATTRIBUTE, node
					.getLocation()
					+ "/olas-auth/device/exit");
		} else if (source.equals("user")) {
			session.setAttribute(DEVICE_LANDING_SERVICE_URL_ATTRIBUTE, node
					.getLocation()
					+ "/olas/device/landing");
			session.setAttribute(DEVICE_EXIT_SERVICE_URL_ATTRIBUTE, node
					.getHTTPLocation()
					+ "/olas/device/exit");
		} else {
			throw new ServletException("Unknown source");
		}

	}

	public static String getDeviceLandingServiceUrl(HttpSession session) {
		return (String) session
				.getAttribute(DEVICE_LANDING_SERVICE_URL_ATTRIBUTE);
	}

	public static String getDeviceExitServiceUrl(HttpSession session) {
		return (String) session.getAttribute(DEVICE_EXIT_SERVICE_URL_ATTRIBUTE);
	}
}
