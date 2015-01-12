package net.link.safeonline.sdk.api.payment;

import java.io.Serializable;


/**
 * Created by wvdhaute
 * Date: 12/01/15
 * Time: 10:08
 */
public class PaymentStatusDO implements Serializable {

    private final PaymentState paymentState;
    private final boolean      captured;

    public PaymentStatusDO(final PaymentState paymentState, final boolean captured) {

        this.paymentState = paymentState;
        this.captured = captured;
    }

    @Override
    public String toString() {

        return String.format( "State: %s, captured: %s", paymentState, captured );
    }

    public PaymentState getPaymentState() {

        return paymentState;
    }

    public boolean isCaptured() {

        return captured;
    }
}
