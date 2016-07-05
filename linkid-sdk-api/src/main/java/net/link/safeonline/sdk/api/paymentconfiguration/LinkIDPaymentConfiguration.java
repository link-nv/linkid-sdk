/*
 * safe-online - linkid-sdk-api
 *
 * Copyright 2006-2016 linkID Inc. All rights reserved.
 * linkID Inc. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.paymentconfiguration;

import java.io.Serializable;
import java.util.List;
import net.link.safeonline.sdk.api.payment.LinkIDPaymentMethodType;


/**
 * Created by wvdhaute
 * Date: 23/06/16
 * Time: 14:58
 */
@SuppressWarnings("unused")
public class LinkIDPaymentConfiguration implements Serializable {

    private final String                        name;
    //
    private final boolean                       defaultConfiguration;
    //
    private final boolean                       onlyWallets;
    private final boolean                       noWallets;
    //
    private final List<String>                  walletOrganizations;
    private final List<LinkIDPaymentMethodType> paymentMethods;

    public LinkIDPaymentConfiguration(final String name, final boolean defaultConfiguration, final boolean onlyWallets, final boolean noWallets,
                                      final List<String> walletOrganizations, final List<LinkIDPaymentMethodType> paymentMethods) {

        this.name = name;
        this.defaultConfiguration = defaultConfiguration;
        this.onlyWallets = onlyWallets;
        this.noWallets = noWallets;
        this.walletOrganizations = walletOrganizations;
        this.paymentMethods = paymentMethods;
    }

    // Helper methods

    @Override
    public String toString() {

        return "LinkIDPaymentConfiguration{" +
               "name='" + name + '\'' +
               ", defaultConfiguration=" + defaultConfiguration +
               ", onlyWallets=" + onlyWallets +
               ", noWallets=" + noWallets +
               ", walletOrganizations=" + walletOrganizations +
               ", paymentMethods=" + paymentMethods +
               '}';
    }

    // Accessors

    public String getName() {

        return name;
    }

    public boolean isDefaultConfiguration() {

        return defaultConfiguration;
    }

    public boolean isOnlyWallets() {

        return onlyWallets;
    }

    public boolean isNoWallets() {

        return noWallets;
    }

    public List<String> getWalletOrganizations() {

        return walletOrganizations;
    }

    public List<LinkIDPaymentMethodType> getPaymentMethods() {

        return paymentMethods;
    }
}
