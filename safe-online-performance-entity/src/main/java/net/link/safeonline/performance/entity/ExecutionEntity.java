/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.performance.entity;

import java.util.Set;
import java.util.TreeSet;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
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

	@OneToMany()
	private Set<AgentTimeEntity> agentTimes;

	public ExecutionEntity() {

		this.profiles = new TreeSet<DriverProfileEntity>();
		this.agentTimes = new TreeSet<AgentTimeEntity>();
	}

	public ExecutionEntity(String scenarioName, String hostname) {

		this.scenarioName = scenarioName;
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
}
