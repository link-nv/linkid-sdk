/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.ws.linkid.payment;

public class LinkIDPaymentCaptureException extends Exception {

    private final LinkIDPaymentCaptureErrorCode errorCode;

    public LinkIDPaymentCaptureException(final LinkIDPaymentCaptureErrorCode errorCode) {

        this.errorCode = errorCode;
    }

    public LinkIDPaymentCaptureErrorCode getErrorCode() {

        return errorCode;
    }
}
