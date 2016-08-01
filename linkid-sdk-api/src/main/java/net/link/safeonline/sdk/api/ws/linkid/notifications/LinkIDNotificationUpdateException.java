/*
 * safe-online - linkid-sdk-api
 *
 * Copyright 2006-2016 linkID Inc. All rights reserved.
 * linkID Inc. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.ws.linkid.notifications;

public class LinkIDNotificationUpdateException extends Exception {

    private final LinkIDNotificationUpdateErrorCode errorCode;

    public LinkIDNotificationUpdateException(final String message, final LinkIDNotificationUpdateErrorCode errorCode) {

        super( String.format( "Error code: \"%s\", message=\"%s\"", errorCode, message ) );
        this.errorCode = errorCode;
    }

    public LinkIDNotificationUpdateErrorCode getErrorCode() {

        return errorCode;
    }
}
