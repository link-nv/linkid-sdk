/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.performance.drivers;

import java.util.Date;

/**
 * 
 * 
 * @author mbillemo
 */
public class DriverException extends Exception {

	private static final long serialVersionUID = 1L;
	private long occurredTime;

	public DriverException(String string) {

		this(new RuntimeException(string));
	}

	public DriverException(Exception exception) {

		this(System.currentTimeMillis(), exception);
	}

	public DriverException(long occurredTime, Exception exception) {

		super(exception);

		this.occurredTime = occurredTime;
	}

	/**
	 * Retrieve the occurredTime of this {@link DriverException}.
	 */
	public long getOccurredTime() {

		return this.occurredTime;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {

		return "At " + new Date(this.occurredTime) + " this happened:\n"
				+ getCause();
	}
}
