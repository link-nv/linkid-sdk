/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.logging.exception;

import net.link.safeonline.sdk.ws.AuthenticationErrorCode;


/**
 * Exceptions thrown by the linkID authentication web service.
 *
 * @author wvdhaute
 */
public class WSAuthenticationException extends Exception {

    private AuthenticationErrorCode errorCode;

    private String message;

    public WSAuthenticationException(AuthenticationErrorCode errorCode, String message) {

        super();
        this.errorCode = errorCode;
        this.message = message;
    }

    public AuthenticationErrorCode getErrorCode() {

        return errorCode;
    }

    @Override
    public String getMessage() {

        return message;
    }
}
