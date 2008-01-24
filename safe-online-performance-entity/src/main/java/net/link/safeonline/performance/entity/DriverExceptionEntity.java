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
 * <h2>{@link DriverExceptionEntity} - Holds problems encountered during driver
 * execution.</h2>
 *
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
