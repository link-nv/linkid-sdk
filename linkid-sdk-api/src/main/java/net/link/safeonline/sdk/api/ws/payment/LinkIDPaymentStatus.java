package net.link.safeonline.sdk.api.ws.payment;

import java.io.Serializable;
import net.link.safeonline.sdk.api.payment.LinkIDPaymentState;


/**
 * Created by wvdhaute
 * Date: 12/01/15
 * Time: 10:08
 */
public class LinkIDPaymentStatus implements Serializable {

    private final LinkIDPaymentState   paymentState;
    private final boolean              captured;
    private final double               amountPayed;
    private final LinkIDPaymentDetails paymentDetails;

    public LinkIDPaymentStatus(final LinkIDPaymentState paymentState, final boolean captured, final double amountPayed,
                               final LinkIDPaymentDetails paymentDetails) {

        this.paymentState = paymentState;
        this.captured = captured;
        this.amountPayed = amountPayed;
        this.paymentDetails = paymentDetails;
    }

    @Override
    public String toString() {

        return "PaymentStatusDO{" +
               "paymentState=" + paymentState +
               ", captured=" + captured +
               ", amountPayed=" + amountPayed +
               ", paymentDetails=" + paymentDetails +
               '}';
    }

    public LinkIDPaymentState getPaymentState() {

        return paymentState;
    }

    public boolean isCaptured() {

        return captured;
    }

    public double getAmountPayed() {

        return amountPayed;
    }

    public LinkIDPaymentDetails getPaymentDetails() {

        return paymentDetails;
    }
}
