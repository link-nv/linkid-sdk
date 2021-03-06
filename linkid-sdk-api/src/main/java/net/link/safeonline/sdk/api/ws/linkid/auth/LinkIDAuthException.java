/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.ws.linkid.auth;

/**
 * Created by wvdhaute
 * Date: 14/01/14
 * Time: 11:03
 */
public class LinkIDAuthException extends Exception {

    private final LinkIDAuthErrorCode errorCode;

    public LinkIDAuthException(final LinkIDAuthErrorCode errorCode, final String message) {

        super( String.format( "Error code: \"%s\", message=\"%s\"", errorCode, message ) );
        this.errorCode = errorCode;
    }

    public LinkIDAuthErrorCode getErrorCode() {

        return errorCode;
    }
}
