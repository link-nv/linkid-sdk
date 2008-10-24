/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.serial.exception;

public class SerialCommunicationsException extends Exception {

    private static final long serialVersionUID = 1L;


    public SerialCommunicationsException(String message) {

        super(message);
    }

    public SerialCommunicationsException(String message, Throwable cause) {

        super(message, cause);
    }

}
