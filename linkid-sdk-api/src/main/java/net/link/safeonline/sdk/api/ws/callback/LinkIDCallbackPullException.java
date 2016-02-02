/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.ws.callback;

/**
 * Created by wvdhaute
 * Date: 14/01/14
 * Time: 11:03
 */
public class LinkIDCallbackPullException extends Exception {

    private final LinkIDCallbackPullErrorCode errorCode;
    private final String                      info;

    public LinkIDCallbackPullException(final LinkIDCallbackPullErrorCode errorCode, final String info) {

        super( String.format( "Error code: %s, info: %s", errorCode, info ) );
        this.errorCode = errorCode;
        this.info = info;
    }

    public LinkIDCallbackPullErrorCode getErrorCode() {

        return errorCode;
    }

    public String getInfo() {

        return info;
    }
}
