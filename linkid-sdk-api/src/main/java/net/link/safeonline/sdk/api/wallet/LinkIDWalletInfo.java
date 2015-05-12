package net.link.safeonline.sdk.api.wallet;

import java.io.Serializable;
import net.link.safeonline.sdk.api.payment.LinkIDCurrency;


/**
 * Created by wvdhaute
 * Date: 11/05/15
 * Time: 14:10
 */
public class LinkIDWalletInfo implements Serializable {

    private final String         walletId;
    private final double         amount;            // The amount in the wallet ( in cents ). If applicable, else just 0
    private final LinkIDCurrency currency;

    public LinkIDWalletInfo(final String walletId, final double amount, final LinkIDCurrency currency) {

        this.walletId = walletId;
        this.amount = amount;
        this.currency = currency;
    }

    // Helper methods

    @Override
    public String toString() {

        return "LinkIDWalletInfo{" +
               "walletId='" + walletId + '\'' +
               ", amount=" + amount +
               ", currency=" + currency +
               '}';
    }

    // Accessors

    public String getWalletId() {

        return walletId;
    }

    public double getAmount() {

        return amount;
    }

    public LinkIDCurrency getCurrency() {

        return currency;
    }
}
