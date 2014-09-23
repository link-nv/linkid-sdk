/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.ltqr;

/**
 * Created by wvdhaute
 * Date: 14/01/14
 * Time: 11:03
 */
public class ChangeException extends Exception {

    private final ChangeErrorCode errorCode;

    public ChangeException(final ChangeErrorCode errorCode) {

        this.errorCode = errorCode;
    }

    public ChangeErrorCode getErrorCode() {

        return errorCode;
    }
}
