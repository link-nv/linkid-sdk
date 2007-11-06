/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.performance.drivers;

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
	protected List<Exception> profileError = new ArrayList<Exception>();

	protected String host;

	private String title;

	/**
	 * @param hostname
	 *            The hostname of the host that's running the service.
	 */
	public ProfileDriver(String hostname, String title) {

		this.host = hostname;
		this.title = title;
	}

	protected void addProfileData(AbstractMessageAccessor service) {

		this.profileData.add(new ProfileData(service.getHeaders()));
	}

	protected void addProfileError(Exception error) {

		this.profileError.add(error);
	}

	public List<ProfileData> getProfileData() {

		return this.profileData;
	}

	public List<Exception> getProfileError() {

		return this.profileError;
	}

	public String getHost() {

		return this.host;
	}

	public String getTitle() {

		return this.title;
	}

	/**
	 * @{inheritDoc}
	 */
	@Override
	public String toString() {

		return String.format("%s%n---%n%nHost: %s%n%s", getTitle(), getHost(),
				getProfileData());
	}
}
