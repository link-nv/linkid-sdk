/*
 * safe-online - linkid-sdk-api
 *
 * Copyright 2006-2016 linkID Inc. All rights reserved.
 * linkID Inc. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.ws.linkid.voucher;

public class LinkIDVoucherOrganizationListPermissionsException extends Exception {

    private final LinkIDVoucherOrganizationListPermissionsErrorCode errorCode;

    public LinkIDVoucherOrganizationListPermissionsException(final String message, final LinkIDVoucherOrganizationListPermissionsErrorCode errorCode) {

        super( message );
        this.errorCode = errorCode;
    }

    public LinkIDVoucherOrganizationListPermissionsErrorCode getErrorCode() {

        return errorCode;
    }

}
