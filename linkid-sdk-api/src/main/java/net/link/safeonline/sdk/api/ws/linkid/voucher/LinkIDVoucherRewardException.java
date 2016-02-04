/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.ws.linkid.voucher;

public class LinkIDVoucherRewardException extends Exception {

    private final LinkIDVoucherRewardErrorCode errorCode;

    public LinkIDVoucherRewardException(final LinkIDVoucherRewardErrorCode errorCode) {

        this.errorCode = errorCode;
    }

    public LinkIDVoucherRewardErrorCode getErrorCode() {

        return errorCode;
    }
}
