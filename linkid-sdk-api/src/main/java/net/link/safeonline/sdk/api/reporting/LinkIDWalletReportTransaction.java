package net.link.safeonline.sdk.api.reporting;

import java.util.Date;
import net.link.safeonline.sdk.api.payment.LinkIDCurrency;
import net.link.safeonline.sdk.api.payment.LinkIDWalletTransaction;


/**
 * Created by wvdhaute
 * Date: 30/03/15
 * Time: 16:44
 */
@SuppressWarnings("UnusedDeclaration")
public class LinkIDWalletReportTransaction extends LinkIDWalletTransaction {

    private final String                 userId;
    private final String                 applicationName;
    private final LinkIDWalletReportType type;

    public LinkIDWalletReportTransaction(final String walletId, final Date creationDate, final String transactionId, final double amount,
                                         final LinkIDCurrency currency, final String walletCoin, final double refundAmount, final String userId,
                                         final String applicationName, final LinkIDWalletReportType type) {

        super( walletId, creationDate, transactionId, amount, currency, walletCoin, refundAmount );

        this.userId = userId;
        this.applicationName = applicationName;
        this.type = type;
    }

    // Helper methods

    @Override
    public String toString() {

        return "LinkIDWalletReportTransaction{" +
               "userId='" + userId + '\'' +
               ", applicationName='" + applicationName + '\'' +
               ", type='" + type + '\'' +
               '}';
    }

    // Accessors

    public String getUserId() {

        return userId;
    }

    public String getApplicationName() {

        return applicationName;
    }

    public LinkIDWalletReportType getType() {

        return type;
    }
}
