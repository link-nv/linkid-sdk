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
 * <h2>{@link StartTimeEntity} - Holds the time at which a scenario has been
 * executed.</h2>
 *
 * <p>
 * <i>Jan 17, 2008</i>
 * </p>
 *
 * @author mbillemo
 */
@Entity
public class StartTimeEntity implements Comparable<StartTimeEntity> {

	@Id
	@SuppressWarnings("unused")
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;

	private Long time;

	public StartTimeEntity() {
	}

	public StartTimeEntity(Long startTime) {

		this.time = startTime;
	}

	/**
	 * @return The time of this {@link StartTimeEntity}.
	 */
	public Long getTime() {

		return this.time;
	}

	/**
	 * {@inheritDoc}
	 */
	public int compareTo(StartTimeEntity o) {

		return this.time.compareTo(o.time);
	}
}
