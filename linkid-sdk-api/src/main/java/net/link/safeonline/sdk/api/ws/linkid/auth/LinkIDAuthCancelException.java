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
public class LinkIDAuthCancelException extends Exception {

    private final LinkIDAuthCancelErrorCode errorCode;
    private final String                    info;

    public LinkIDAuthCancelException(final LinkIDAuthCancelErrorCode errorCode, final String info) {

        super( String.format( "Error code: \"%s\", message=\"%s\"", errorCode, info ) );
        this.errorCode = errorCode;
        this.info = info;
    }

    public LinkIDAuthCancelErrorCode getErrorCode() {

        return errorCode;
    }

    public String getInfo() {

        return info;
    }
}
