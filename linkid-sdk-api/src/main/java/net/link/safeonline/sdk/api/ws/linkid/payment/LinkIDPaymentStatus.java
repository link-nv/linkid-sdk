package net.link.safeonline.sdk.api.ws.linkid.payment;

import java.io.Serializable;
import java.util.Date;
import net.link.safeonline.sdk.api.payment.LinkIDCurrency;
import net.link.safeonline.sdk.api.payment.LinkIDPaymentState;


/**
 * Created by wvdhaute
 * Date: 12/01/15
 * Time: 10:08
 */
@SuppressWarnings("unused")
public class LinkIDPaymentStatus implements Serializable {

    private final String               orderReference;
    private final String               userId;
    private final LinkIDPaymentState   paymentState;
    private final boolean              authorized;
    private final boolean              captured;
    private final double               amountPayed;
    private final double               amount;
    private final double               refundAmount;
    private final LinkIDCurrency       currency;
    private final String               walletCoin;
    private final String               description;
    private final String               profile;
    private final Date                 created;
    private final String               mandateReference;
    private final LinkIDPaymentDetails paymentDetails;

    public LinkIDPaymentStatus(final String orderReference, final String userId, final LinkIDPaymentState paymentState, final boolean authorized,
                               final boolean captured, final double amountPayed, final double amount, final double refundAmount, final LinkIDCurrency currency,
                               final String walletCoin, final String description, final String profile, final Date created, final String mandateReference,
                               final LinkIDPaymentDetails paymentDetails) {

        this.orderReference = orderReference;
        this.userId = userId;
        this.paymentState = paymentState;
        this.authorized = authorized;
        this.captured = captured;
        this.amountPayed = amountPayed;
        this.amount = amount;
        this.refundAmount = refundAmount;
        this.currency = currency;
        this.walletCoin = walletCoin;
        this.description = description;
        this.profile = profile;
        this.created = created;
        this.mandateReference = mandateReference;
        this.paymentDetails = paymentDetails;
    }

    @Override
    public String toString() {

        return "LinkIDPaymentStatus{" +
               "orderReference='" + orderReference + '\'' +
               ", userId='" + userId + '\'' +
               ", paymentState=" + paymentState +
               ", authorized=" + authorized +
               ", captured=" + captured +
               ", amountPayed=" + amountPayed +
               ", amount=" + amount +
               ", refundAmount=" + refundAmount +
               ", currency=" + currency +
               ", walletCoin=" + walletCoin +
               ", description='" + description + '\'' +
               ", profile='" + profile + '\'' +
               ", created=" + created +
               ", mandateReference='" + mandateReference + '\'' +
               ", paymentDetails=" + paymentDetails +
               '}';
    }

    public String getOrderReference() {

        return orderReference;
    }

    public String getUserId() {

        return userId;
    }

    public LinkIDPaymentState getPaymentState() {

        return paymentState;
    }

    public boolean isAuthorized() {

        return authorized;
    }

    public boolean isCaptured() {

        return captured;
    }

    public double getAmountPayed() {

        return amountPayed;
    }

    public double getAmount() {

        return amount;
    }

    public double getRefundAmount() {

        return refundAmount;
    }

    public LinkIDCurrency getCurrency() {

        return currency;
    }

    public String getWalletCoin() {

        return walletCoin;
    }

    public String getDescription() {

        return description;
    }

    public String getProfile() {

        return profile;
    }

    public Date getCreated() {

        return created;
    }

    public String getMandateReference() {

        return mandateReference;
    }

    public LinkIDPaymentDetails getPaymentDetails() {

        return paymentDetails;
    }
}
