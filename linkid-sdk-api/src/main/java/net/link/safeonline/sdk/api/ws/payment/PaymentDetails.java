package net.link.safeonline.sdk.api.ws.payment;

import java.io.Serializable;
import java.util.List;


/**
 * Created by wvdhaute
 * Date: 18/02/15
 * Time: 16:22
 */
@SuppressWarnings("UnusedDeclaration")
public class PaymentDetails implements Serializable {

    private final List<PaymentTransactionDO> transactions;
    private final List<WalletTransactionDO>  walletTransactions;

    public PaymentDetails(final List<PaymentTransactionDO> transactions, final List<WalletTransactionDO> walletTransactions) {

        this.transactions = transactions;
        this.walletTransactions = walletTransactions;
    }

    // Accessors

    public List<PaymentTransactionDO> getTransactions() {

        return transactions;
    }

    public List<WalletTransactionDO> getWalletTransactions() {

        return walletTransactions;
    }
}
