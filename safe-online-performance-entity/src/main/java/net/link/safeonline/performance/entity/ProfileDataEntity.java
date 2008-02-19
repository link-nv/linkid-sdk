/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.performance.entity;

import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;

import net.link.safeonline.util.performance.ProfileData;

/**
 * <h2>{@link ProfileDataEntity}<br>
 * <sub>Holds data gathered by OLAS during a single request.</sub></h2>
 * 
 * <p>
 * <i>Jan 10, 2008</i>
 * </p>
 * 
 * @author mbillemo
 */
@Entity
@NamedQuery(name = ProfileDataEntity.getByProfile, query = "SELECT p"
		+ "    FROM ProfileDataEntity p"
		+ "    WHERE p.profile = :profile")
public class ProfileDataEntity implements Comparable<ProfileDataEntity> {

	public static final String getByProfile = "ProfileDataEntity.getByProfile";

	@Id
	@SuppressWarnings("unused")
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;

	@ManyToOne
	private DriverProfileEntity profile;

	@OneToMany
	private Set<MeasurementEntity> measurements;

	private Long scenarioStart;

	public ProfileDataEntity() {
	}

	public ProfileDataEntity(DriverProfileEntity profile, Long scenarioStart,
			Set<MeasurementEntity> measurements) {

		this.profile = profile;
		this.scenarioStart = scenarioStart;
		this.measurements = measurements;
	}

	/**
	 * @return The measurements of this {@link ProfileDataEntity}.
	 */
	public Set<MeasurementEntity> getMeasurements() {

		return this.measurements;
	}

	/**
	 * Retrieve the duration for a certain measurement.
	 */
	public long getMeasurement(String key) {

		for (MeasurementEntity measurement : this.measurements)
			if (measurement.getMeasurement().equals(key))
				return measurement.getDuration() == null ? 0 : measurement
						.getDuration();

		return 0;
	}

	/**
	 * The time the request was started on the OLAS server.
	 */
	public Long getStartTime() {

		return getMeasurement(ProfileData.REQUEST_START_TIME);
	}

	/**
	 * The time the scenario execution of this profile data was started.
	 */
	public Long getScenarioStart() {

		return this.scenarioStart;
	}

	/**
	 * @return The {@link DriverProfileEntity} that generated this
	 *         {@link ProfileDataEntity}.
	 */
	public DriverProfileEntity getProfile() {

		return this.profile;
	}

	/**
	 * {@inheritDoc}
	 */
	public int compareTo(ProfileDataEntity o) {

		if (getStartTime() == null)
			return -1;
		if (o == null || o.getStartTime() == null)
			return 1;

		return getStartTime().compareTo(o.getStartTime());
	}
}
