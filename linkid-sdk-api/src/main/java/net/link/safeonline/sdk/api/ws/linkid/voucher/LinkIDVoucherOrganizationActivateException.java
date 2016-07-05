/*
 * safe-online - linkid-sdk-api
 *
 * Copyright 2006-2016 linkID Inc. All rights reserved.
 * linkID Inc. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.ws.linkid.voucher;

public class LinkIDVoucherOrganizationActivateException extends Exception {

    private final LinkIDVoucherOrganizationActivateErrorCode errorCode;

    public LinkIDVoucherOrganizationActivateException(final String message, final LinkIDVoucherOrganizationActivateErrorCode errorCode) {

        super( String.format( "Error code: \"%s\", message=\"%s\"", errorCode, message ) );
        this.errorCode = errorCode;
    }

    public LinkIDVoucherOrganizationActivateErrorCode getErrorCode() {

        return errorCode;
    }

}
