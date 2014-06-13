/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.ws.mandate;

public class PayException extends Exception {

    private final ErrorCode errorCode;

    public PayException(final ErrorCode errorCode) {

        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {

        return errorCode;
    }
}
