/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.auth.protocol;

import java.util.Set;

import net.link.safeonline.authentication.service.AuthenticationDevice;

/**
 * Protocol Context class. Protocol Context objects should be generated by
 * protocol handlers after they have successfully processed an authentication
 * request.
 * 
 * @author fcorneli
 * 
 */
public class ProtocolContext {

	private final String applicationId;

	private final String target;

	private final Set<AuthenticationDevice> requiredDevices;

	public ProtocolContext(String applicationId, String target) {
		this(applicationId, target, null);
	}

	/**
	 * Main constructor.
	 * 
	 * @param applicationId
	 *            the application Id of the application that the authentication
	 *            protocol handler has determined that issued the authentication
	 *            request.
	 * @param target
	 *            the target URL to which to send the authentication response.
	 * @param requiredDevices
	 *            the optional set of required devices.
	 */
	public ProtocolContext(String applicationId, String target,
			Set<AuthenticationDevice> requiredDevices) {
		this.applicationId = applicationId;
		this.target = target;
		this.requiredDevices = requiredDevices;
	}

	public String getApplicationId() {
		return this.applicationId;
	}

	public String getTarget() {
		return this.target;
	}

	public Set<AuthenticationDevice> getRequiredDevices() {
		return this.requiredDevices;
	}
}
