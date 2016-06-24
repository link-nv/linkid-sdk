/*
 * safe-online - linkid-sdk-api
 *
 * Copyright 2006-2016 linkID Inc. All rights reserved.
 * linkID Inc. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.ws.linkid.themes;

public class LinkIDThemeRemoveException extends Exception {

    private final LinkIDThemeRemoveErrorCode errorCode;

    public LinkIDThemeRemoveException(final String message, final LinkIDThemeRemoveErrorCode errorCode) {

        super( String.format( "Error code: \"%s\", message=\"%s\"", errorCode, message ) );
        this.errorCode = errorCode;
    }

    public LinkIDThemeRemoveErrorCode getErrorCode() {

        return errorCode;
    }
}
