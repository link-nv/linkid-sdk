/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.performance.drivers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import net.link.safeonline.sdk.ws.AbstractMessageAccessor;
import net.link.safeonline.util.jacc.ProfileData;

/**
 * Abstract class of a service driver. This class manages the internals; such as
 * collecting profile data, exceptions and execution speed for iterations.<br />
 * <br />
 * Implementing drivers need to declare methods specific to their functionality
 * in which they should call {@link #startNewIteration()} before performing any
 * driver logic and {@link #setIterationData(AbstractMessageAccessor)} once they
 * have completed their task; or {@link #setIterationError(Exception)} if an
 * error occurred during the work they were doing. <br />
 * <br />
 * The profiling data will be gathered by this class and can later be retrieved
 * by using the getters ({@link #getProfileData()}, {@link #getProfileError()},
 * {@link #getProfileSpeed()}).
 * 
 * @author mbillemo
 */
public abstract class ProfileDriver {

	private long iterationStart;
	private String title;

	protected String host;
	protected LinkedList<ProfileData> profileData = new LinkedList<ProfileData>();
	protected LinkedList<Exception> profileError = new LinkedList<Exception>();
	protected LinkedList<Double> profileSpeed = new LinkedList<Double>();

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

		return Collections.unmodifiableList(new ArrayList<ProfileData>(
				this.profileData));
	}

	public synchronized List<Exception> getProfileError() {

		return Collections.unmodifiableList(new ArrayList<Exception>(
				this.profileError));
	}

	public synchronized List<Double> getProfileSpeed() {

		return Collections.unmodifiableList(new ArrayList<Double>(
				this.profileSpeed));
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

	protected void setIterationData(AbstractMessageAccessor service) {

		setIterationData(new ProfileData(service.getHeaders()));
	}

	protected synchronized void setIterationData(ProfileData data) {

		this.profileData.removeLast();
		this.profileData.addLast(data);
	}

	protected synchronized void setIterationError(Exception error) {

		this.profileError.removeLast();
		this.profileError.addLast(error);
	}

	protected synchronized void startNewIteration() {

		Double speed = null;
		if (0 != this.iterationStart && null != this.profileData.getLast()) {
			Long requestTime = this.profileData.getLast().getMeasurements()
					.get(ProfileData.REQUEST_DELTA_TIME);
			Long iterationTime = System.currentTimeMillis()
					- this.iterationStart;

			if (null != requestTime)
				speed = 1000d / (requestTime + iterationTime);
		}

		this.profileData.add(null);
		this.profileError.add(null);
		this.profileSpeed.add(speed);

		this.iterationStart = System.currentTimeMillis();
	}
}
