/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.ws.linkid.payment;

public class LinkIDMandatePaymentException extends Exception {

    private final LinkIDMandatePaymentErrorCode linkIDMandatePaymentErrorCode;

    public LinkIDMandatePaymentException(final LinkIDMandatePaymentErrorCode errorCode) {

        super( String.format( "Error code: \"%s\"", errorCode ) );
        this.linkIDMandatePaymentErrorCode = errorCode;
    }

    public LinkIDMandatePaymentErrorCode getErrorCode() {

        return linkIDMandatePaymentErrorCode;
    }
}
