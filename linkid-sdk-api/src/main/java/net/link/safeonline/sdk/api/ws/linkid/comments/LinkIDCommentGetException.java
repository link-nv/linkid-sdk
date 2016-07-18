/*
 * safe-online - linkid-sdk-api
 *
 * Copyright 2006-2016 linkID Inc. All rights reserved.
 * linkID Inc. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.ws.linkid.comments;

public class LinkIDCommentGetException extends Exception {

    private final LinkIDCommentGetErrorCode errorCode;

    public LinkIDCommentGetException(final String message, final LinkIDCommentGetErrorCode errorCode) {

        super( String.format( "Error code: \"%s\", message=\"%s\"", errorCode, message ) );
        this.errorCode = errorCode;
    }

    public LinkIDCommentGetErrorCode getErrorCode() {

        return errorCode;
    }
}
