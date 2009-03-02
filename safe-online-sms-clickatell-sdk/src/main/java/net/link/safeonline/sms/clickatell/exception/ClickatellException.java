/*
 * SafeOnline project.
 * 
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.sms.clickatell.exception;

/**
 * <h2>{@link ClickatellException}<br>
 * <sub>Generic Clickatell Exception</sub></h2>
 * 
 * <p>
 * Generic Clickatell Exception
 * </p>
 * 
 * <p>
 * <i>Feb 20, 2009</i>
 * </p>
 * 
 * @author dhouthoo
 */
public class ClickatellException extends Exception {

    private static final long serialVersionUID = 1L;


    public ClickatellException() {

        super();
    }

    public ClickatellException(String message) {

        super(message);
    }

    public ClickatellException(String message, Throwable t) {

        super(message, t);
    }
}
