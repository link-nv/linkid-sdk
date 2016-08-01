/*
 * safe-online - linkid-sdk-api
 *
 * Copyright 2006-2016 linkID Inc. All rights reserved.
 * linkID Inc. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.ws.linkid.notifications;

public class LinkIDNotificationAddException extends Exception {

    private final LinkIDNotificationAddErrorCode errorCode;

    public LinkIDNotificationAddException(final String message, final LinkIDNotificationAddErrorCode errorCode) {

        super( String.format( "Error code: \"%s\", message=\"%s\"", errorCode, message ) );
        this.errorCode = errorCode;
    }

    public LinkIDNotificationAddErrorCode getErrorCode() {

        return errorCode;
    }
}
