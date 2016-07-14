/*
 * safe-online - linkid-sdk-api
 *
 * Copyright 2006-2016 linkID Inc. All rights reserved.
 * linkID Inc. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.ws.linkid.wallet;

public class LinkIDWalletOrganizationRemoveException extends Exception {

    private final LinkIDWalletOrganizationRemoveErrorCode errorCode;

    public LinkIDWalletOrganizationRemoveException(final String message, final LinkIDWalletOrganizationRemoveErrorCode errorCode) {

        super( String.format( "Error code: \"%s\", message=\"%s\"", errorCode, message ) );
        this.errorCode = errorCode;
    }

    public LinkIDWalletOrganizationRemoveErrorCode getErrorCode() {

        return errorCode;
    }

}
