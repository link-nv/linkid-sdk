/*
 * safe-online - linkid-sdk-api
 *
 * Copyright 2006-2016 linkID Inc. All rights reserved.
 * linkID Inc. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.ws.linkid.credentials;

public class LinkIDCredentialRemoveException extends Exception {

    private final LinkIDCredentialRemoveErrorCode errorCode;

    public LinkIDCredentialRemoveException(final String message, final LinkIDCredentialRemoveErrorCode errorCode) {

        super( String.format( "Error code: \"%s\", message=\"%s\"", errorCode, message ) );
        this.errorCode = errorCode;
    }

    public LinkIDCredentialRemoveErrorCode getErrorCode() {

        return errorCode;
    }

}
