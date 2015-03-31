package net.link.safeonline.sdk.api.ws.payment;

import java.io.Serializable;
import java.util.Date;
import net.link.safeonline.sdk.api.payment.LinkIDCurrency;
import net.link.safeonline.sdk.api.payment.LinkIDPaymentState;


/**
 * Created by wvdhaute
 * Date: 18/02/15
 * Time: 16:25
 */
@SuppressWarnings("UnusedDeclaration")
public class PaymentTransactionDO implements Serializable {

    private final LinkIDPaymentState paymentState;
    private final Date               creationDate;
    private final Date               authorizationDate;
    private final Date               capturedDate;
    private final String             docdataReference;
    private final double             amount;
    private final LinkIDCurrency     currency;

    public PaymentTransactionDO(final LinkIDPaymentState paymentState, final Date creationDate, final Date authorizationDate, final Date capturedDate,
                                final String docdataReference, final double amount, final LinkIDCurrency currency) {

        this.paymentState = paymentState;
        this.creationDate = creationDate;
        this.authorizationDate = authorizationDate;
        this.capturedDate = capturedDate;
        this.docdataReference = docdataReference;
        this.amount = amount;
        this.currency = currency;
    }

    @Override
    public String toString() {

        return "PaymentTransactionDO{" +
               "paymentState=" + paymentState +
               ", creationDate=" + creationDate +
               ", authorizationDate=" + authorizationDate +
               ", capturedDate=" + capturedDate +
               ", docdataReference='" + docdataReference + '\'' +
               ", amount=" + amount +
               ", currency=" + currency +
               '}';
    }

    // Accessors

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

    public String getDocdataReference() {

        return docdataReference;
    }

    public double getAmount() {

        return amount;
    }

    public LinkIDCurrency getCurrency() {

        return currency;
    }
}
