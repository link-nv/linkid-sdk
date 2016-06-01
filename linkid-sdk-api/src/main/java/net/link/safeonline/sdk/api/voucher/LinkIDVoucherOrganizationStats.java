/*
 * safe-online - linkid-sdk-api
 *
 * Copyright 2006-2016 linkID Inc. All rights reserved.
 * linkID Inc. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.voucher;

import java.io.Serializable;


/**
 * Created by wvdhaute
 * Date: 01/06/16
 * Time: 10:24
 */
public class LinkIDVoucherOrganizationStats implements Serializable {

    private final long numberOfVouchers;
    private final long numberOfInactiveVouchers;
    private final long numberOfActiveVouchers;
    private final long numberOfRedeemedVouchers;

    public LinkIDVoucherOrganizationStats(final long numberOfVouchers, final long numberOfInactiveVouchers, final long numberOfActiveVouchers,
                                          final long numberOfRedeemedVouchers) {

        this.numberOfVouchers = numberOfVouchers;
        this.numberOfInactiveVouchers = numberOfInactiveVouchers;
        this.numberOfActiveVouchers = numberOfActiveVouchers;
        this.numberOfRedeemedVouchers = numberOfRedeemedVouchers;
    }

    // Helper methods

    @Override
    public String toString() {

        return "LinkIDVoucherOrganizationStats{" +
               "numberOfVouchers=" + numberOfVouchers +
               ", numberOfInactiveVouchers=" + numberOfInactiveVouchers +
               ", numberOfActiveVouchers=" + numberOfActiveVouchers +
               ", numberOfRedeemedVouchers=" + numberOfRedeemedVouchers +
               '}';
    }

    // Accessors

    /**
     * @return Total number of vouchers, not yet active, active and redeemed ones
     */
    public long getNumberOfVouchers() {

        return numberOfVouchers;
    }

    /**
     * @return Total number of inactive vouchers, i.e. voucher limit not yet reached
     */
    public long getNumberOfInactiveVouchers() {

        return numberOfInactiveVouchers;
    }

    /**
     * @return Total number of active vouchers, i.e. not yet redeemed
     */
    public long getNumberOfActiveVouchers() {

        return numberOfActiveVouchers;
    }

    /**
     * @return Total number of redeemed vouchers
     */
    public long getNumberOfRedeemedVouchers() {

        return numberOfRedeemedVouchers;
    }
}
