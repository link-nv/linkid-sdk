/*
 * safe-online - linkid-sdk-api
 *
 * Copyright 2006-2016 linkID Inc. All rights reserved.
 * linkID Inc. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.ws.linkid.voucher;

public class LinkIDVoucherOrganizationListUsersException extends Exception {

    private final LinkIDVoucherOrganizationListUsersErrorCode errorCode;

    public LinkIDVoucherOrganizationListUsersException(final String message, final LinkIDVoucherOrganizationListUsersErrorCode errorCode) {

        super( message );
        this.errorCode = errorCode;
    }

    public LinkIDVoucherOrganizationListUsersErrorCode getErrorCode() {

        return errorCode;
    }

}
