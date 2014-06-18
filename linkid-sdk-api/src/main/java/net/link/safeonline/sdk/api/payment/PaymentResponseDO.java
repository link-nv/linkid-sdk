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

    public static final String ORDER_REF_KEY    = "PaymentResponse.txnId";
    public static final String STATE_KEY        = "PaymentResponse.state";
    public static final String MANDATE_REF_KEY  = "PaymentResponse.mandateRef";
    //
    public static final String DOCDATA__REF_KEY = "PaymentResponse.docdataRef";

    private final String       orderReference;
    private final PaymentState paymentState;
    private final String       mandateReference;
    //
    private final String       docdataReference;

    /**
     * @param orderReference the payment order reference
     * @param paymentState   the payment order state
     */
    public PaymentResponseDO(final String orderReference, final PaymentState paymentState, @Nullable final String mandateReference,
                             @Nullable final String docdataReference) {

        this.orderReference = orderReference;
        this.paymentState = paymentState;
        this.mandateReference = mandateReference;

        this.docdataReference = docdataReference;
    }

    // Helper methods

    public Map<String, String> toMap() {

        Map<String, String> map = new HashMap<String, String>();

        map.put( ORDER_REF_KEY, orderReference );
        map.put( STATE_KEY, paymentState.name() );
        map.put( MANDATE_REF_KEY, mandateReference );

        map.put( DOCDATA__REF_KEY, docdataReference );

        return map;
    }

    @Nullable
    public static PaymentResponseDO fromMap(final Map<String, String> paymentResponseMap) {

        // check map valid
        if (!paymentResponseMap.containsKey( ORDER_REF_KEY ))
            throw new RuntimeException( "Payment response's transaction ID field is not present!" );
        if (!paymentResponseMap.containsKey( STATE_KEY ))
            throw new RuntimeException( "Payment response's state field is not present!" );

        // convert
        return new PaymentResponseDO( paymentResponseMap.get( ORDER_REF_KEY ), PaymentState.parse( paymentResponseMap.get( STATE_KEY ) ),
                paymentResponseMap.get( MANDATE_REF_KEY ), paymentResponseMap.get( DOCDATA__REF_KEY ) );
    }

    // Accessors

    public String getOrderReference() {

        return orderReference;
    }

    public PaymentState getPaymentState() {

        return paymentState;
    }

    public String getMandateReference() {

        return mandateReference;
    }

    public String getDocdataReference() {

        return docdataReference;
    }
}
