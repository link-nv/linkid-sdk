/*
 * safe-online - linkid-sdk-api
 *
 * Copyright 2006-2016 linkID Inc. All rights reserved.
 * linkID Inc. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.ws.linkid.wallet;

public class LinkIDWalletOrganizationUpdateException extends Exception {

    private final LinkIDWalletOrganizationUpdateErrorCode errorCode;

    public LinkIDWalletOrganizationUpdateException(final String message, final LinkIDWalletOrganizationUpdateErrorCode errorCode) {

        super( String.format( "Error code: \"%s\", message=\"%s\"", errorCode, message ) );
        this.errorCode = errorCode;
    }

    public LinkIDWalletOrganizationUpdateErrorCode getErrorCode() {

        return errorCode;
    }

}
