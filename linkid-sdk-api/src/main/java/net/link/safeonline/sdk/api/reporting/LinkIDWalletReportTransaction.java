package net.link.safeonline.sdk.api.reporting;

import java.util.Date;
import net.link.safeonline.sdk.api.payment.LinkIDCurrency;
import net.link.safeonline.sdk.api.payment.LinkIDWalletTransaction;
import org.jetbrains.annotations.Nullable;


/**
 * Created by wvdhaute
 * Date: 30/03/15
 * Time: 16:44
 */
@SuppressWarnings("UnusedDeclaration")
public class LinkIDWalletReportTransaction extends LinkIDWalletTransaction {

    @Nullable
    private final String                 userId;
    private final String                 applicationName;
    private final String                 applicationFriendly;
    private final LinkIDWalletReportType type;

    public LinkIDWalletReportTransaction(final String walletId, final Date creationDate, final String transactionId, final double amount,
                                         final LinkIDCurrency currency, final String walletCoin, final double refundAmount,
                                         @Nullable final String paymentDescription, @Nullable final String userId, final String applicationName,
                                         final String applicationFriendly, final LinkIDWalletReportType type) {

        super( walletId, creationDate, transactionId, amount, currency, walletCoin, refundAmount, paymentDescription );

        this.userId = userId;
        this.applicationName = applicationName;
        this.applicationFriendly = applicationFriendly;
        this.type = type;
    }

    // Helper methods

    @Override
    public String toString() {

        return "LinkIDWalletReportTransaction{" +
               "userId='" + userId + '\'' +
               ", applicationName='" + applicationName + '\'' +
               ", applicationFriendly='" + applicationFriendly + '\'' +
               ", type='" + type + '\'' +
               ", super='" + super.toString() + '\'' +
               '}';
    }

    // Accessors

    @Nullable
    public String getUserId() {

        return userId;
    }

    public String getApplicationName() {

        return applicationName;
    }

    public String getApplicationFriendly() {

        return applicationFriendly;
    }

    public LinkIDWalletReportType getType() {

        return type;
    }
}
