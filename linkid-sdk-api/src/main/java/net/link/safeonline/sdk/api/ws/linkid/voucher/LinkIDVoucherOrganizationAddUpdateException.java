/*
 * safe-online - linkid-sdk-api
 *
 * Copyright 2006-2016 linkID Inc. All rights reserved.
 * linkID Inc. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.ws.linkid.voucher;

public class LinkIDVoucherOrganizationAddUpdateException extends Exception {

    private final LinkIDVoucherOrganizationAddUpdateErrorCode errorCode;

    public LinkIDVoucherOrganizationAddUpdateException(final String message, final LinkIDVoucherOrganizationAddUpdateErrorCode errorCode) {

        super( String.format( "Error code: \"%s\", message=\"%s\"", errorCode, message ) );
        this.errorCode = errorCode;
    }

    public LinkIDVoucherOrganizationAddUpdateErrorCode getErrorCode() {

        return errorCode;
    }

}
