package net.link.safeonline.sdk.api.payment;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import org.jetbrains.annotations.Nullable;


public class PaymentResponseDO implements Serializable {

    public static final String ORDER_REF_KEY = "PaymentResponse.orderRef";
    public static final String STATE_KEY     = "PaymentResponse.state";

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

        map.put( ORDER_REF_KEY, orderReference );
        map.put( STATE_KEY, paymentState.name() );

        return map;
    }

    @Nullable
    public static PaymentResponseDO fromMap(final Map<String, String> paymentResponseMap) {

        // check map valid
        if (!paymentResponseMap.containsKey( ORDER_REF_KEY ))
            throw new RuntimeException( "Payment response's order ref field is not present!" );
        if (!paymentResponseMap.containsKey( STATE_KEY ))
            throw new RuntimeException( "Payment response's state field is not present!" );

        // convert
        return new PaymentResponseDO( paymentResponseMap.get( ORDER_REF_KEY ), PaymentState.parse( paymentResponseMap.get( STATE_KEY ) ) );
    }

    // Accessors

    public String getOrderReference() {

        return orderReference;
    }

    public PaymentState getPaymentState() {

        return paymentState;
    }
}
