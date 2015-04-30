package net.link.safeonline.sdk.api.reporting;

import java.io.Serializable;


/**
 * Created by wvdhaute
 * Date: 29/04/15
 * Time: 14:32
 */
public class LinkIDReportWalletFilter implements Serializable {

    private final String walletId;
    private final String userId;

    public LinkIDReportWalletFilter(final String walletId, final String userId) {

        this.walletId = walletId;
        this.userId = userId;
    }

    public String getWalletId() {

        return walletId;
    }

    public String getUserId() {

        return userId;
    }
}
