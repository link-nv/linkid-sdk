/*
 * safe-online - linkid-sdk-api
 *
 * Copyright 2006-2016 linkID Inc. All rights reserved.
 * linkID Inc. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.ws.linkid.voucher;

public class LinkIDVoucherOrganizationRemoveException extends Exception {

    private final LinkIDVoucherOrganizationRemoveErrorCode errorCode;

    public LinkIDVoucherOrganizationRemoveException(final String message, final LinkIDVoucherOrganizationRemoveErrorCode errorCode) {

        super( message );
        this.errorCode = errorCode;
    }

    public LinkIDVoucherOrganizationRemoveErrorCode getErrorCode() {

        return errorCode;
    }

}
