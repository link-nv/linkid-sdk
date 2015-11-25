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
    private final Date           creationDate;
    @Nullable
    private final String         transactionId;
    private final double         amount;
    private final LinkIDCurrency currency;
    private final String         walletCoin;
    private final double         refundAmount;
    @Nullable
    private final String         paymentDescription;

    public LinkIDWalletTransaction(final String walletId, final Date creationDate, @Nullable final String transactionId, final double amount,
                                   final LinkIDCurrency currency, final String walletCoin, final double refundAmount,
                                   @Nullable final String paymentDescription) {

        this.walletId = walletId;
        this.creationDate = creationDate;
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
               ", creationDate=" + creationDate +
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

    public Date getCreationDate() {

        return creationDate;
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
