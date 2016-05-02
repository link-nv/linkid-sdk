/*
 * safe-online - linkid-sdk-api
 *
 * Copyright 2006-2016 linkID Inc. All rights reserved.
 * linkID Inc. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.ws.linkid.voucher;

public class LinkIDVoucherOrganizationRemovePermissionException extends Exception {

    private final LinkIDVoucherOrganizationRemovePermissionErrorCode errorCode;

    public LinkIDVoucherOrganizationRemovePermissionException(final String message, final LinkIDVoucherOrganizationRemovePermissionErrorCode errorCode) {

        super( message );
        this.errorCode = errorCode;
    }

    public LinkIDVoucherOrganizationRemovePermissionErrorCode getErrorCode() {

        return errorCode;
    }

}
