/*
 * safe-online - linkid-sdk-api
 *
 * Copyright 2006-2016 linkID Inc. All rights reserved.
 * linkID Inc. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.ws.linkid.wallet;

public class LinkIDWalletOrganizationAddException extends Exception {

    private final LinkIDWalletOrganizationAddErrorCode errorCode;

    public LinkIDWalletOrganizationAddException(final String message, final LinkIDWalletOrganizationAddErrorCode errorCode) {

        super( String.format( "Error code: \"%s\", message=\"%s\"", errorCode, message ) );
        this.errorCode = errorCode;
    }

    public LinkIDWalletOrganizationAddErrorCode getErrorCode() {

        return errorCode;
    }

}
