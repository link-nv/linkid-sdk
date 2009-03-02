/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.authentication.exception;

import javax.ejb.ApplicationException;


@ApplicationException(rollback = true)
public class DeviceAuthenticationException extends SafeOnlineException {

    private static final long serialVersionUID = 1L;


    public DeviceAuthenticationException() {

    }

    public DeviceAuthenticationException(Throwable cause) {

        super(cause);
    }

    public DeviceAuthenticationException(String message) {

        super(message);
    }

    public DeviceAuthenticationException(String message, Throwable cause) {

        super(message, cause);
    }
}
