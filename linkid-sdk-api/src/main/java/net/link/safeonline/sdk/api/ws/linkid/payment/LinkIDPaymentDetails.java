package net.link.safeonline.sdk.api.ws.linkid.payment;

import java.io.Serializable;
import java.util.List;
import net.link.safeonline.sdk.api.payment.LinkIDPaymentTransaction;
import net.link.safeonline.sdk.api.payment.LinkIDWalletTransaction;


/**
 * Created by wvdhaute
 * Date: 18/02/15
 * Time: 16:22
 */
@SuppressWarnings("UnusedDeclaration")
public class LinkIDPaymentDetails implements Serializable {

    private final List<LinkIDPaymentTransaction> transactions;
    private final List<LinkIDWalletTransaction>  walletTransactions;

    public LinkIDPaymentDetails(final List<LinkIDPaymentTransaction> transactions, final List<LinkIDWalletTransaction> walletTransactions) {

        this.transactions = transactions;
        this.walletTransactions = walletTransactions;
    }

    // Accessors

    public List<LinkIDPaymentTransaction> getTransactions() {

        return transactions;
    }

    public List<LinkIDWalletTransaction> getWalletTransactions() {

        return walletTransactions;
    }
}
