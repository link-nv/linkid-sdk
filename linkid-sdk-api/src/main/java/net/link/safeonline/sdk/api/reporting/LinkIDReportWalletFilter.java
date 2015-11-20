package net.link.safeonline.sdk.api.reporting;

import java.io.Serializable;


/**
 * Created by wvdhaute
 * Date: 29/04/15
 * Time: 14:32
 */
public class LinkIDReportWalletFilter implements Serializable {

    private final String walletId;

    public LinkIDReportWalletFilter(final String walletId) {

        this.walletId = walletId;
    }

    public String getWalletId() {

        return walletId;
    }

}
