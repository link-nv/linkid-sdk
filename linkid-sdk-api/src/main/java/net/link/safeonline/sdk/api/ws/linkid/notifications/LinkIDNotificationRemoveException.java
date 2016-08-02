/*
 * safe-online - linkid-sdk-api
 *
 * Copyright 2006-2016 linkID Inc. All rights reserved.
 * linkID Inc. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.ws.linkid.notifications;

public class LinkIDNotificationRemoveException extends Exception {

    private final LinkIDNotificationRemoveErrorCode errorCode;

    public LinkIDNotificationRemoveException(final String message, final LinkIDNotificationRemoveErrorCode errorCode) {

        super( String.format( "Error code: \"%s\", message=\"%s\"", errorCode, message ) );
        this.errorCode = errorCode;
    }

    public LinkIDNotificationRemoveErrorCode getErrorCode() {

        return errorCode;
    }
}
