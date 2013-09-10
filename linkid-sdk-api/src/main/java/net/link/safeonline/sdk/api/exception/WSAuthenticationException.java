/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.exception;

import net.link.safeonline.sdk.api.ws.auth.AuthenticationStatusCode;


/**
 * Exceptions thrown by the linkID authentication web service.
 *
 * @author wvdhaute
 */
public class WSAuthenticationException extends Exception {

    private AuthenticationStatusCode statusCode;

    public WSAuthenticationException(AuthenticationStatusCode statusCode, String message, final Throwable cause) {

        super(message, cause);

        this.statusCode = statusCode;
    }

    public WSAuthenticationException(AuthenticationStatusCode statusCode, final Throwable cause) {

        this( statusCode, null, cause );
    }

    public AuthenticationStatusCode getStatusCode() {

        return statusCode;
    }
}
