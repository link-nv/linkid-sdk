/*
 * safe-online - linkid-sdk-api
 *
 * Copyright 2006-2016 linkID Inc. All rights reserved.
 * linkID Inc. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.ws.linkid.voucher;

public class LinkIDVoucherOrganizationHistoryException extends Exception {

    private final LinkIDVoucherOrganizationHistoryErrorCode errorCode;

    public LinkIDVoucherOrganizationHistoryException(final String message, final LinkIDVoucherOrganizationHistoryErrorCode errorCode) {

        super( message );
        this.errorCode = errorCode;
    }

    public LinkIDVoucherOrganizationHistoryErrorCode getErrorCode() {

        return errorCode;
    }

}
