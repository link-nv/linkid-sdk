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

    private final double                  amount;
    private final LinkIDCurrency          currency;
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
    private       LinkIDPaymentAddBrowser paymentAddBrowser;
    //
    // whether or not deferred payments are allowed. An e-mail will be sent to the user to complete the payment at a later time.
    // default is not allowed
    private       boolean                 allowDeferredPay;
    //
    // mandates
    private final boolean                 mandate;      // payment context for a mandate?
    private final String                  mandateDescription;
    private final String                  mandateReference;
    //
    // optional payment menu return URLs (docdata payment menu)
    private       String                  paymentMenuResultSuccess;
    private       String                  paymentMenuResultCanceled;
    private       String                  paymentMenuResultPending;
    private       String                  paymentMenuResultError;
    //
    // wallet related flags
    private       boolean                 allowPartial;       // allow partial payments via wallets, this flag does make sense if you allow normal payment methods
    private       boolean                 onlyWallets;        // allow only wallets for this payment

    /**
     * @param amount         amount in cents
     * @param currency       currency
     * @param description    optional description
     * @param paymentProfile optional payment profile
     */
    public LinkIDPaymentContext(final double amount, final LinkIDCurrency currency, @Nullable final String description, @Nullable final String orderReference,
                                @Nullable final String paymentProfile, final int paymentValidationTime, final LinkIDPaymentAddBrowser paymentAddBrowser,
                                final boolean allowDeferredPay, final boolean mandate, @Nullable final String mandateDescription,
                                @Nullable final String mandateReference)
            throws LinkIDInvalidPaymentContextException {

        this.amount = amount;
        this.currency = currency;
        this.description = description;
        this.orderReference = orderReference;
        this.paymentProfile = paymentProfile;
        this.paymentValidationTime = paymentValidationTime;
        this.paymentAddBrowser = paymentAddBrowser;
        this.allowDeferredPay = allowDeferredPay;

        this.mandate = mandate;
        this.mandateDescription = mandateDescription;
        this.mandateReference = mandateReference;
    }

    /**
     * @param amount         amount in cents
     * @param currency       currency
     * @param description    optional description
     * @param paymentProfile optional payment profile
     */
    public LinkIDPaymentContext(final double amount, final LinkIDCurrency currency, @Nullable final String description, @Nullable final String orderReference,
                                @Nullable final String paymentProfile, final int paymentValidationTime, final LinkIDPaymentAddBrowser paymentAddBrowser,
                                final boolean allowDeferredPay)
            throws LinkIDInvalidPaymentContextException {

        this( amount, currency, description, orderReference, paymentProfile, paymentValidationTime, paymentAddBrowser, allowDeferredPay, false, null, null );
    }

    /**
     * @param amount         amount in cents
     * @param currency       currency
     * @param description    optional description
     * @param paymentProfile optional payment profile
     */
    public LinkIDPaymentContext(final double amount, final LinkIDCurrency currency, @Nullable final String description, @Nullable final String orderReference,
                                @Nullable final String paymentProfile)
            throws LinkIDInvalidPaymentContextException {

        this( amount, currency, description, orderReference, paymentProfile, 5, LinkIDPaymentAddBrowser.NOT_ALLOWED, false );
    }

    /**
     * @param amount   amount in cents
     * @param currency currency
     */
    public LinkIDPaymentContext(final double amount, final LinkIDCurrency currency)
            throws LinkIDInvalidPaymentContextException {

        this( amount, currency, null, null, null, 5, LinkIDPaymentAddBrowser.NOT_ALLOWED, false );
    }

    // Helper methods

    public Map<String, String> toMap() {

        Map<String, String> map = new HashMap<String, String>();

        map.put( AMOUNT_KEY, Double.toString( amount ) );
        map.put( CURRENCY_KEY, currency.name() );
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
        if (null == paymentAddBrowser) {
            map.put( ADD_BROWSER_KEY, LinkIDPaymentAddBrowser.NOT_ALLOWED.name() );
        } else {
            map.put( ADD_BROWSER_KEY, paymentAddBrowser.name() );
        }
        map.put( DEFERRED_PAY_KEY, Boolean.toString( allowDeferredPay ) );

        map.put( MANDATE_KEY, Boolean.toString( mandate ) );
        if (null != mandateDescription) {
            map.put( MANDATE_DESCRIPTION_KEY, mandateDescription );
        }
        if (null != mandateReference) {
            map.put( MANDATE_REFERENCE_KEY, mandateReference );
        }

        if (null != paymentMenuResultSuccess) {
            map.put( MENU_RESULT_SUCCESS_KEY, paymentMenuResultSuccess );
        }
        if (null != paymentMenuResultCanceled) {
            map.put( MENU_RESULT_CANCELED_KEY, paymentMenuResultCanceled );
        }
        if (null != paymentMenuResultPending) {
            map.put( MENU_RESULT_PENDING_KEY, paymentMenuResultPending );
        }
        if (null != paymentMenuResultSuccess) {
            map.put( MENU_RESULT_ERROR_KEY, paymentMenuResultError );
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

        if (!paymentContextMap.containsKey( CURRENCY_KEY )) {
            throw new LinkIDInvalidPaymentContextException( "Payment context's currency field is not present!" );
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

        Double amount = Double.parseDouble( paymentContextMap.get( AMOUNT_KEY ) );
        if (amount <= 0) {
            throw new LinkIDInvalidPaymentContextException( String.format( "Invalid payment context amount: %f", amount ) );
        }

        // convert
        LinkIDPaymentContext linkIDPaymentContext = new LinkIDPaymentContext( amount, LinkIDCurrency.parse( paymentContextMap.get( CURRENCY_KEY ) ),
                paymentContextMap.get( DESCRIPTION_KEY ), paymentContextMap.get( ORDER_REFERENCE_KEY ), paymentContextMap.get( PROFILE_KEY ),
                Integer.parseInt( paymentContextMap.get( VALIDATION_TIME_KEY ) ), paymentAddBrowser,
                Boolean.parseBoolean( paymentContextMap.get( DEFERRED_PAY_KEY ) ), Boolean.parseBoolean( paymentContextMap.get( MANDATE_KEY ) ),
                paymentContextMap.get( MANDATE_DESCRIPTION_KEY ), paymentContextMap.get( MANDATE_REFERENCE_KEY ) );

        linkIDPaymentContext.setPaymentMenuResultSuccess( paymentContextMap.get( MENU_RESULT_SUCCESS_KEY ) );
        linkIDPaymentContext.setPaymentMenuResultCanceled( paymentContextMap.get( MENU_RESULT_CANCELED_KEY ) );
        linkIDPaymentContext.setPaymentMenuResultPending( paymentContextMap.get( MENU_RESULT_PENDING_KEY ) );
        linkIDPaymentContext.setPaymentMenuResultError( paymentContextMap.get( MENU_RESULT_ERROR_KEY ) );

        boolean allowPartial = false;
        String allowPartialString = paymentContextMap.get( ALLOW_PARTIAL_KEY );
        if (null != allowPartialString) {
            allowPartial = Boolean.parseBoolean( allowPartialString );
        }
        linkIDPaymentContext.setAllowPartial( allowPartial );

        boolean onlyWallets = false;
        String onlyWalletsString = paymentContextMap.get( ONLY_WALLETS_KEY );
        if (null != onlyWalletsString) {
            onlyWallets = Boolean.parseBoolean( onlyWalletsString );
        }
        linkIDPaymentContext.setOnlyWallets( onlyWallets );

        return linkIDPaymentContext;
    }

    @Override
    public String toString() {

        return "PaymentContextDO{" +
               "amount=" + amount +
               ", currency=" + currency +
               ", description='" + description + '\'' +
               ", orderReference='" + orderReference + '\'' +
               ", paymentProfile='" + paymentProfile + '\'' +
               ", paymentValidationTime=" + paymentValidationTime +
               ", paymentAddBrowser=" + paymentAddBrowser +
               ", allowDeferredPay=" + allowDeferredPay +
               ", mandate=" + mandate +
               ", mandateDescription='" + mandateDescription + '\'' +
               ", mandateReference='" + mandateReference + '\'' +
               ", paymentMenuResultSuccess='" + paymentMenuResultSuccess + '\'' +
               ", paymentMenuResultCanceled='" + paymentMenuResultCanceled + '\'' +
               ", paymentMenuResultPending='" + paymentMenuResultPending + '\'' +
               ", paymentMenuResultError='" + paymentMenuResultError + '\'' +
               ", allowPartial=" + allowPartial +
               ", onlyWallets=" + onlyWallets +
               '}';
    }

    // Accessors

    public double getAmount() {

        return amount;
    }

    public LinkIDCurrency getCurrency() {

        return currency;
    }

    public String getOrderReference() {

        return orderReference;
    }

    public String getPaymentProfile() {

        return paymentProfile;
    }

    public String getDescription() {

        return description;
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

    public boolean isMandate() {

        return mandate;
    }

    public String getMandateDescription() {

        return mandateDescription;
    }

    public String getMandateReference() {

        return mandateReference;
    }

    public String getPaymentMenuResultSuccess() {

        return paymentMenuResultSuccess;
    }

    public void setPaymentMenuResultSuccess(final String paymentMenuResultSuccess) {

        this.paymentMenuResultSuccess = paymentMenuResultSuccess;
    }

    public String getPaymentMenuResultCanceled() {

        return paymentMenuResultCanceled;
    }

    public void setPaymentMenuResultCanceled(final String paymentMenuResultCanceled) {

        this.paymentMenuResultCanceled = paymentMenuResultCanceled;
    }

    public String getPaymentMenuResultPending() {

        return paymentMenuResultPending;
    }

    public void setPaymentMenuResultPending(final String paymentMenuResultPending) {

        this.paymentMenuResultPending = paymentMenuResultPending;
    }

    public String getPaymentMenuResultError() {

        return paymentMenuResultError;
    }

    public void setPaymentMenuResultError(final String paymentMenuResultError) {

        this.paymentMenuResultError = paymentMenuResultError;
    }

    public boolean isAllowPartial() {

        return allowPartial;
    }

    public void setAllowPartial(final boolean allowPartial) {

        this.allowPartial = allowPartial;
    }

    public boolean isOnlyWallets() {

        return onlyWallets;
    }

    public void setOnlyWallets(final boolean onlyWallets) {

        this.onlyWallets = onlyWallets;
    }

    public void setPaymentAddBrowser(final LinkIDPaymentAddBrowser paymentAddBrowser) {

        this.paymentAddBrowser = paymentAddBrowser;
    }

    public void setAllowDeferredPay(final boolean allowDeferredPay) {

        this.allowDeferredPay = allowDeferredPay;
    }
}
