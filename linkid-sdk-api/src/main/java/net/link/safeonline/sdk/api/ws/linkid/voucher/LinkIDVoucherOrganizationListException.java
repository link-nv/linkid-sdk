/*
 * safe-online - linkid-sdk-api
 *
 * Copyright 2006-2016 linkID Inc. All rights reserved.
 * linkID Inc. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.ws.linkid.voucher;

public class LinkIDVoucherOrganizationListException extends Exception {

    private final LinkIDVoucherOrganizationListErrorCode errorCode;

    public LinkIDVoucherOrganizationListException(final String message, final LinkIDVoucherOrganizationListErrorCode errorCode) {

        super( message );
        this.errorCode = errorCode;
    }

    public LinkIDVoucherOrganizationListErrorCode getErrorCode() {

        return errorCode;
    }

}