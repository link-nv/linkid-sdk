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
	private Long agentDuration;

	public AgentTimeEntity() {

		this.time = System.currentTimeMillis();
	}

	/**
	 * @return The time of this {@link AgentTimeEntity}.
	 */
	public Long getStart() {

		return this.time;
	}

	/**
	 * @return The duration of this {@link AgentTimeEntity}.
	 */
	public Long getDuration() {

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
}
