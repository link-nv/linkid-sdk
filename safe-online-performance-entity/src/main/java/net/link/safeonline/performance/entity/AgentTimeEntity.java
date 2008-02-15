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
 * <h2>{@link AgentTimeEntity} - Holds the time at which a scenario has been
 * executed.</h2>
 *
 * <p>
 * <i>Jan 17, 2008</i>
 * </p>
 *
 * @author mbillemo
 */
@Entity
public class AgentTimeEntity implements Comparable<AgentTimeEntity> {

	@Id
	@SuppressWarnings("unused")
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;

	private Long time;
	private Long olasDuration;
	private Long agentDuration;
	private Long startFreeMem;
	private Long endFreeMem;

	@ManyToOne
	private ExecutionEntity execution;

	public AgentTimeEntity() {

		this.time = System.currentTimeMillis();
	}

	public AgentTimeEntity(ExecutionEntity execution) {

		this();

		this.execution = execution;
	}

	/**
	 * @return The time of this {@link AgentTimeEntity}.
	 */
	public Long getStart() {

		return this.time;
	}

	/**
	 * Add a new timing information about a call made to OLAS during the
	 * scenario that is timed with this entity.
	 */
	public void addOlasTime(long newOlasTime) {

		if (this.olasDuration == null)
			this.olasDuration = newOlasTime;
		else
			this.olasDuration += newOlasTime;
	}

	/**
	 * @return The duration of this {@link AgentTimeEntity}.
	 */
	public Long getOlasDuration() {

		return this.olasDuration;
	}

	/**
	 * @return The duration of this {@link AgentTimeEntity}.
	 */
	public Long getAgentDuration() {

		return this.agentDuration;
	}

	/**
	 * Signal that the scenario started at the time contained in this entity has
	 * just ended.
	 */
	public void stop() {

		this.agentDuration = System.currentTimeMillis() - this.time;
	}

	/**
	 * {@inheritDoc}
	 */
	public int compareTo(AgentTimeEntity o) {

		return this.time.compareTo(o.time);
	}

	/**
	 * Remember the amount of memory was available when the execution of the
	 * scenario timed by this entity started.
	 */
	public void setStartMemory(long startFreeMem) {

		this.startFreeMem = startFreeMem;
	}

	/**
	 * @return The amount of memory was available when the execution of the
	 *         scenario timed by this entity started.
	 */
	public Long getStartFreeMem() {

		return this.startFreeMem;
	}

	/**
	 * Remember the amount of memory was available when the execution of the
	 * scenario timed by this entity ended.
	 */
	public void setEndMemory(long endFreeMem) {

		this.endFreeMem = endFreeMem;
	}

	/**
	 * @return The amount of memory was available when the execution of the
	 *         scenario timed by this entity ended.
	 */
	public Long getEndFreeMem() {

		return this.endFreeMem;
	}

	/**
	 * @return The execution of this {@link AgentTimeEntity}.
	 */
	public ExecutionEntity getExecution() {
		return this.execution;
	}
}
