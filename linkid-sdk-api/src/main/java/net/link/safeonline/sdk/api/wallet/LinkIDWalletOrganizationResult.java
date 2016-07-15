/*
 * safe-online - linkid-sdk-api
 *
 * Copyright 2006-2016 linkID Inc. All rights reserved.
 * linkID Inc. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.wallet;

import javax.annotation.Nullable;


/**
 * Created by wvdhaute
 * Date: 15/07/16
 * Time: 10:41
 */
@SuppressWarnings("unused")
public class LinkIDWalletOrganizationResult {

    private final String name;      // the official wallet organization technical name
    @Nullable
    private final String coinName;  // the official wallet organization coin technical name

    public LinkIDWalletOrganizationResult(final String name, @org.jetbrains.annotations.Nullable final String coinName) {

        this.name = name;
        this.coinName = coinName;
    }

    // Helper methods

    @Override
    public String toString() {

        return "LinkIDWalletOrganizationResult{" + "name='" + name + '\'' + ", coinName='" + coinName + '\'' + '}';
    }

    // Accessors

    public String getName() {

        return name;
    }

    @Nullable
    public String getCoinName() {

        return coinName;
    }
}
