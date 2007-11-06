/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.authentication.service;

import java.util.Set;

import javax.ejb.Local;

import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.exception.EmptyDevicePolicyException;

/**
 * Interface for device policy service bean.
 * 
 * @author fcorneli
 */
@Local
public interface DevicePolicyService {

	/**
	 * Gives back the device policy for the given application. The device policy
	 * is a set of device Id's which the application accepts as authentication
	 * devices. The method signature has been optimized for ease of use in the
	 * authentication web application.
	 * 
	 * @param applicationId
	 * @param requiredDevicePolicy
	 *            the optional required device policy as communicated by the
	 *            application via the authentication protocol.
	 * @return
	 * @throws ApplicationNotFoundException
	 * @throws EmptyDevicePolicyException
	 */
	Set<AuthenticationDevice> getDevicePolicy(String applicationId,
			Set<AuthenticationDevice> requiredDevicePolicy)
			throws ApplicationNotFoundException, EmptyDevicePolicyException;

	/**
	 * Returns all devices
	 * 
	 * @return
	 */
	Set<AuthenticationDevice> getDevices();
}
