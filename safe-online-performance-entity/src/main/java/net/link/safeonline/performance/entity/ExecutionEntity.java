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
import java.util.SortedSet;
import java.util.TreeSet;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;

/**
 * <h2>{@link ExecutionEntity} - [in short] (TODO).</h2>
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
@NamedQuery(name = ExecutionEntity.findById, query = "SELECT e"
		+ "    FROM ExecutionEntity e WHERE e.id = :executionId")
public class ExecutionEntity {

	public static final String findById = "ExecutionEntity.findById";

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;

	private String scenarioName;
	private String hostname;

	@OneToMany(mappedBy = "execution")
	private Set<DriverProfileEntity> profiles;

	@OneToMany(mappedBy = "execution")
	private Set<StartTimeEntity> startTimes;

	public ExecutionEntity() {

		this.profiles = new TreeSet<DriverProfileEntity>();
		this.startTimes = new TreeSet<StartTimeEntity>();
	}

	public ExecutionEntity(String scenarioName, String hostname) {

		this.scenarioName = scenarioName;
		this.hostname = hostname;

		this.startTimes = new TreeSet<StartTimeEntity>();
	}

	/**
	 * @return The name of the scenario that was executed.
	 */
	public String getScenarioName() {

		return this.scenarioName;
	}

	/**
	 * @return A number that identifies an execution uniquely.
	 */
	public int getId() {

		return this.id;
	}

	/**
	 * @return The name of the host that runs the OLAS service.
	 */
	public String getHostname() {

		return this.hostname;
	}

	/**
	 * @return The driver profiles generated for this execution.
	 */
	public Set<DriverProfileEntity> getProfiles() {

		return this.profiles;
	}

	/**
	 * @return A set of times at which scenarios were started for this
	 *         execution.
	 */
	public SortedSet<StartTimeEntity> getStartTimes() {

		return new TreeSet<StartTimeEntity>(this.startTimes);
	}
}
