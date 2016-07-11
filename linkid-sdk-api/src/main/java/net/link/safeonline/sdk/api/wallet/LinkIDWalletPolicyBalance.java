/*
 * safe-online - linkid-sdk-api
 *
 * Copyright 2006-2016 linkID Inc. All rights reserved.
 * linkID Inc. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.wallet;

import java.io.Serializable;
import net.link.safeonline.sdk.api.payment.LinkIDCurrency;
import org.jetbrains.annotations.Nullable;


/**
 * Created by wvdhaute
 * Date: 06/07/16
 * Time: 09:37
 */
public class LinkIDWalletPolicyBalance implements Serializable {

    private final double         balance;
    @Nullable
    private final LinkIDCurrency currency;
    @Nullable
    private final String         coinId;

    public LinkIDWalletPolicyBalance(final double balance, @Nullable final LinkIDCurrency currency, @Nullable final String coinId) {

        this.balance = balance;
        this.currency = currency;
        this.coinId = coinId;
    }

    // Helper methods

    @Override
    public String toString() {

        return "LinkIDWalletPolicyBalance{" +
               "balance=" + balance +
               ", currency=" + currency +
               ", coinId='" + coinId + '\'' +
               '}';
    }

    // Accessors

    public double getBalance() {

        return balance;
    }

    @Nullable
    public LinkIDCurrency getCurrency() {

        return currency;
    }

    @Nullable
    public String getCoinId() {

        return coinId;
    }
}
