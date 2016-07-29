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
 * Date: 06/07/16
 * Time: 09:37
 */
public class LinkIDWalletPolicyBalance implements Serializable {

    private final double balance;

    public LinkIDWalletPolicyBalance(final double balance) {

        this.balance = balance;
    }

    // Helper methods

    @Override
    public String toString() {

        return "LinkIDWalletPolicyBalance{" + "balance=" + balance + '}';
    }

    // Accessors

    public double getBalance() {

        return balance;
    }

}
