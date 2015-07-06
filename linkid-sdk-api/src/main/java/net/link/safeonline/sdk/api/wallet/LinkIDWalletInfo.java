package net.link.safeonline.sdk.api.wallet;

import java.io.Serializable;


/**
 * Created by wvdhaute
 * Date: 11/05/15
 * Time: 14:10
 */
public class LinkIDWalletInfo implements Serializable {

    private final String walletId;

    public LinkIDWalletInfo(final String walletId) {

        this.walletId = walletId;
    }

    // Helper methods

    @Override
    public String toString() {

        return "LinkIDWalletInfo{" +
               "walletId='" + walletId + '\'' +
               '}';
    }

    // Accessors

    public String getWalletId() {

        return walletId;
    }

}
