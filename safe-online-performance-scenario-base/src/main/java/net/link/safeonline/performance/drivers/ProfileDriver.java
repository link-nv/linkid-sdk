/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.performance.drivers;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.link.safeonline.sdk.ws.MessageAccessor;
import net.link.safeonline.util.jacc.ProfileData;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Abstract class of a service driver. This class manages the internals; such as
 * collecting profile data, exceptions and execution speed for iterations.<br>
 * <br>
 * Implementing drivers need to declare methods specific to their functionality
 * in which they should call {@link #startNewIteration()} before performing any
 * driver logic and {@link #unloadDriver()} once they have completed their task;
 * or {@link #setDriverError(Exception)} if an error occurred during the work
 * they were doing. <br>
 * <br>
 * The profiling data will be gathered by this class and can later be retrieved
 * by using the getters ({@link #getProfileData()}, {@link #getProfileError()},
 * {@link #getProfileSpeed()}).<br>
 * <br>
 * TODO: Profile Data thread collisions:<br>
 * Thread1 loads, Thread 2 loads, Thread1 unloads, Thread2 unloads.
 * 
 * @author mbillemo
 */
public abstract class ProfileDriver {

	private static final Log LOG = LogFactory.getLog(ProfileDriver.class);

	private String title;
	protected String host;
	protected Map<Object, Integer> services = new HashMap<Object, Integer>();
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

	protected void unloadDriver(MessageAccessor service) {

		unloadDriver(service, new ProfileData(service.getHeaders()));
	}

	protected synchronized void unloadDriver(Object key, ProfileData data) {

		Integer index = this.services.remove(key);
		if (data == null)
			LOG.warn("Unloading " + getClass() + " (iteration "
					+ (this.profileData.size() - 1)
					+ ") with empty profile data!");

		else if (data.getMeasurement(ProfileData.REQUEST_START_TIME) == 0)
			LOG.warn("No valid profile data in " + getClass() + " (iteration "
					+ (this.profileData.size() - 1) + ").");

		else if (this.profileData.size() <= index)
			LOG.warn("Illegal unload index " + index + " while we only have "
					+ this.profileData.size() + " datas.");

		else
			this.profileData.set(index, data);
	}

	protected synchronized DriverException setDriverError(Object key,
			Exception error) {

		Integer index = this.services.remove(key);
		LOG.debug(String.format("Failed driver request: %s", error));

		DriverException driverException;
		if (error instanceof DriverException)
			driverException = (DriverException) error;
		else
			driverException = new DriverException(error);

		this.profileError.set(index, driverException);

		return driverException;
	}

	/**
	 * The key you use here should be a thread-specific object. You are advised
	 * to use the SDK service as a key. If you're not using the SDK in this
	 * driver implementation, use another object that has a unique
	 * {@link Object#hashCode()} between different threads (such as
	 * {@link Thread#currentThread()}).
	 */
	protected synchronized void loadDriver(Object key) {

		// Null placeholders for this iteration.
		this.services.put(key, this.profileData.size());
		this.profileData.add(null);
		this.profileError.add(null);
	}
}
