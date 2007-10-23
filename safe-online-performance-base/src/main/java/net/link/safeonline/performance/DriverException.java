/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.performance;

import java.util.Date;

/**
 * 
 * 
 * @author mbillemo
 */
public class DriverException extends Exception {

	private static final long serialVersionUID = 1L;
	private Exception exception;
	private long occurredTime;

	/**
	 * Create a new {@link DriverException} instance.
	 */
	public DriverException(Exception exception) {

		this.occurredTime = System.currentTimeMillis();
		this.exception = exception;
	}

	public DriverException(long occurredTime, Exception exception) {

		this.occurredTime = occurredTime;
		this.exception = exception;
	}

	/**
	 * Retrieve the occurredTime of this {@link DriverException}.
	 */
	public long getOccurredTime() {

		return this.occurredTime;
	}

	/**
	 * Retrieve the exception of this {@link DriverException}.
	 */
	public Exception getException() {

		return this.exception;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {

		return "At " + new Date(this.occurredTime) + " this happened:\\n"
				+ this.exception;
	}
}
