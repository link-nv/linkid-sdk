/*
 * safe-online - linkid-sdk-api
 *
 * Copyright 2006-2016 linkID Inc. All rights reserved.
 * linkID Inc. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.ws.linkid.permissions;

public class LinkIDApplicationPermissionRemoveException extends Exception {

    private final LinkIDApplicationPermissionRemoveErrorCode errorCode;

    public LinkIDApplicationPermissionRemoveException(final String message, final LinkIDApplicationPermissionRemoveErrorCode errorCode) {

        super( String.format( "Error code: \"%s\", message=\"%s\"", errorCode, message ) );
        this.errorCode = errorCode;
    }

    public LinkIDApplicationPermissionRemoveErrorCode getErrorCode() {

        return errorCode;
    }

}
