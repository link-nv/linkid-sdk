package net.link.safeonline.sdk.api.payment;

import java.io.Serializable;
import java.util.Date;
import org.jetbrains.annotations.Nullable;


/**
 * Created by wvdhaute
 * Date: 30/03/15
 * Time: 16:44
 */
@SuppressWarnings("UnusedDeclaration")
public class LinkIDWalletTransaction implements Serializable {

    private final String         walletId;
    private final String         walletOrganizationId;
    private final Date           creationDate;
    @Nullable
    private final Date           refundedDate;
    @Nullable
    private final Date           committedDate;
    @Nullable
    private final String         transactionId;
    private final double         amount;
    private final LinkIDCurrency currency;
    private final String         walletCoin;
    private final double         refundAmount;
    @Nullable
    private final String         paymentDescription;

    public LinkIDWalletTransaction(final String walletId, final String walletOrganizationId, final Date creationDate, @Nullable final Date refundedDate,
                                   @Nullable final Date committedDate, @Nullable final String transactionId, final double amount, final LinkIDCurrency currency,
                                   final String walletCoin, final double refundAmount, @Nullable final String paymentDescription) {

        this.walletId = walletId;
        this.walletOrganizationId = walletOrganizationId;
        this.creationDate = creationDate;
        this.refundedDate = refundedDate;
        this.committedDate = committedDate;
        this.transactionId = transactionId;
        this.amount = amount;
        this.currency = currency;
        this.walletCoin = walletCoin;
        this.refundAmount = refundAmount;
        this.paymentDescription = paymentDescription;
    }

    // Helper methods

    @Override
    public String toString() {

        return "LinkIDWalletTransaction{" +
               "walletId='" + walletId + '\'' +
               ", walletOrganizationId=" + walletOrganizationId +
               ", creationDate=" + creationDate +
               ", refundedDate=" + refundedDate +
               ", committedDate=" + committedDate +
               ", transactionId='" + transactionId + '\'' +
               ", amount=" + amount +
               ", currency=" + currency +
               ", walletCoin=" + walletCoin +
               ", refundAmount=" + refundAmount +
               ", paymentDescription=" + paymentDescription +
               '}';
    }

    // Accessors

    public String getWalletId() {

        return walletId;
    }

    public String getWalletOrganizationId() {

        return walletOrganizationId;
    }

    public Date getCreationDate() {

        return creationDate;
    }

    @Nullable
    public Date getRefundedDate() {

        return refundedDate;
    }

    @Nullable
    public Date getCommittedDate() {

        return committedDate;
    }

    @Nullable
    public String getTransactionId() {

        return transactionId;
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

    public double getRefundAmount() {

        return refundAmount;
    }

    @Nullable
    public String getPaymentDescription() {

        return paymentDescription;
    }
}
