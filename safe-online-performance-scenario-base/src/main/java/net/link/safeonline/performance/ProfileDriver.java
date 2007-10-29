/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.performance;

import java.util.ArrayList;
import java.util.List;

import net.link.safeonline.sdk.ws.AbstractMessageAccessor;
import net.link.safeonline.util.jacc.ProfileData;

/**
 * 
 * 
 * @author mbillemo
 */
public abstract class ProfileDriver {

	protected List<ProfileData> profileData = new ArrayList<ProfileData>();

	protected String host;

	/**
	 * @param hostname
	 *            The hostname of the host that's running the service.
	 */
	public ProfileDriver(String hostname) {

		this.host = hostname;
	}

	protected void addProfileData(AbstractMessageAccessor service) {

		this.profileData.add(new ProfileData(service.getHeaders()));
	}

	public List<ProfileData> getProfileData() {

		return this.profileData;
	}
}
