/*
 * safe-online - linkid-sdk-api
 *
 * Copyright 2006-2016 linkID Inc. All rights reserved.
 * linkID Inc. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.wallet;

import com.google.common.base.MoreObjects;
import java.io.Serializable;
import java.util.List;
import net.link.safeonline.sdk.api.common.LinkIDRequestStatusCode;
import net.link.safeonline.sdk.api.localization.LinkIDLocalizationValue;
import net.link.safeonline.sdk.api.payment.LinkIDCurrency;
import org.jetbrains.annotations.Nullable;


/**
 * Created by wvdhaute
 * Date: 29/04/16
 * Time: 14:56
 */
public class LinkIDWalletOrganization implements Serializable {

    private final String                        id;
    private final String                        logoUrl;
    private final long                          expirationInSecs;
    private final boolean                       sticky;
    private final boolean                       autoEnroll;
    private final boolean                       removeWalletOnUnsubscribe;
    private final List<LinkIDLocalizationValue> nameLocalizations;
    private final List<LinkIDLocalizationValue> descriptionLocalizations;
    //
    @Nullable
    private final LinkIDCurrency                currency;
    @Nullable
    private final String                        coinId;
    @Nullable
    private final List<LinkIDLocalizationValue> coinNameLocalization;
    @Nullable
    private final List<LinkIDLocalizationValue> coinNameMultipleLocalization;
    //
    @Nullable
    private final LinkIDWalletPolicyBalance     policyBalance;
    //
    private final LinkIDRequestStatusCode       statusCode;

    public LinkIDWalletOrganization(final String id, final String logoUrl, final long expirationInSecs, final boolean sticky, final boolean autoEnroll,
                                    final boolean removeWalletOnUnsubscribe, final List<LinkIDLocalizationValue> nameLocalizations,
                                    final List<LinkIDLocalizationValue> descriptionLocalizations,
                                    @Nullable final List<LinkIDLocalizationValue> coinNameLocalization,
                                    @Nullable final List<LinkIDLocalizationValue> coinNameMultipleLocalization, @Nullable final LinkIDCurrency currency,
                                    @Nullable final String coinId, @Nullable final LinkIDWalletPolicyBalance policyBalance) {

        this( id, logoUrl, expirationInSecs, sticky, autoEnroll, removeWalletOnUnsubscribe, nameLocalizations, descriptionLocalizations, coinNameLocalization,
                coinNameMultipleLocalization, currency, coinId, policyBalance, LinkIDRequestStatusCode.PENDING );

    }

    public LinkIDWalletOrganization(final String id, final String logoUrl, final long expirationInSecs, final boolean sticky, final boolean autoEnroll,
                                    final boolean removeWalletOnUnsubscribe, final List<LinkIDLocalizationValue> nameLocalizations,
                                    final List<LinkIDLocalizationValue> descriptionLocalizations,
                                    @Nullable final List<LinkIDLocalizationValue> coinNameLocalization,
                                    @Nullable final List<LinkIDLocalizationValue> coinNameMultipleLocalization, @Nullable final LinkIDCurrency currency,
                                    @Nullable final String coinId, @Nullable final LinkIDWalletPolicyBalance policyBalance,
                                    final LinkIDRequestStatusCode statusCode) {

        this.id = id;
        this.logoUrl = logoUrl;
        this.expirationInSecs = expirationInSecs;
        this.sticky = sticky;
        this.autoEnroll = autoEnroll;
        this.removeWalletOnUnsubscribe = removeWalletOnUnsubscribe;
        this.nameLocalizations = nameLocalizations;
        this.descriptionLocalizations = descriptionLocalizations;
        this.coinNameLocalization = coinNameLocalization;
        this.coinNameMultipleLocalization = coinNameMultipleLocalization;
        this.currency = currency;
        this.coinId = coinId;
        this.policyBalance = policyBalance;
        this.statusCode = statusCode;
    }

    // Helper methods

    @Override
    public String toString() {

        return MoreObjects.toStringHelper( this )
                          .add( "id", id )
                          .add( "logoUrl", logoUrl )
                          .add( "expirationInSecs", expirationInSecs )
                          .add( "sticky", sticky )
                          .add( "autoEnroll", autoEnroll )
                          .add( "removeWalletOnUnsubscribe", removeWalletOnUnsubscribe )
                          .add( "nameLocalizations", nameLocalizations )
                          .add( "descriptionLocalizations", descriptionLocalizations )
                          .add( "currency", currency )
                          .add( "coinId", coinId )
                          .add( "coinNameLocalization", coinNameLocalization )
                          .add( "coinNameMultipleLocalization", coinNameMultipleLocalization )
                          .add( "policyBalance", policyBalance )
                          .add( "statusCode", statusCode )
                          .toString();
    }

    // Helper methods

    public String getId() {

        return id;
    }

    public String getLogoUrl() {

        return logoUrl;
    }

    public long getExpirationInSecs() {

        return expirationInSecs;
    }

    public boolean isSticky() {

        return sticky;
    }

    public boolean isAutoEnroll() {

        return autoEnroll;
    }

    public boolean isRemoveWalletOnUnsubscribe() {

        return removeWalletOnUnsubscribe;
    }

    public List<LinkIDLocalizationValue> getNameLocalizations() {

        return nameLocalizations;
    }

    public List<LinkIDLocalizationValue> getDescriptionLocalizations() {

        return descriptionLocalizations;
    }

    @Nullable
    public List<LinkIDLocalizationValue> getCoinNameLocalization() {

        return coinNameLocalization;
    }

    @Nullable
    public List<LinkIDLocalizationValue> getCoinNameMultipleLocalization() {

        return coinNameMultipleLocalization;
    }

    @Nullable
    public LinkIDCurrency getCurrency() {

        return currency;
    }

    @Nullable
    public String getCoinId() {

        return coinId;
    }

    @Nullable
    public LinkIDWalletPolicyBalance getPolicyBalance() {

        return policyBalance;
    }

    public LinkIDRequestStatusCode getStatusCode() {

        return statusCode;
    }

}
