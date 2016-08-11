/*
 * safe-online - linkid-sdk-api
 *
 * Copyright 2006-2016 linkID Inc. All rights reserved.
 * linkID Inc. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.payment;

import com.google.common.base.MoreObjects;
import java.io.Serializable;
import java.util.List;
import net.link.safeonline.sdk.api.wallet.LinkIDWalletOrganizationDetails;


/**
 * Created by wvdhaute
 * Date: 29/07/16
 * Time: 15:00
 */
@SuppressWarnings("unused")
public class LinkIDPaymentInfo implements Serializable {

    private final List<LinkIDWalletOrganizationDetails> walletOrganizations;
    private final List<LinkIDPaymentMethodType>         paymentMethods;
    private final List<LinkIDCurrency>                  currencies;
    private final List<String>                          walletCoinIds;

    public LinkIDPaymentInfo(final List<LinkIDWalletOrganizationDetails> walletOrganizations, final List<LinkIDPaymentMethodType> paymentMethods,
                             final List<LinkIDCurrency> currencies, final List<String> walletCoinIds) {

        this.walletOrganizations = walletOrganizations;
        this.paymentMethods = paymentMethods;
        this.currencies = currencies;
        this.walletCoinIds = walletCoinIds;
    }

    // Helper methods

    @Override
    public String toString() {

        return MoreObjects.toStringHelper( this )
                          .add( "walletOrganizations", walletOrganizations )
                          .add( "paymentMethods", paymentMethods )
                          .add( "currencies", currencies )
                          .add( "walletCoinIds", walletCoinIds )
                          .toString();
    }

    // Accessors

    public List<LinkIDWalletOrganizationDetails> getWalletOrganizations() {

        return walletOrganizations;
    }

    public List<LinkIDPaymentMethodType> getPaymentMethods() {

        return paymentMethods;
    }

    public List<LinkIDCurrency> getCurrencies() {

        return currencies;
    }

    public List<String> getWalletCoinIds() {

        return walletCoinIds;
    }
}
