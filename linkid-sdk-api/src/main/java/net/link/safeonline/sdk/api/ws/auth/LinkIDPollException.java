/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.ws.auth;

/**
 * Created by wvdhaute
 * Date: 14/01/14
 * Time: 11:03
 */
public class LinkIDPollException extends Exception {

    private final LinkIDPollErrorCode errorCode;
    private final String              info;

    public LinkIDPollException(final LinkIDPollErrorCode errorCode, final String info) {

        this.errorCode = errorCode;
        this.info = info;
    }

    public LinkIDPollErrorCode getErrorCode() {

        return errorCode;
    }

    public String getInfo() {

        return info;
    }
}