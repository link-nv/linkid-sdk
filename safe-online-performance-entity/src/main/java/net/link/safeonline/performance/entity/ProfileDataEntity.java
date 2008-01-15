/*
 *   Copyright 2008, Maarten Billemont
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package net.link.safeonline.performance.entity;

import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import net.link.safeonline.util.performance.ProfileData;

/**
 * <h2>{@link ProfileDataEntity} - [in short] (TODO).</h2>
 * <p>
 * [description / usage].
 * </p>
 * <p>
 * <i>Jan 10, 2008</i>
 * </p>
 *
 * @author mbillemo
 */
@Entity
public class ProfileDataEntity {

	@Id
	@SuppressWarnings("unused")
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;

	@OneToMany
	private Set<MeasurementEntity> measurements;

	public ProfileDataEntity() {
	}

	public ProfileDataEntity(Set<MeasurementEntity> measurements) {

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
	public long getStartTime() {

		return getMeasurement(ProfileData.REQUEST_START_TIME);
	}
}
