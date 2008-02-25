/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.performance.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

/**
 * <h2>{@link MeasurementEntity}<br>
 * <sub>Holds a description and duration for a single measurement.</sub></h2>
 *
 * <p>
 * <i>Jan 14, 2008</i>
 * </p>
 *
 * @author mbillemo
 */
@Entity
public class MeasurementEntity {

	@Id
	@SuppressWarnings("unused")
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;

	@ManyToOne
	private ProfileDataEntity profileData;

	private String measurement;
	private Long duration;

	public MeasurementEntity() {
	}

	public MeasurementEntity(String measurement, Double duration) {

		this(measurement, duration.longValue());
	}

	public MeasurementEntity(String measurement, Long duration) {

		this.measurement = measurement;
		this.duration = duration;
	}

	/**
	 * @return The measurement of this {@link MeasurementEntity}.
	 */
	public String getMeasurement() {

		return this.measurement;
	}

	/**
	 * @return The duration of this {@link MeasurementEntity}.
	 */
	public Long getDuration() {

		return this.duration;
	}

	/**
	 * @return The {@link ProfileDataEntity} linking to this
	 *         {@link MeasurementEntity}.
	 */
	public ProfileDataEntity getProfileData() {

		return this.profileData;
	}
}
