/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.ws.linkid.payment;

public class LinkIDPaymentStatusException extends Exception {

    private final LinkIDPaymentStatusErrorCode errorCode;

    public LinkIDPaymentStatusException(final LinkIDPaymentStatusErrorCode errorCode) {

        super( String.format( "Error code: \"%s\"", errorCode ) );
        this.errorCode = errorCode;
    }

    public LinkIDPaymentStatusErrorCode getErrorCode() {

        return errorCode;
    }
}
