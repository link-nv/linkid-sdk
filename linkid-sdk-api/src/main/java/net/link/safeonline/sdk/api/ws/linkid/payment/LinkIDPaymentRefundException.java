/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.ws.linkid.payment;

public class LinkIDPaymentRefundException extends Exception {

    private final LinkIDPaymentRefundErrorCode errorCode;

    public LinkIDPaymentRefundException(final LinkIDPaymentRefundErrorCode errorCode) {

        this.errorCode = errorCode;
    }

    public LinkIDPaymentRefundErrorCode getErrorCode() {

        return errorCode;
    }
}
