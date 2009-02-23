/*
 * SafeOnline project.
 * 
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.osgi.sms.exception;

/**
 * <h2>{@link SmsServiceException}<br>
 * <sub>Generic SmsService Exception</sub></h2>
 * 
 * <p>
 * This exception can be thrown by bundles implementing the SmsService
 * </p>
 * 
 * <p>
 * <i>Feb 20, 2009</i>
 * </p>
 * 
 * @author dhouthoo
 */
public class SmsServiceException extends Exception {

    private static final long serialVersionUID = 1L;


    public SmsServiceException() {

        super();
    }

    public SmsServiceException(String message) {

        super(message);
    }

    public SmsServiceException(String message, Throwable e) {

        super(message, e);
    }
}
