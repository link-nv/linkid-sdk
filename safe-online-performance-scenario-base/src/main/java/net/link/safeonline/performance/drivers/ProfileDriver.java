/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.performance.drivers;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import net.link.safeonline.sdk.ws.MessageAccessor;
import net.link.safeonline.util.jacc.ProfileData;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Abstract class of a service driver. This class manages the internals; such as
 * collecting profile data, exceptions and execution speed for iterations.<br />
 * <br />
 * Implementing drivers need to declare methods specific to their functionality
 * in which they should call {@link #startNewIteration()} before performing any
 * driver logic and {@link #unloadDriver()} once they have completed their task;
 * or {@link #setDriverError(Exception)} if an error occurred during the work
 * they were doing. <br />
 * <br />
 * The profiling data will be gathered by this class and can later be retrieved
 * by using the getters ({@link #getProfileData()}, {@link #getProfileError()},
 * {@link #getProfileSpeed()}).
 * 
 * @author mbillemo
 */
public abstract class ProfileDriver<S extends MessageAccessor> {

	private static final Log LOG = LogFactory.getLog(ProfileDriver.class);

	protected S service;
	private String title;
	protected String host;
	protected LinkedList<DriverException> profileError = new LinkedList<DriverException>();
	protected LinkedList<ProfileData> profileData = new LinkedList<ProfileData>();

	/**
	 * @param hostname
	 *            The hostname of the host that's running the service.
	 */
	public ProfileDriver(String hostname, String title) {

		this.host = hostname;
		this.title = title;
	}

	public String getHost() {

		return this.host;
	}

	public synchronized List<ProfileData> getProfileData() {

		return Collections.unmodifiableList(this.profileData);
	}

	public synchronized List<DriverException> getProfileError() {

		return Collections.unmodifiableList(this.profileError);
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

	protected void unloadDriver() {

		unloadDriver(new ProfileData(this.service.getHeaders()));
	}

	protected synchronized void unloadDriver(ProfileData data) {

		this.profileData.removeLast(); // Remove the null placeholder.
		this.profileData.addLast(data);
	}

	protected synchronized DriverException setDriverError(Exception error) {

		LOG.debug(String.format("Failed driver request: %s", error));

		DriverException driverException;
		if (error instanceof DriverException)
			driverException = (DriverException) error;
		else
			driverException = new DriverException(error);

		this.profileError.removeLast(); // Remove the null placeholder.
		this.profileError.addLast(driverException);

		return driverException;
	}

	protected void loadDriver(S newService) {

		this.service = newService;
		loadDriver();
	}

	protected synchronized void loadDriver() {

		// Null placeholders for this iteration.
		this.profileData.add(null);
		this.profileError.add(null);
	}
}
