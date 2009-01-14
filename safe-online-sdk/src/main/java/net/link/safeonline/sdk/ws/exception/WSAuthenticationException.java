/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.ws.exception;

import net.link.safeonline.ws.common.WSAuthenticationErrorCode;


/**
 * Exceptions thrown by the OLAS authentication web service.
 * 
 * @author wvdhaute
 * 
 */
public class WSAuthenticationException extends Exception {

    private static final long         serialVersionUID = 1L;

    private WSAuthenticationErrorCode errorCode;

    private String                    message;


    public WSAuthenticationException(WSAuthenticationErrorCode errorCode, String message) {

        super();
        this.errorCode = errorCode;
        this.message = message;
    }

    public WSAuthenticationErrorCode getErrorCode() {

        return this.errorCode;
    }

    @Override
    public String getMessage() {

        return this.message;
    }
}
