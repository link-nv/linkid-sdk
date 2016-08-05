/*
 * safe-online - linkid-sdk-api
 *
 * Copyright 2006-2016 linkID Inc. All rights reserved.
 * linkID Inc. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.ws.linkid.applications;

public class LinkIDApplicationAddException extends Exception {

    private final LinkIDApplicationAddErrorCode errorCode;

    public LinkIDApplicationAddException(final String message, final LinkIDApplicationAddErrorCode errorCode) {

        super( String.format( "Error code: \"%s\", message=\"%s\"", errorCode, message ) );
        this.errorCode = errorCode;
    }

    public LinkIDApplicationAddErrorCode getErrorCode() {

        return errorCode;
    }
}
