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
package net.link.safeonline.performance.scenario;

import java.util.Date;

/**
 * <h2>{@link ExecutionMetadata} - [in short] (TODO).</h2>
 *
 * <p>
 * [description / usage].
 * </p>
 *
 * <p>
 * <i>Feb 5, 2008</i>
 * </p>
 *
 * @author mbillemo
 */
public class ExecutionMetadata {

	private Integer id;
	private Integer agents;
	private Integer workers;
	private Date startTime;
	private Long duration;
	private String hostname;
	private Double speed;
	private String scenarioName;
	private String scenarioDescription;

	/**
	 * Use this constructor to create an execution initiation request.
	 */
	public static ExecutionMetadata createRequest(String scenarioName,
			Integer agents, Integer workers, Date startTime, Long duration,
			String hostname) {

		return new ExecutionMetadata(null, scenarioName, null, agents, workers,
				startTime, duration, hostname, null);
	}

	/**
	 * Use this constructor to create an execution result response.
	 */
	public static ExecutionMetadata createResponse(Integer executionId,
			String scenarioName, String scenarioDescription, Integer agents,
			Integer workers, Date startTime, Long duration, String hostname,
			Double speed) {

		return new ExecutionMetadata(executionId, scenarioName,
				scenarioDescription, agents, workers, startTime, duration,
				hostname, speed);
	}

	/**
	 * Complete constructor.
	 */
	private ExecutionMetadata(Integer id, String scenarioName,
			String scenarioDescription, Integer agents, Integer workers,
			Date startTime, Long duration, String hostname, Double speed) {

		this.id = id;
		this.scenarioName = scenarioName;
		this.scenarioDescription = scenarioDescription;
		this.agents = agents;
		this.workers = workers;
		this.startTime = startTime;
		this.duration = duration;
		this.hostname = hostname;
		this.speed = speed;
	}

	public Integer getId() {

		return this.id;
	}

	public String getScenarioName() {

		return this.scenarioName;
	}

	public String getScenarioDescription() {

		return this.scenarioDescription;
	}

	public void setScenarioName(String scenarioName) {

		this.scenarioName = scenarioName;
	}

	public Integer getAgents() {

		return this.agents;
	}

	public Integer getWorkers() {

		return this.workers;
	}

	public Date getStartTime() {

		return this.startTime;
	}

	public Long getDuration() {

		return this.duration;
	}

	public String getHostname() {

		return this.hostname;
	}

	public Double getSpeed() {

		return this.speed;
	}
}
