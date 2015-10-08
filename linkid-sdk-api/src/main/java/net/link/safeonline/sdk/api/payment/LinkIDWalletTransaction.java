package net.link.safeonline.sdk.api.payment;

import java.io.Serializable;
import java.util.Date;


/**
 * Created by wvdhaute
 * Date: 30/03/15
 * Time: 16:44
 */
@SuppressWarnings("UnusedDeclaration")
public class LinkIDWalletTransaction implements Serializable {

    private final String         walletId;
    private final Date           creationDate;
    private final String         transactionId;
    private final double         amount;
    private final LinkIDCurrency currency;
    private final String         walletCoin;
    private final double         refundAmount;

    public LinkIDWalletTransaction(final String walletId, final Date creationDate, final String transactionId, final double amount,
                                   final LinkIDCurrency currency, final String walletCoin, final double refundAmount) {

        this.walletId = walletId;
        this.creationDate = creationDate;
        this.transactionId = transactionId;
        this.amount = amount;
        this.currency = currency;
        this.walletCoin = walletCoin;
        this.refundAmount = refundAmount;
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
               '}';
    }

    // Accessors

    public String getWalletId() {

        return walletId;
    }

    public Date getCreationDate() {

        return creationDate;
    }

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
}
