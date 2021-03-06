/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.ws.linkid.voucher;

public class LinkIDVoucherRedeemException extends Exception {

    private final LinkIDVoucherRedeemErrorCode errorCode;

    public LinkIDVoucherRedeemException(final LinkIDVoucherRedeemErrorCode errorCode) {

        super( String.format( "Error code: \"%s\"", errorCode ) );
        this.errorCode = errorCode;
    }

    public LinkIDVoucherRedeemErrorCode getErrorCode() {

        return errorCode;
    }
}
