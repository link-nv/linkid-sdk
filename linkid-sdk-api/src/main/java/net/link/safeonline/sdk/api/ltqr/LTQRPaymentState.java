/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.ltqr;

public enum LTQRPaymentState {

    STARTED,                // payment is being processed
    PAYED,                  // completed
    FAILED;                 // payment has failed

    public static LTQRPaymentState parse(final String stateString) {

        for (LTQRPaymentState paymentState : LTQRPaymentState.values()) {

            if (paymentState.name().equals( stateString.toUpperCase() )) {
                return paymentState;
            }
        }

        throw new RuntimeException( String.format( "Unsupported payment state %s!", stateString ) );
    }

}
