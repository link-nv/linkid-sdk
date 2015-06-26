/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.payment;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import net.link.safeonline.sdk.api.exception.LinkIDInvalidPaymentContextException;
import org.jetbrains.annotations.Nullable;


@SuppressWarnings("UnusedDeclaration")
public class LinkIDPaymentContext implements Serializable {

    public static final String AMOUNT_KEY               = "PaymentContext.amount";
    public static final String CURRENCY_KEY             = "PaymentContext.currency";
    public static final String WALLET_COIN_KEY          = "PaymentContext.walletCoin";
    public static final String DESCRIPTION_KEY          = "PaymentContext.description";
    public static final String ORDER_REFERENCE_KEY      = "PaymentContext.orderReference";
    public static final String PROFILE_KEY              = "PaymentContext.profile";
    public static final String VALIDATION_TIME_KEY      = "PaymentContext.validationTime";
    public static final String ADD_LINK_KEY             = "PaymentContext.addLinkKey";
    public static final String RETURN_MENU_URL_KEY      = "PaymentContext.returnMenuURL";
    public static final String ADD_BROWSER_KEY          = "PaymentContext.addBrowser";
    public static final String DEFERRED_PAY_KEY         = "PaymentContext.deferredPay";
    public static final String MANDATE_KEY              = "PaymentContext.mandate";
    public static final String MANDATE_DESCRIPTION_KEY  = "PaymentContext.mandateDescription";
    public static final String MANDATE_REFERENCE_KEY    = "PaymentContext.mandateReference";
    //
    public static final String MENU_RESULT_SUCCESS_KEY  = "PaymentContext.menuResultSuccess";
    public static final String MENU_RESULT_CANCELED_KEY = "PaymentContext.menuResultCanceled";
    public static final String MENU_RESULT_PENDING_KEY  = "PaymentContext.menuResultPending";
    public static final String MENU_RESULT_ERROR_KEY    = "PaymentContext.menuResultError";
    //
    public static final String ALLOW_PARTIAL_KEY        = "PaymentContext.allowPartial";
    public static final String ONLY_WALLETS_KEY         = "PaymentContext.onlyWallets";

    private final LinkIDPaymentAmount     amount;
    private final String                  description;
    //
    // optional order reference, if not specified linkID will generate one in UUID format
    private final String                  orderReference;
    //
    // optional payment profile
    private final String                  paymentProfile;
    //
    // maximum time to wait for payment validation, if not specified defaults to 5s
    private final int                     paymentValidationTime;
    //
    // whether or not to allow to display the option in the client to add a payment method in the browser.
    // default is not allowed
    private final LinkIDPaymentAddBrowser paymentAddBrowser;
    //
    // whether or not deferred payments are allowed. An e-mail will be sent to the user to complete the payment at a later time.
    // default is not allowed
    private final boolean                 allowDeferredPay;
    //
    // mandates
    @Nullable
    private final LinkIDPaymentMandate    mandate;      // payment context for a mandate?
    //
    // optional payment menu return URLs (docdata payment menu)
    @Nullable
    private final LinkIDPaymentMenu       paymentMenu;
    //
    // wallet related flags
    private final boolean                 allowPartial;       // allow partial payments via wallets, this flag does make sense if you allow normal payment methods
    private final boolean                 onlyWallets;        // allow only wallets for this payment

    private LinkIDPaymentContext(final Builder builder) {

        // validate payment context
        if (null == builder.amount.getCurrency() && null == builder.amount.getWalletCoin()) {
            throw new RuntimeException( "LinkIDPaymentContext.amount needs or currecy or walletCoin specified, both are null" );
        }
        if (null != builder.amount.getCurrency() && null != builder.amount.getWalletCoin()) {
            throw new RuntimeException( "LinkIDPaymentContext.amount needs or currecy or walletCoin specified, both are specified" );
        }
        if (builder.amount.getAmount() <= 0) {
            throw new RuntimeException( "LinkIDPaymentContext.amount is <= 0, this is not allowed" );
        }

        // initialize
        this.amount = builder.amount;
        this.description = builder.description;
        this.orderReference = builder.orderReference;
        this.paymentProfile = builder.paymentProfile;
        this.paymentValidationTime = builder.paymentValidationTime;
        this.paymentAddBrowser = builder.paymentAddBrowser;
        this.allowDeferredPay = builder.allowDeferredPay;
        this.mandate = builder.mandate;
        this.paymentMenu = builder.paymentMenu;
        this.allowPartial = builder.allowPartial;
        this.onlyWallets = builder.onlyWallets;
    }

    // Helper methods

    public Map<String, String> toMap() {

        Map<String, String> map = new HashMap<String, String>();

        map.put( AMOUNT_KEY, Double.toString( amount.getAmount() ) );
        if (null != amount.getCurrency()) {
            map.put( CURRENCY_KEY, amount.getCurrency().name() );
        }
        if (null != amount.getWalletCoin()) {
            map.put( WALLET_COIN_KEY, amount.getWalletCoin() );
        }

        if (null != description) {
            map.put( DESCRIPTION_KEY, description );
        }
        if (null != orderReference) {
            map.put( ORDER_REFERENCE_KEY, orderReference );
        }
        if (null != paymentProfile) {
            map.put( PROFILE_KEY, paymentProfile );
        }
        map.put( VALIDATION_TIME_KEY, Integer.toString( paymentValidationTime ) );
        map.put( ADD_BROWSER_KEY, paymentAddBrowser.name() );
        map.put( DEFERRED_PAY_KEY, Boolean.toString( allowDeferredPay ) );

        if (null != mandate) {
            map.put( MANDATE_KEY, Boolean.toString( true ) );
            map.put( MANDATE_DESCRIPTION_KEY, mandate.getDescription() );
            if (null != mandate.getReference()) {
                map.put( MANDATE_REFERENCE_KEY, mandate.getReference() );
            }
        } else {
            map.put( MANDATE_KEY, Boolean.toString( false ) );
        }

        if (null != paymentMenu) {
            map.put( MENU_RESULT_SUCCESS_KEY, paymentMenu.getMenuResultSuccess() );
            map.put( MENU_RESULT_CANCELED_KEY, paymentMenu.getMenuResultCanceled() );
            map.put( MENU_RESULT_PENDING_KEY, paymentMenu.getMenuResultPending() );
            map.put( MENU_RESULT_ERROR_KEY, paymentMenu.getMenuResultError() );
        }

        map.put( ALLOW_PARTIAL_KEY, Boolean.toString( allowPartial ) );
        map.put( ONLY_WALLETS_KEY, Boolean.toString( onlyWallets ) );

        return map;
    }

    @Nullable
    public static LinkIDPaymentContext fromMap(final Map<String, String> paymentContextMap) {

        // check map valid
        if (!paymentContextMap.containsKey( AMOUNT_KEY )) {
            throw new LinkIDInvalidPaymentContextException( "Payment context's amount field is not present!" );
        }

        if (!paymentContextMap.containsKey( CURRENCY_KEY ) && !paymentContextMap.containsKey( WALLET_COIN_KEY )) {
            throw new LinkIDInvalidPaymentContextException( "Payment context's currency not walletCoin field is not present!" );
        }

        double amount = Double.parseDouble( paymentContextMap.get( AMOUNT_KEY ) );
        if (amount <= 0) {
            throw new LinkIDInvalidPaymentContextException( String.format( "Invalid payment context amount: %f", amount ) );
        }

        if (!paymentContextMap.containsKey( VALIDATION_TIME_KEY )) {
            throw new LinkIDInvalidPaymentContextException( "Payment context's validation time field is not present!" );
        }

        LinkIDPaymentAddBrowser paymentAddBrowser = LinkIDPaymentAddBrowser.parse( paymentContextMap.get( ADD_BROWSER_KEY ) );

        // TODO: backward support for old SDK, remove one day...
        String addLinkString = paymentContextMap.get( ADD_LINK_KEY );
        if (null != addLinkString) {
            boolean addLink = Boolean.parseBoolean( addLinkString );
            if (addLink) {
                paymentAddBrowser = LinkIDPaymentAddBrowser.REDIRECT;
            }
        }
        String returnMenuString = paymentContextMap.get( RETURN_MENU_URL_KEY );
        if (null != returnMenuString) {
            boolean returnMenu = Boolean.parseBoolean( returnMenuString );
            if (returnMenu) {
                paymentAddBrowser = LinkIDPaymentAddBrowser.REDIRECT;
            }
        }

        // intialize
        Builder builder = new Builder(
                new LinkIDPaymentAmount( amount, LinkIDCurrency.parse( paymentContextMap.get( CURRENCY_KEY ) ), paymentContextMap.get( WALLET_COIN_KEY ) ) );
        builder.description( paymentContextMap.get( DESCRIPTION_KEY ) );
        builder.orderReference( paymentContextMap.get( ORDER_REFERENCE_KEY ) );
        builder.paymentProfile( paymentContextMap.get( PROFILE_KEY ) );
        builder.paymentValidationTime( Integer.parseInt( paymentContextMap.get( VALIDATION_TIME_KEY ) ) );
        builder.paymentAddBrowser( paymentAddBrowser );
        builder.allowDeferredPay( getBoolean( paymentContextMap, DEFERRED_PAY_KEY ) );
        builder.allowPartial( getBoolean( paymentContextMap, ALLOW_PARTIAL_KEY ) );
        builder.onlyWallets( getBoolean( paymentContextMap, ONLY_WALLETS_KEY ) );

        // payment menu
        if (Boolean.parseBoolean( paymentContextMap.get( MANDATE_KEY ) )) {
            builder.mandate( new LinkIDPaymentMandate( paymentContextMap.get( MANDATE_DESCRIPTION_KEY ), paymentContextMap.get( MANDATE_REFERENCE_KEY ) ) );
        }

        // payment menu
        if (paymentContextMap.containsKey( MENU_RESULT_SUCCESS_KEY )) {
            builder.paymentMenu( new LinkIDPaymentMenu( paymentContextMap.get( MENU_RESULT_SUCCESS_KEY ), paymentContextMap.get( MENU_RESULT_CANCELED_KEY ),
                    paymentContextMap.get( MENU_RESULT_PENDING_KEY ), paymentContextMap.get( MENU_RESULT_ERROR_KEY ) ) );
        }

        return builder.build();
    }

    private static boolean getBoolean(final Map<String, String> map, final String key) {

        if (!map.containsKey( key ))
            return false;

        return Boolean.parseBoolean( map.get( key ) );
    }

    @Override
    public String toString() {

        return "LinkIDPaymentContext{" +
               "amount=" + amount +
               ", description='" + description + '\'' +
               ", orderReference='" + orderReference + '\'' +
               ", paymentProfile='" + paymentProfile + '\'' +
               ", paymentValidationTime=" + paymentValidationTime +
               ", paymentAddBrowser=" + paymentAddBrowser +
               ", allowDeferredPay=" + allowDeferredPay +
               ", mandate=" + mandate +
               ", paymentMenu=" + paymentMenu +
               ", allowPartial=" + allowPartial +
               ", onlyWallets=" + onlyWallets +
               '}';
    }

    // Builder


    public static class Builder {

        // Required parameters
        private final LinkIDPaymentAmount amount;

        // Optional parameters - initialized to default values
        private String                  description           = null;
        private String                  orderReference        = null;
        private String                  paymentProfile        = null;
        private int                     paymentValidationTime = 5;
        private LinkIDPaymentAddBrowser paymentAddBrowser     = LinkIDPaymentAddBrowser.NOT_ALLOWED;
        private boolean                 allowDeferredPay      = false;
        private LinkIDPaymentMandate    mandate               = null;
        private LinkIDPaymentMenu       paymentMenu           = null;
        private boolean                 allowPartial          = false;
        private boolean                 onlyWallets           = false;

        public LinkIDPaymentContext build() {

            return new LinkIDPaymentContext( this );
        }

        public Builder(final LinkIDPaymentAmount amount) {

            this.amount = amount;
        }

        public Builder description(final String description) {

            this.description = description;
            return this;
        }

        public Builder orderReference(final String orderReference) {

            this.orderReference = orderReference;
            return this;
        }

        public Builder paymentProfile(final String paymentProfile) {

            this.paymentProfile = paymentProfile;
            return this;
        }

        public Builder paymentValidationTime(final int paymentValidationTime) {

            this.paymentValidationTime = paymentValidationTime;
            return this;
        }

        public Builder paymentAddBrowser(final LinkIDPaymentAddBrowser paymentAddBrowser) {

            this.paymentAddBrowser = paymentAddBrowser;
            return this;
        }

        public Builder allowDeferredPay(final boolean allowDeferredPay) {

            this.allowDeferredPay = allowDeferredPay;
            return this;
        }

        public Builder mandate(final LinkIDPaymentMandate mandate) {

            this.mandate = mandate;
            return this;
        }

        public Builder paymentMenu(final LinkIDPaymentMenu paymentMenu) {

            this.paymentMenu = paymentMenu;
            return this;
        }

        public Builder allowPartial(final boolean allowPartial) {

            this.allowPartial = allowPartial;
            return this;
        }

        public Builder onlyWallets(final boolean onlyWallets) {

            this.onlyWallets = onlyWallets;
            return this;
        }
    }

    // Accessors

    public LinkIDPaymentAmount getAmount() {

        return amount;
    }

    public String getDescription() {

        return description;
    }

    public String getOrderReference() {

        return orderReference;
    }

    public String getPaymentProfile() {

        return paymentProfile;
    }

    public int getPaymentValidationTime() {

        return paymentValidationTime;
    }

    public LinkIDPaymentAddBrowser getPaymentAddBrowser() {

        return paymentAddBrowser;
    }

    public boolean isAllowDeferredPay() {

        return allowDeferredPay;
    }

    @Nullable
    public LinkIDPaymentMandate getMandate() {

        return mandate;
    }

    @Nullable
    public LinkIDPaymentMenu getPaymentMenu() {

        return paymentMenu;
    }

    public boolean isAllowPartial() {

        return allowPartial;
    }

    public boolean isOnlyWallets() {

        return onlyWallets;
    }
}
