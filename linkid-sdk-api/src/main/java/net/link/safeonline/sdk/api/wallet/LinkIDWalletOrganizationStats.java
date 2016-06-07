/*
 * safe-online - linkid-sdk-api
 *
 * Copyright 2006-2016 linkID Inc. All rights reserved.
 * linkID Inc. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.wallet;

import java.io.Serializable;


/**
 * Created by wvdhaute
 * Date: 01/06/16
 * Time: 10:24
 */
public class LinkIDWalletOrganizationStats implements Serializable {

    private final long numberOfWallets;

    public LinkIDWalletOrganizationStats(final long numberOfWallets) {

        this.numberOfWallets = numberOfWallets;
    }

    // Helper methods

    @Override
    public String toString() {

        return "LinkIDVoucherOrganizationStats{" +
               "numberOfWallets=" + numberOfWallets +
               '}';
    }

    // Accessors

    public long getNumberOfWallets() {

        return numberOfWallets;
    }
}
