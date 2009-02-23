/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.authentication.exception;

import javax.ejb.ApplicationException;

import net.link.safeonline.shared.SharedConstants;


@ApplicationException(rollback = true)
public class SafeOnlineException extends Exception {

    private static final long serialVersionUID = 1L;

    private final String      errorCode;


    /**
     * Gives back the error code. The error code is a unique code per exception type. It can be used by client-side components for better
     * error handling.
     * 
     */
    public String getErrorCode() {

        return errorCode;
    }

    public SafeOnlineException() {

        this(null, (Throwable) null);
    }

    public SafeOnlineException(Throwable cause) {

        this(null, cause);
    }

    public SafeOnlineException(String message) {

        this(message, (Throwable) null);
    }

    public SafeOnlineException(String message, Throwable cause) {

        this(message, cause, SharedConstants.UNDEFINED_ERROR);
    }

    public SafeOnlineException(String message, String errorCode) {

        this(message, null, errorCode);
    }

    public SafeOnlineException(String message, Throwable cause, String errorCode) {

        super(message, cause);
        this.errorCode = errorCode;
    }
}
