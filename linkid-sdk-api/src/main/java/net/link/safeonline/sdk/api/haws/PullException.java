/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.haws;

/**
 * Created by wvdhaute
 * Date: 14/01/14
 * Time: 11:03
 */
public class PullException extends Exception {

    private final PullErrorCode errorCode;
    private final String        info;

    public PullException(final PullErrorCode errorCode, final String info) {

        this.errorCode = errorCode;
        this.info = info;
    }

    public PullErrorCode getErrorCode() {

        return errorCode;
    }

    public String getInfo() {

        return info;
    }
}
