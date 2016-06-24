/*
 * safe-online - linkid-sdk-api
 *
 * Copyright 2006-2016 linkID Inc. All rights reserved.
 * linkID Inc. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.ws.linkid.voucher;

public class LinkIDVoucherOrganizationAddPermissionException extends Exception {

    private final LinkIDVoucherOrganizationAddPermissionErrorCode errorCode;

    public LinkIDVoucherOrganizationAddPermissionException(final String message, final LinkIDVoucherOrganizationAddPermissionErrorCode errorCode) {

        super( String.format( "Error code: \"%s\", message=\"%s\"", errorCode, message ) );
        this.errorCode = errorCode;
    }

    public LinkIDVoucherOrganizationAddPermissionErrorCode getErrorCode() {

        return errorCode;
    }

}
