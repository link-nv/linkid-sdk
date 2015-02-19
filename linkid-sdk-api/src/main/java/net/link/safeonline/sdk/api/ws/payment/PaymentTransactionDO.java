package net.link.safeonline.sdk.api.ws.payment;

import java.io.Serializable;
import java.util.Date;
import net.link.safeonline.sdk.api.payment.Currency;
import net.link.safeonline.sdk.api.payment.PaymentState;


/**
 * Created by wvdhaute
 * Date: 18/02/15
 * Time: 16:25
 */
@SuppressWarnings("UnusedDeclaration")
public class PaymentTransactionDO implements Serializable {

    private final PaymentState paymentState;
    private final Date         creationDate;
    private final Date         authorizationDate;
    private final Date         capturedDate;
    private final String       docdataReference;
    private final double       amount;
    private final Currency     currency;

    public PaymentTransactionDO(final PaymentState paymentState, final Date creationDate, final Date authorizationDate, final Date capturedDate,
                                final String docdataReference, final double amount, final Currency currency) {

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

    public PaymentState getPaymentState() {

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

    public Currency getCurrency() {

        return currency;
    }
}
