/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.data;

import java.io.Serializable;
import java.util.Set;

import net.link.safeonline.authentication.service.AuthenticationDevice;
import net.link.safeonline.entity.SubscriptionEntity;

public class SubscriptionDO implements Serializable {

	private static final long serialVersionUID = 1L;

	private SubscriptionEntity subscription;

	private Set<AuthenticationDevice> allowedDevices;

	public SubscriptionDO(SubscriptionEntity subscription,
			Set<AuthenticationDevice> allowedDevices) {
		this.subscription = subscription;
		this.allowedDevices = allowedDevices;
	}

	public SubscriptionEntity getSubscription() {
		return this.subscription;
	}

	public Set<AuthenticationDevice> getAllowedDevices() {
		return this.allowedDevices;
	}

	public String getDeviceRestrictionList() {
		if (!this.subscription.getApplication().isDeviceRestriction())
			return null;
		String deviceList = "";
		for (AuthenticationDevice allowedDevice : this.allowedDevices) {
			deviceList += allowedDevice.getDeviceName() + " ";
		}
		return deviceList;
	}

	public String getApplicationName() {
		if (null != this.subscription.getApplication().getFriendlyName())
			return this.subscription.getApplication().getFriendlyName();
		return this.subscription.getApplication().getName();

	}
}
