/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.payment;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import org.jetbrains.annotations.Nullable;


public class PaymentResponseDO implements Serializable {

    public static final String TXN_ID_KEY = "PaymentResponse.txnId";
    public static final String STATE_KEY  = "PaymentResponse.state";

    private final String       orderReference;
    private final PaymentState paymentState;

    /**
     * @param orderReference the payment order reference
     * @param paymentState   the payment order state
     */
    public PaymentResponseDO(final String orderReference, final PaymentState paymentState) {

        this.orderReference = orderReference;
        this.paymentState = paymentState;
    }

    // Helper methods

    public Map<String, String> toMap() {

        Map<String, String> map = new HashMap<String, String>();

        map.put( TXN_ID_KEY, orderReference );
        map.put( STATE_KEY, paymentState.name() );

        return map;
    }

    @Nullable
    public static PaymentResponseDO fromMap(final Map<String, String> paymentResponseMap) {

        // check map valid
        if (!paymentResponseMap.containsKey( TXN_ID_KEY ))
            throw new RuntimeException( "Payment response's transaction ID field is not present!" );
        if (!paymentResponseMap.containsKey( STATE_KEY ))
            throw new RuntimeException( "Payment response's state field is not present!" );

        // convert
        return new PaymentResponseDO( paymentResponseMap.get( TXN_ID_KEY ), PaymentState.parse( paymentResponseMap.get( STATE_KEY ) ) );
    }

    // Accessors

    public String getOrderReference() {

        return orderReference;
    }

    public PaymentState getPaymentState() {

        return paymentState;
    }
}
