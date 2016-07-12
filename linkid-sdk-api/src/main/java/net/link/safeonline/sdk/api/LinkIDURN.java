/*
 * safe-online - linkid-sdk-api
 *
 * Copyright 2006-2016 linkID Inc. All rights reserved.
 * linkID Inc. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api;

/**
 * Created by wvdhaute
 * Date: 12/07/16
 * Time: 10:13
 */
public abstract class LinkIDURN {

    private static final String URN_PREFIX                 = "urn:be:linkid";
    private static final String THEME_PART                 = "theme";
    private static final String VOUCHER_PART               = "voucher";
    private static final String PAYMENT_CONFIGURATION_PART = "payment:configuration";
    private static final String WALLET_ORGANIZATION_PART   = "wallet:organization";

    private LinkIDURN() {

        throw new AssertionError();
    }

    /**
     * Get the application linkID URN from the application name
     *
     * @param applicationName the application name
     *
     * @return the application URN
     */
    public static String getApplicationURN(final String applicationName) {

        return String.format( "%s:%s", URN_PREFIX, applicationName );
    }

    /**
     * Get the theme URN
     *
     * @param applicationName the application name
     * @param themeName       the theme name
     *
     * @return the theme URN
     */
    public static String getThemeURN(final String applicationName, final String themeName) {

        return String.format( "%s:%s:%s", getApplicationURN( applicationName ), THEME_PART, themeName );
    }

    /**
     * Get the voucher organization URN
     *
     * @param applicationName         the application name
     * @param voucherOrganizationName the voucher organization name
     *
     * @return the voucher organization URN
     */
    public static String getVoucherOrganizationURN(final String applicationName, final String voucherOrganizationName) {

        return String.format( "%s:%s:%s", getApplicationURN( applicationName ), VOUCHER_PART, voucherOrganizationName );
    }

    /**
     * Get the payment configuration URN
     *
     * @param applicationName          the application name
     * @param paymentConfigurationName the payment configuration name
     *
     * @return the payment configuration URN
     */
    public static String getPaymentConfigurationURN(final String applicationName, final String paymentConfigurationName) {

        return String.format( "%s:%s:%s", getApplicationURN( applicationName ), PAYMENT_CONFIGURATION_PART, paymentConfigurationName );
    }

    /**
     * Get the wallet organization URN
     *
     * @param applicationName        the application name
     * @param walletOrganizationName the wallet organization name
     *
     * @return the wallet organization URN
     */
    public static String getWalletOrganizationURN(final String applicationName, final String walletOrganizationName) {

        return String.format( "%s:%s:%s", getApplicationURN( applicationName ), WALLET_ORGANIZATION_PART, walletOrganizationName );
    }

}
