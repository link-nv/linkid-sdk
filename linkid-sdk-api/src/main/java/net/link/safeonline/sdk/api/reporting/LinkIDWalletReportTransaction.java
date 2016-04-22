package net.link.safeonline.sdk.api.reporting;

import java.util.Date;
import net.link.safeonline.sdk.api.payment.LinkIDCurrency;
import net.link.safeonline.sdk.api.payment.LinkIDWalletTransaction;
import net.link.safeonline.sdk.api.ws.linkid.wallet.LinkIDWalletReportInfo;
import org.jetbrains.annotations.Nullable;


/**
 * Created by wvdhaute
 * Date: 30/03/15
 * Time: 16:44
 */
@SuppressWarnings("UnusedDeclaration")
public class LinkIDWalletReportTransaction extends LinkIDWalletTransaction {

    private final String                 id;
    @Nullable
    private final String                 userId;
    private final String                 applicationName;
    private final String                 applicationFriendly;
    private final LinkIDWalletReportType type;
    @Nullable
    private final LinkIDWalletReportInfo reportInfo;

    public LinkIDWalletReportTransaction(final String id, final String walletId, final String walletOrganizationId, final Date creationDate,
                                         @Nullable final Date refundedDate, @Nullable final Date committedDate, final String transactionId, final double amount,
                                         final LinkIDCurrency currency, final String walletCoin, final double refundAmount,
                                         @Nullable final String paymentDescription, @Nullable final String userId, final String applicationName,
                                         final String applicationFriendly, final LinkIDWalletReportType type,
                                         @Nullable final LinkIDWalletReportInfo reportInfo) {

        super( walletId, walletOrganizationId, creationDate, refundedDate, committedDate, transactionId, amount, currency, walletCoin, refundAmount,
                paymentDescription );

        this.id = id;
        this.userId = userId;
        this.applicationName = applicationName;
        this.applicationFriendly = applicationFriendly;
        this.type = type;
        this.reportInfo = reportInfo;
    }

    // Helper methods

    @Override
    public String toString() {

        return "LinkIDWalletReportTransaction{" +
               "id='" + id + '\'' +
               ", userId='" + userId + '\'' +
               ", applicationName='" + applicationName + '\'' +
               ", applicationFriendly='" + applicationFriendly + '\'' +
               ", type='" + type + '\'' +
               ", reportInfo='" + reportInfo + '\'' +
               ", super='" + super.toString() + '\'' +
               '}';
    }

    // Accessors

    public String getId() {

        return id;
    }

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

    @Nullable
    public LinkIDWalletReportInfo getReportInfo() {

        return reportInfo;
    }
}
