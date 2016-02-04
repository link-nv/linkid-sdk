/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.ws.linkid.voucher;

public class LinkIDVoucherListException extends Exception {

    private final LinkIDVoucherListErrorCode errorCode;

    public LinkIDVoucherListException(final LinkIDVoucherListErrorCode errorCode) {

        this.errorCode = errorCode;
    }

    public LinkIDVoucherListErrorCode getErrorCode() {

        return errorCode;
    }
}
