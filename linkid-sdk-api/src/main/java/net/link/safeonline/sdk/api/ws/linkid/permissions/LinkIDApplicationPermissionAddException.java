/*
 * safe-online - linkid-sdk-api
 *
 * Copyright 2006-2016 linkID Inc. All rights reserved.
 * linkID Inc. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.ws.linkid.permissions;

public class LinkIDApplicationPermissionAddException extends Exception {

    private final LinkIDApplicationPermissionAddErrorCode errorCode;

    public LinkIDApplicationPermissionAddException(final String message, final LinkIDApplicationPermissionAddErrorCode errorCode) {

        super( String.format( "Error code: \"%s\", message=\"%s\"", errorCode, message ) );
        this.errorCode = errorCode;
    }

    public LinkIDApplicationPermissionAddErrorCode getErrorCode() {

        return errorCode;
    }

}
