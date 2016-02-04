/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.ws.linkid.voucher;

public class LinkIDVoucherListRedeemedException extends Exception {

    private final LinkIDVoucherListRedeemedErrorCode errorCode;

    public LinkIDVoucherListRedeemedException(final LinkIDVoucherListRedeemedErrorCode errorCode) {

        this.errorCode = errorCode;
    }

    public LinkIDVoucherListRedeemedErrorCode getErrorCode() {

        return errorCode;
    }
}
