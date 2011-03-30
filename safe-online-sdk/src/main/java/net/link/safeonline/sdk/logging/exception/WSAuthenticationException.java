/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.logging.exception;

import net.link.safeonline.auth.ws.AuthenticationErrorCode;


/**
 * Exceptions thrown by the linkID authentication web service.
 *
 * @author wvdhaute
 */
public class WSAuthenticationException extends Exception {

    private AuthenticationErrorCode errorCode;

    public WSAuthenticationException(AuthenticationErrorCode errorCode, String message, final Throwable cause) {

        super(message, cause);

        this.errorCode = errorCode;
    }

    public WSAuthenticationException(AuthenticationErrorCode errorCode, final Throwable cause) {

        this( errorCode, null, cause );
    }

    public AuthenticationErrorCode getErrorCode() {

        return errorCode;
    }
}
