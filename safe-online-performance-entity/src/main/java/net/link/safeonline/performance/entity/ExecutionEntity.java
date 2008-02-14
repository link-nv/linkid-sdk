/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.performance.entity;

import java.util.Date;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;

/**
 * <h2>{@link ExecutionEntity} - Holds the global metadata for a scenario
 * execution.</h2>
 *
 * <p>
 * <i>Jan 11, 2008</i>
 * </p>
 *
 * @author mbillemo
 */
@Entity
@NamedQueries( {
		@NamedQuery(name = ExecutionEntity.findAll, query = "SELECT e"
				+ "    FROM ExecutionEntity e"),
		@NamedQuery(name = ExecutionEntity.findById, query = "SELECT e"
				+ "    FROM ExecutionEntity e WHERE e.id = :executionId") })
public class ExecutionEntity {

	public static final String findById = "ExecutionEntity.findById";

	public static final String findAll = "ExecutionEntity.findAll";

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;

	private String scenarioName;
	private Integer agents;
	private Integer workers;
	private Date startTime;
	private Long duration;
	private String hostname;
	private Double speed;

	@OneToMany(mappedBy = "execution")
	private Set<DriverProfileEntity> profiles;

	@OneToMany
	private Set<AgentTimeEntity> agentTimes;

	private transient boolean dirtySpeed;

	public ExecutionEntity() {

		this.profiles = new TreeSet<DriverProfileEntity>();
		this.agentTimes = new TreeSet<AgentTimeEntity>();
	}

	public ExecutionEntity(String scenarioName, Integer agents,
			Integer workers, Date startTime, Long duration, String hostname) {

		this.scenarioName = scenarioName;
		this.agents = agents;
		this.workers = workers;
		this.startTime = startTime;
		this.duration = duration;
		this.hostname = hostname;

		this.agentTimes = new TreeSet<AgentTimeEntity>();
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
	public Set<AgentTimeEntity> getAgentTimes() {

		return this.agentTimes;
	}

	/**
	 * @return The amount of agents this scenario execution was initiated on.
	 */
	public Integer getAgents() {

		return this.agents;
	}

	/**
	 * @return The amount of workers that was used to process this execution.
	 */
	public Integer getWorkers() {

		return this.workers;
	}

	/**
	 * @return The time at which this execution first started.
	 */
	public Date getStartTime() {

		return this.startTime;
	}

	/**
	 * @return The amount of time this execution was schedules to run (ms).
	 */
	public Long getDuration() {

		return this.duration;
	}

	/**
	 * The speed will only be recalculated if it has been set as dirty.
	 * 
	 * @return The average scenario execution speed in this execution.
	 */
	public Double getSpeed() {

		if (this.dirtySpeed)
			updateSpeed();

		return this.speed;
	}

	public void updateSpeed() {

		SortedSet<AgentTimeEntity> sortedTimes = new TreeSet<AgentTimeEntity>();

		this.speed = (sortedTimes.last().getAgentDuration()
				+ sortedTimes.last().getStart() - sortedTimes.first()
				.getStart())
				/ (double) sortedTimes.size();
	}

	/**
	 * Signal that the speed value currently contained in this
	 * {@link ExecutionEntity} is dirty and needs to be recalculated.
	 */
	public void dirtySpeed() {

		this.dirtySpeed = true;
	}
}
