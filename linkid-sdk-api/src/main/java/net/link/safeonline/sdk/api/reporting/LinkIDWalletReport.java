package net.link.safeonline.sdk.api.reporting;

import java.io.Serializable;
import java.util.List;


/**
 * Created by wvdhaute
 * Date: 19/11/15
 * Time: 10:46
 */
public class LinkIDWalletReport implements Serializable {

    private final long                                total;
    private final List<LinkIDWalletReportTransaction> walletTransactions;

    public LinkIDWalletReport(final long total, final List<LinkIDWalletReportTransaction> walletTransactions) {

        this.total = total;
        this.walletTransactions = walletTransactions;
    }

    // Helper methods

    @Override
    public String toString() {

        return "LinkIDWalletReport{" +
               "total=" + total +
               ", walletTransactions=" + walletTransactions +
               '}';
    }

    // Accessors

    public long getTotal() {

        return total;
    }

    public List<LinkIDWalletReportTransaction> getWalletTransactions() {

        return walletTransactions;
    }
}
