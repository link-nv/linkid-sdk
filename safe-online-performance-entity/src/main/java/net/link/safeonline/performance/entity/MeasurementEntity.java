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

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * <h2>{@link MeasurementEntity} - [in short] (TODO).</h2>
 * <p>
 * [description / usage].
 * </p>
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

	private String measurement;
	private Long duration;

	public MeasurementEntity() {
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
}
