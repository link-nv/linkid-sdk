/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.authentication.service;

import java.util.HashMap;
import java.util.Map;

import javax.ejb.EJBException;

/**
 * Enumerates the supported authentication devices.
 * 
 * @author fcorneli
 * 
 */
public enum AuthenticationDevice {
	PASSWORD("password"), BEID("beid");

	private final String deviceName;

	private AuthenticationDevice(String deviceName) {
		this.deviceName = deviceName;
	}

	public String getDeviceName() {
		return this.deviceName;
	}

	private static final Map<String, AuthenticationDevice> deviceMap = new HashMap<String, AuthenticationDevice>();

	static {
		AuthenticationDevice[] authenticationDevices = AuthenticationDevice
				.values();
		for (AuthenticationDevice authenticationDevice : authenticationDevices) {
			deviceMap.put(authenticationDevice.getDeviceName(),
					authenticationDevice);
		}
	}

	public static AuthenticationDevice getAuthenticationDevice(String deviceName) {
		AuthenticationDevice authenticationDevice = deviceMap.get(deviceName);
		if (null == authenticationDevice) {
			throw new EJBException("unsupported device name: " + deviceName);
		}
		return authenticationDevice;
	}
}
