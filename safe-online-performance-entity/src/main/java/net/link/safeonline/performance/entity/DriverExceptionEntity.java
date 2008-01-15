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
 * <h2>{@link DriverExceptionEntity} - [in short] (TODO).</h2>
 * <p>
 * [description / usage].
 * </p>
 * <p>
 * <i>Jan 11, 2008</i>
 * </p>
 *
 * @author mbillemo
 */
@Entity
public class DriverExceptionEntity {

	@Id
	@SuppressWarnings("unused")
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;

	private long occurredTime;
	private String message;

	public DriverExceptionEntity() {
	}

	/**
	 * Create a new {@link DriverExceptionEntity} instance.
	 */
	public DriverExceptionEntity(long occurredTime, String message) {

		this.occurredTime = occurredTime;
		this.message = message;
	}

	/**
	 * @return The time the exception occurred.
	 */
	public long getOccurredTime() {

		return this.occurredTime;
	}

	/**
	 * @return A message describing problem that occurred.
	 */
	public String getMessage() {

		return this.message;
	}
}
