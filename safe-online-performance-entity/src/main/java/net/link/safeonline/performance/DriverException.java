/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.performance;

import java.util.Date;


/**
 * <h2>{@link DriverException}<br>
 * <sub>This exception occurs during driver requests and keeps the time at which it occurred.</sub></h2>
 * 
 * <p>
 * <i>Feb 19, 2008</i>
 * </p>
 * 
 * @author mbillemo
 */
public class DriverException extends Exception {

    private static final long serialVersionUID = 1L;

    private long              occurredTime;


    public DriverException(long occurredTime, Throwable exception) {

        super(exception);
        this.occurredTime = occurredTime;
    }

    public DriverException(long occurredTime, String message) {

        super(message);
        this.occurredTime = occurredTime;
    }

    public DriverException(Throwable exception) {

        this(System.currentTimeMillis(), exception);
    }

    public DriverException(String message) {

        this(System.currentTimeMillis(), message);
    }

    /**
     * Retrieve the time this {@link DriverException} occurred.
     */
    public long getOccurredTime() {

        return this.occurredTime;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {

        return new Date(this.occurredTime) + ": " + getMessage();
    }
}
