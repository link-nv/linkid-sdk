package net.link.safeonline.sdk.api.payment;

import java.io.Serializable;
import java.util.Date;


/**
 * Created by wvdhaute
 * Date: 29/08/14
 * Time: 14:14
 */
@SuppressWarnings("UnusedDeclaration")
public class PaymentTransactionDO implements Serializable {

    private final Date         date;
    private final double       amount;
    private final Currency     currency;
    private final String       paymentMethod;
    private final String       description;
    private final PaymentState paymentState;
    private final boolean      paid;
    private final String       orderReference;
    private final String       docdataReference;
    private final String       userId;
    private final String       email;
    private final String       givenName;
    private final String       familyName;

    public PaymentTransactionDO(final Date date, final double amount, final Currency currency, final String paymentMethod, final String description,
                                final PaymentState paymentState, final boolean paid, final String orderReference, final String docdataReference,
                                final String userId, final String email, final String givenName, final String familyName) {

        this.date = date;
        this.amount = amount;
        this.currency = currency;
        this.paymentMethod = paymentMethod;
        this.description = description;
        this.paymentState = paymentState;
        this.paid = paid;
        this.orderReference = orderReference;
        this.docdataReference = docdataReference;
        this.userId = userId;
        this.email = email;
        this.givenName = givenName;
        this.familyName = familyName;
    }

    // Accessors

    public Date getDate() {

        return date;
    }

    public double getAmount() {

        return amount;
    }

    public Currency getCurrency() {

        return currency;
    }

    public String getPaymentMethod() {

        return paymentMethod;
    }

    public String getDescription() {

        return description;
    }

    public PaymentState getPaymentState() {

        return paymentState;
    }

    public boolean isPaid() {

        return paid;
    }

    public String getOrderReference() {

        return orderReference;
    }

    public String getDocdataReference() {

        return docdataReference;
    }

    public String getUserId() {

        return userId;
    }

    public String getEmail() {

        return email;
    }

    public String getGivenName() {

        return givenName;
    }

    public String getFamilyName() {

        return familyName;
    }
}
