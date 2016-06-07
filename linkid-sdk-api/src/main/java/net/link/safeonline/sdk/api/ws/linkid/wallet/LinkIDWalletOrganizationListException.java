/*
 * safe-online - linkid-sdk-api
 *
 * Copyright 2006-2016 linkID Inc. All rights reserved.
 * linkID Inc. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.ws.linkid.wallet;

public class LinkIDWalletOrganizationListException extends Exception {

    private final LinkIDWalletOrganizationListErrorCode errorCode;

    public LinkIDWalletOrganizationListException(final String message, final LinkIDWalletOrganizationListErrorCode errorCode) {

        super( message );
        this.errorCode = errorCode;
    }

    public LinkIDWalletOrganizationListErrorCode getErrorCode() {

        return errorCode;
    }

}
