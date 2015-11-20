package net.link.safeonline.sdk.api.reporting;

import java.io.Serializable;
import java.util.Date;
import org.jetbrains.annotations.Nullable;


/**
 * Created by wvdhaute
 * Date: 20/11/15
 * Time: 10:46
 */
public class LinkIDWalletInfoReport implements Serializable {

    private final String walletId;
    private final Date   created;
    private final Date   removed;
    @Nullable
    private final String userId;
    private final String organizationId;
    private final double balance;

    public LinkIDWalletInfoReport(final String walletId, final Date created, final Date removed, final String userId, final String organizationId,
                                  final double balance) {

        this.walletId = walletId;
        this.created = created;
        this.removed = removed;
        this.userId = userId;
        this.organizationId = organizationId;
        this.balance = balance;
    }

    // Helper methods

    @Override
    public String toString() {

        return "LinkIDWalletInfoReport{" +
               "walletId='" + walletId + '\'' +
               ", created=" + created +
               ", removed=" + removed +
               ", userId='" + userId + '\'' +
               ", organizationId='" + organizationId + '\'' +
               ", balance=" + balance +
               '}';
    }

    // Accessors

    public String getWalletId() {

        return walletId;
    }

    public Date getCreated() {

        return created;
    }

    public Date getRemoved() {

        return removed;
    }

    @Nullable
    public String getUserId() {

        return userId;
    }

    public String getOrganizationId() {

        return organizationId;
    }

    public double getBalance() {

        return balance;
    }
}
