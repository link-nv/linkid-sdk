/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.payment;

public enum LinkIDPaymentState {

    STARTED,                // payment is being processed
    DEFERRED,               // deferred payment
    WAITING_FOR_UPDATE,     // linkID stopped waiting for status update, SP will be informed on payment status change
    FAILED,                 // payment has failed
    REFUNDED,               // payment has been refunded
    REFUND_STARTED,         // payment refund has started
    PAYED;                  // completed

    public static LinkIDPaymentState parse(final String stateString) {

        for (LinkIDPaymentState paymentState : LinkIDPaymentState.values()) {

            if (paymentState.name().equals( stateString.toUpperCase() )) {
                return paymentState;
            }
        }

        throw new RuntimeException( String.format( "Unsupported payment state %s!", stateString ) );
    }

}
