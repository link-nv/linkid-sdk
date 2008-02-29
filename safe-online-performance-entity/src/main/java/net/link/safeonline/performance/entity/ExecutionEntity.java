/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.performance.entity;

import java.util.Date;
import java.util.Set;
import java.util.TreeSet;

import javax.ejb.EJB;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;

import net.link.safeonline.performance.service.ExecutionService;

/**
 * <h2>{@link ExecutionEntity}<br>
 * <sub>Holds the global metadata for a scenario execution.</sub></h2>
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
				+ "    FROM ExecutionEntity e"
				+ "    WHERE e.startTime = :startTime"),
		@NamedQuery(name = ExecutionEntity.calcSpeed, query = "SELECT 1000 * COUNT(t) / ( MAX(t.startTime) - MIN(t.startTime) )"
				+ "    FROM ScenarioTimingEntity t"
				+ "    WHERE t.execution = :execution") })
public class ExecutionEntity {

	public static final String findAll = "ExecutionEntity.findAll";
	public static final String findById = "ExecutionEntity.findById";
	public static final String calcSpeed = "ExecutionEntity.calcSpeed";

	@Id
	private Date startTime;

	private String scenarioName;
	private int agents;
	private int workers;
	private long duration;
	private String hostname;
	private Double speed;
	private boolean dirtySpeed;

	@OneToMany(mappedBy = "execution")
	private Set<DriverProfileEntity> profiles;

	@EJB
	private transient ExecutionService executionService;

	public ExecutionEntity() {

		this.profiles = new TreeSet<DriverProfileEntity>();
		this.dirtySpeed = false;
	}

	public ExecutionEntity(String scenarioName, Integer agents, int workers,
			Date startTime, long duration, String hostname) {

		this();

		this.scenarioName = scenarioName;
		this.agents = agents;
		this.workers = workers;
		this.startTime = startTime;
		this.duration = duration;
		this.hostname = hostname;
	}

	/**
	 * @return The name of the scenario that was executed.
	 */
	public String getScenarioName() {

		return this.scenarioName;
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
	 * @return The amount of agents this scenario execution was initiated on.
	 */
	public int getAgents() {

		return this.agents;
	}

	/**
	 * @return The amount of workers that was used to process this execution.
	 */
	public int getWorkers() {

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
	public long getDuration() {

		return this.duration;
	}

	/**
	 * The speed will only be recalculated if it has been set as dirty (which
	 * automatically happens each time a scenario has been completed for it).
	 *
	 * @return The average scenario execution speed in this execution.
	 */
	public Double getSpeed() {

		if (this.dirtySpeed)
			this.executionService.updateSpeed(this);

		return this.speed;
	}

	/**
	 * @param speed
	 *            The average scenario execution speed in this execution.
	 */
	public void setSpeed(Double speed) {

		this.speed = speed;
	}

	/**
	 * Signal that the speed value currently contained in this
	 * {@link ExecutionEntity} is dirty and needs to be recalculated.
	 */
	public void dirtySpeed() {

		this.dirtySpeed = true;
	}
}
