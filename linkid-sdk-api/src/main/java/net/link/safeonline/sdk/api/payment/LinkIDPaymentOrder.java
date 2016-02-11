package net.link.safeonline.sdk.api.payment;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import org.jetbrains.annotations.Nullable;


/**
 * Created by wvdhaute
 * Date: 30/03/15
 * Time: 16:41
 */
@SuppressWarnings("UnusedDeclaration")
public class LinkIDPaymentOrder implements Serializable {

    private final Date                           date;
    private final double                         amount;
    private final LinkIDCurrency                 currency;
    private final String                         walletCoin;
    private final String                         description;
    private final LinkIDPaymentState             paymentState;
    private final double                         amountPayed;
    private final double                         amountRefunded;
    private final boolean                        authorized;
    @Nullable
    private final Date                           authorizedDate;
    private final boolean                        captured;
    @Nullable
    private final Date                           capturedDate;
    private final boolean                        refunded;
    @Nullable
    private final Date                           refundedDate;
    private final String                         orderReference;
    private final String                         userId;
    private final String                         email;
    private final String                         givenName;
    private final String                         familyName;
    private final List<LinkIDPaymentTransaction> transactions;
    private final List<LinkIDWalletTransaction>  walletTransactions;

    public LinkIDPaymentOrder(final Date date, final double amount, final LinkIDCurrency currency, final String walletCoin, final String description,
                              final LinkIDPaymentState paymentState, final double amountPayed, final double amountRefunded, final boolean authorized,
                              @Nullable final Date authorizedDate, final boolean captured, @Nullable final Date capturedDate, final boolean refunded,
                              @Nullable final Date refundedDate, final String orderReference, final String userId, final String email, final String givenName,
                              final String familyName, final List<LinkIDPaymentTransaction> transactions,
                              final List<LinkIDWalletTransaction> walletTransactions) {

        this.date = date;
        this.amount = amount;
        this.currency = currency;
        this.walletCoin = walletCoin;
        this.description = description;
        this.paymentState = paymentState;
        this.amountPayed = amountPayed;
        this.amountRefunded = amountRefunded;
        this.authorized = authorized;
        this.authorizedDate = authorizedDate;
        this.captured = captured;
        this.capturedDate = capturedDate;
        this.refunded = refunded;
        this.refundedDate = refundedDate;
        this.orderReference = orderReference;
        this.userId = userId;
        this.email = email;
        this.givenName = givenName;
        this.familyName = familyName;
        this.transactions = transactions;
        this.walletTransactions = walletTransactions;
    }

    // Helper methods

    @Override
    public String toString() {

        return "LinkIDPaymentOrder{" +
               "date=" + date +
               ", amount=" + amount +
               ", currency=" + currency +
               ", walletCoin=" + walletCoin +
               ", description='" + description + '\'' +
               ", paymentState=" + paymentState +
               ", amountPayed=" + amountPayed +
               ", amountRefunded=" + amountRefunded +
               ", authorized=" + authorized +
               ", authorizedDate=" + authorizedDate +
               ", captured=" + captured +
               ", capturedDate=" + capturedDate +
               ", refunded=" + refunded +
               ", refundedDate=" + refundedDate +
               ", orderReference='" + orderReference + '\'' +
               ", userId='" + userId + '\'' +
               ", email='" + email + '\'' +
               ", givenName='" + givenName + '\'' +
               ", familyName='" + familyName + '\'' +
               "\n, transactions=" + transactions +
               "\n, walletTransactions=" + walletTransactions +
               '}';
    }

    // Accessors

    public Date getDate() {

        return date;
    }

    public double getAmount() {

        return amount;
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

    public LinkIDPaymentState getPaymentState() {

        return paymentState;
    }

    public double getAmountPayed() {

        return amountPayed;
    }

    public double getAmountRefunded() {

        return amountRefunded;
    }

    public boolean isAuthorized() {

        return authorized;
    }

    @Nullable
    public Date getAuthorizedDate() {

        return authorizedDate;
    }

    public boolean isCaptured() {

        return captured;
    }

    @Nullable
    public Date getCapturedDate() {

        return capturedDate;
    }

    public boolean isRefunded() {

        return refunded;
    }

    @Nullable
    public Date getRefundedDate() {

        return refundedDate;
    }

    public String getOrderReference() {

        return orderReference;
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

    public List<LinkIDPaymentTransaction> getTransactions() {

        return transactions;
    }

    public List<LinkIDWalletTransaction> getWalletTransactions() {

        return walletTransactions;
    }
}
