package net.link.safeonline.sdk.api.payment;

import java.io.Serializable;
import org.jetbrains.annotations.Nullable;


/**
 * Payment amount in normal currency
 * <p/>
 * Created by wvdhaute
 * Date: 26/06/15
 * Time: 08:07
 */
public class LinkIDPaymentAmount implements Serializable {

    private final double         amount;
    @Nullable
    private final LinkIDCurrency currency;          // currency, optional, if not specified, walletCoin is required
    @Nullable
    private final String         walletCoin;        // urn of the coin to be used, optional, if not specified currency is required

    @SuppressWarnings("NullableProblems")
    public LinkIDPaymentAmount(final double amount, final LinkIDCurrency currency) {

        this.amount = amount;
        this.currency = currency;
        this.walletCoin = null;
    }

    @SuppressWarnings("NullableProblems")
    public LinkIDPaymentAmount(final double amount, final String walletCoin) {

        this.amount = amount;
        this.walletCoin = walletCoin;
        this.currency = null;
    }

    public LinkIDPaymentAmount(final double amount, @Nullable final LinkIDCurrency currency, @Nullable final String walletCoin) {

        this.amount = amount;
        this.currency = currency;
        this.walletCoin = walletCoin;
    }

    // Helper methods

    @Override
    public String toString() {

        return "LinkIDPaymentAmount{" +
               "amount=" + amount +
               ", currency=" + currency +
               ", walletCoin='" + walletCoin + '\'' +
               '}';
    }

    // Accessors

    public double getAmount() {

        return amount;
    }

    @Nullable
    public LinkIDCurrency getCurrency() {

        return currency;
    }

    @Nullable
    public String getWalletCoin() {

        return walletCoin;
    }
}
