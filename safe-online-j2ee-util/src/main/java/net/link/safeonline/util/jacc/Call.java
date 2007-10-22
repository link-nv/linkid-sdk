/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.util.jacc;

import java.lang.reflect.Method;
import java.util.Date;

/**
 * 
 * 
 * @author mbillemo
 */
public class Call {

	private String signature;
	private Date initiated;
	private long duration;

	/**
	 * Create a new {@link Call} instance.
	 */
	public Call(Method method, long startTime, long endTime) {

		this(method.toGenericString(), startTime, endTime);
	}

	/**
	 * Create a new {@link Call} instance.
	 */
	public Call(String signature, long initiated, long duration) {

		this.signature = signature;
		this.initiated = new Date(initiated);
		this.duration = duration;
	}

	/**
	 * Retrieve the signature of this {@link Call}.
	 */
	public String getSignature() {

		return this.signature;
	}

	/**
	 * Retrieve time it took to make this {@link Call} (in milliseconds).
	 */
	public long getDuration() {

		return this.duration;
	}

	/**
	 * Retrieve the date/time of when this {@link Call} was initiated.
	 */
	public Date getInitiated() {

		return this.initiated;
	}
}
