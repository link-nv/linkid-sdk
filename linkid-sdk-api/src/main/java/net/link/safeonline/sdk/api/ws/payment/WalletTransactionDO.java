package net.link.safeonline.sdk.api.ws.payment;

import java.io.Serializable;
import java.util.Date;
import net.link.safeonline.sdk.api.payment.Currency;


/**
 * Created by wvdhaute
 * Date: 18/02/15
 * Time: 16:27
 */
@SuppressWarnings("UnusedDeclaration")
public class WalletTransactionDO implements Serializable {

    private final String   walletId;
    private final Date     creationDate;
    private final String   transactionId;
    private final double   amount;
    private final Currency currency;

    public WalletTransactionDO(final String walletId, final Date creationDate, final String transactionId, final double amount, final Currency currency) {

        this.walletId = walletId;
        this.creationDate = creationDate;
        this.transactionId = transactionId;
        this.amount = amount;
        this.currency = currency;
    }

    @Override
    public String toString() {

        return "WalletTransactionDO{" +
               "walletId='" + walletId + '\'' +
               ", creationDate=" + creationDate +
               ", transactionId='" + transactionId + '\'' +
               ", amount=" + amount +
               ", currency=" + currency +
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

    public Currency getCurrency() {

        return currency;
    }
}
