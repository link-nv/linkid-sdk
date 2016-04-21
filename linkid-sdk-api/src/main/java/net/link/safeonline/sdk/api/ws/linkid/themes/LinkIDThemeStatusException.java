/*
 * safe-online - linkid-sdk-api
 *
 * Copyright 2006-2016 linkID Inc. All rights reserved.
 * linkID Inc. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.ws.linkid.themes;

public class LinkIDThemeStatusException extends Exception {

    private final LinkIDThemeStatusErrorCode errorCode;

    public LinkIDThemeStatusException(final String message, final LinkIDThemeStatusErrorCode errorCode) {

        super( message );
        this.errorCode = errorCode;
    }

    public LinkIDThemeStatusErrorCode getErrorCode() {

        return errorCode;
    }
}
