package net.link.safeonline.sdk.api.payment;

import java.io.Serializable;
import java.util.Date;
import org.jetbrains.annotations.Nullable;


/**
 * Created by wvdhaute
 * Date: 29/08/14
 * Time: 14:14
 */
@SuppressWarnings("UnusedDeclaration")
public class LinkIDPaymentTransaction implements Serializable {

    private final String                  id;
    private final LinkIDPaymentMethodType paymentMethodType;
    private final String                  paymentMethod;
    private final LinkIDPaymentState      paymentState;
    private final Date                    creationDate;
    private final Date                    authorizationDate;
    private final Date                    capturedDate;
    @Nullable
    private final Date                    refundedDate;
    private final String                  docdataReference;
    private final double                  amount;
    private final LinkIDCurrency          currency;
    private final double                  refundAmount;

    public LinkIDPaymentTransaction(final String id, final LinkIDPaymentMethodType paymentMethodType, final String paymentMethod,
                                    final LinkIDPaymentState paymentState, final Date creationDate, final Date authorizationDate, final Date capturedDate,
                                    @Nullable final Date refundedDate, final String docdataReference, final double amount, final LinkIDCurrency currency,
                                    final double refundAmount) {

        this.id = id;
        this.paymentMethodType = paymentMethodType;
        this.paymentMethod = paymentMethod;
        this.paymentState = paymentState;
        this.creationDate = creationDate;
        this.authorizationDate = authorizationDate;
        this.capturedDate = capturedDate;
        this.refundedDate = refundedDate;
        this.docdataReference = docdataReference;
        this.amount = amount;
        this.currency = currency;
        this.refundAmount = refundAmount;
    }

    // Helper methods

    @Override
    public String toString() {

        return "LinkIDPaymentTransaction{" +
               "id=" + id +
               ", paymentMethodType='" + paymentMethodType + '\'' +
               ", paymentMethod='" + paymentMethod + '\'' +
               ", paymentState=" + paymentState +
               ", creationDate=" + creationDate +
               ", authorizationDate=" + authorizationDate +
               ", capturedDate=" + capturedDate +
               ", refundedDate=" + refundedDate +
               ", docdataReference='" + docdataReference + '\'' +
               ", amount=" + amount +
               ", currency=" + currency +
               ", refundAmount=" + refundAmount +
               '}';
    }

    // Accessors

    public String getId() {

        return id;
    }

    public LinkIDPaymentMethodType getPaymentMethodType() {

        return paymentMethodType;
    }

    public String getPaymentMethod() {

        return paymentMethod;
    }

    public LinkIDPaymentState getPaymentState() {

        return paymentState;
    }

    public Date getCreationDate() {

        return creationDate;
    }

    public Date getAuthorizationDate() {

        return authorizationDate;
    }

    public Date getCapturedDate() {

        return capturedDate;
    }

    @Nullable
    public Date getRefundedDate() {

        return refundedDate;
    }

    public String getDocdataReference() {

        return docdataReference;
    }

    public double getAmount() {

        return amount;
    }

    public LinkIDCurrency getCurrency() {

        return currency;
    }

    public double getRefundAmount() {

        return refundAmount;
    }
}
