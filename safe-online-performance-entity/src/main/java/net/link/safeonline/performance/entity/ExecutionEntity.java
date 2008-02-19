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

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
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
				+ "    WHERE e.id = :executionId"),
		@NamedQuery(name = ExecutionEntity.getTimes, query = "SELECT at"
				+ "    FROM AgentTimeEntity at"
				+ "    WHERE at.execution = :execution") })
public class ExecutionEntity {

	public static final String findAll = "ExecutionEntity.findAll";
	public static final String findById = "ExecutionEntity.findById";
	public static final String getTimes = "ExecutionEntity.getTimes";

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;

	private String scenarioName;
	private int agents;
	private int workers;
	private Date startTime;
	private long duration;
	private String hostname;
	private Double speed;
	private boolean dirtySpeed;

	@OneToMany(mappedBy = "execution")
	private Set<DriverProfileEntity> profiles;

	public ExecutionEntity() {

		this.profiles = new TreeSet<DriverProfileEntity>();
		this.dirtySpeed = false;
	}

	public ExecutionEntity(String scenarioName, Integer agents,
			Integer workers, Date startTime, Long duration, String hostname) {

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

	/**
	 * Force a recalculation of the speed.
	 *
	 * TODO: This would all be a lot more efficient in a query.
	 */
	public void updateSpeed() {

		try {
			ExecutionService executionService = (ExecutionService) new InitialContext()
					.lookup(ExecutionService.BINDING);
			SortedSet<AgentTimeEntity> sortedTimes = executionService
					.getExecutionTimes(this);

			try {
				this.speed = (double) sortedTimes.size()
						/ (sortedTimes.last().getAgentDuration()
								+ sortedTimes.last().getStart() - sortedTimes
								.first().getStart());
			} catch (NullPointerException e) {
				this.speed = null;
			}

			this.dirtySpeed = false;
		} catch (NamingException e) {
		}
	}

	/**
	 * Signal that the speed value currently contained in this
	 * {@link ExecutionEntity} is dirty and needs to be recalculated.
	 */
	public void dirtySpeed() {

		this.dirtySpeed = true;
	}
}
