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
import net.link.safeonline.sdk.api.exception.InvalidPaymentContextException;
import org.jetbrains.annotations.Nullable;


@SuppressWarnings("UnusedDeclaration")
public class PaymentContextDO implements Serializable {

    public static final String AMOUNT_KEY              = "PaymentContext.amount";
    public static final String CURRENCY_KEY            = "PaymentContext.currency";
    public static final String DESCRIPTION_KEY         = "PaymentContext.description";
    public static final String ORDER_REFERENCE_KEY     = "PaymentContext.orderReference";
    public static final String PROFILE_KEY             = "PaymentContext.profile";
    public static final String VALIDATION_TIME_KEY     = "PaymentContext.validationTime";
    public static final String ADD_LINK_KEY            = "PaymentContext.addLinkKey";
    public static final String RETURN_MENU_URL_KEY     = "PaymentContext.returnMenuURL";
    public static final String DEFERRED_PAY_KEY        = "PaymentContext.deferredPay";
    public static final String MANDATE_KEY             = "PaymentContext.mandate";
    public static final String MANDATE_DESCRIPTION_KEY = "PaymentContext.mandateDescription";
    public static final String MANDATE_REFERENCE_KEY   = "PaymentContext.mandateReference";

    public static final String MENU_RESULT_SUCCESS_KEY  = "PaymentContext.menuResultSuccess";
    public static final String MENU_RESULT_CANCELED_KEY = "PaymentContext.menuResultCanceled";
    public static final String MENU_RESULT_PENDING_KEY  = "PaymentContext.menuResultPending";
    public static final String MENU_RESULT_ERROR_KEY    = "PaymentContext.menuResultError";

    private final double   amount;
    private final Currency currency;
    private final String   description;

    // optional order reference, if not specified linkID will generate one in UUID format
    private final String orderReference;

    // optional payment profile
    private final String paymentProfile;

    // maximum time to wait for payment validation, if not specified defaults to 5s
    private final int paymentValidationTime;

    // whether or not to display a link to linkID's add payment method page if the linkID user does not have any payment methods added, default is true.
    private final boolean showAddPaymentMethodLink;

    // if so and linkID user selects add payment method, the payment menu URL to redirect to will be returned in the payment response
    // this is an alternative to "showAddPaymentMethodLink" where the payment menu is loaded via the iframe/popup. popup blockers...
    private final boolean returnPaymentMenuURL;

    // whether or not deferred payments are allowed, if a user has no payment token attached to the linkID account
    // linkID can allow for the user to make a deferred payment which he can complete later on from his browser.
    private final boolean allowDeferredPay;

    // mandates
    private final boolean mandate;      // payment context for a mandate?
    private final String  mandateDescription;
    private final String  mandateReference;

    // optional payment menu return URLs (returnPaymentMenuURL)
    private String paymentMenuResultSuccess;
    private String paymentMenuResultCanceled;
    private String paymentMenuResultPending;
    private String paymentMenuResultError;

    /**
     * @param amount                   amount in cents
     * @param currency                 currency
     * @param description              optional description
     * @param paymentProfile           optional payment profile
     * @param showAddPaymentMethodLink optional show add payment method link
     * @param allowDeferredPay         optional allow deferred payments flag
     */
    public PaymentContextDO(final double amount, final Currency currency, @Nullable final String description, @Nullable final String orderReference,
                            @Nullable final String paymentProfile, final int paymentValidationTime, final boolean showAddPaymentMethodLink,
                            final boolean returnPaymentMenuURL, final boolean allowDeferredPay, final boolean mandate,
                            @Nullable final String mandateDescription, @Nullable final String mandateReference) {

        this.amount = amount;
        this.currency = currency;
        this.description = description;
        this.orderReference = orderReference;
        this.paymentProfile = paymentProfile;
        this.paymentValidationTime = paymentValidationTime;
        this.showAddPaymentMethodLink = showAddPaymentMethodLink;
        this.returnPaymentMenuURL = returnPaymentMenuURL;
        this.allowDeferredPay = allowDeferredPay;

        this.mandate = mandate;
        this.mandateDescription = mandateDescription;
        this.mandateReference = mandateReference;
    }

    /**
     * @param amount                   amount in cents
     * @param currency                 currency
     * @param description              optional description
     * @param paymentProfile           optional payment profile
     * @param showAddPaymentMethodLink optional show add payment method link
     * @param allowDeferredPay         optional allow deferred payments flag
     */
    public PaymentContextDO(final double amount, final Currency currency, @Nullable final String description, @Nullable final String orderReference,
                            @Nullable final String paymentProfile, final int paymentValidationTime, final boolean showAddPaymentMethodLink,
                            final boolean allowDeferredPay) {

        this( amount, currency, description, orderReference, paymentProfile, paymentValidationTime, showAddPaymentMethodLink, false, allowDeferredPay, false,
                null, null );
    }

    /**
     * @param amount         amount in cents
     * @param currency       currency
     * @param description    optional description
     * @param paymentProfile optional payment profile
     */
    public PaymentContextDO(final double amount, final Currency currency, @Nullable final String description, @Nullable final String orderReference,
                            @Nullable final String paymentProfile) {

        this( amount, currency, description, orderReference, paymentProfile, 5, true, false );
    }

    /**
     * @param amount   amount in cents
     * @param currency currency
     */
    public PaymentContextDO(final double amount, final Currency currency) {

        this( amount, currency, null, null, null, 5, true, false );
    }

    // Helper methods

    public Map<String, String> toMap() {

        Map<String, String> map = new HashMap<String, String>();

        map.put( AMOUNT_KEY, Double.toString( amount ) );
        map.put( CURRENCY_KEY, currency.name() );
        if (null != description)
            map.put( DESCRIPTION_KEY, description );
        if (null != orderReference)
            map.put( ORDER_REFERENCE_KEY, orderReference );
        if (null != paymentProfile)
            map.put( PROFILE_KEY, paymentProfile );
        map.put( VALIDATION_TIME_KEY, Integer.toString( paymentValidationTime ) );
        map.put( ADD_LINK_KEY, Boolean.toString( showAddPaymentMethodLink ) );
        map.put( RETURN_MENU_URL_KEY, Boolean.toString( returnPaymentMenuURL ) );
        map.put( DEFERRED_PAY_KEY, Boolean.toString( allowDeferredPay ) );

        map.put( MANDATE_KEY, Boolean.toString( mandate ) );
        if (null != mandateDescription)
            map.put( MANDATE_DESCRIPTION_KEY, mandateDescription );
        if (null != mandateReference)
            map.put( MANDATE_REFERENCE_KEY, mandateReference );

        if (null != paymentMenuResultSuccess)
            map.put( MENU_RESULT_SUCCESS_KEY, paymentMenuResultSuccess );
        if (null != paymentMenuResultCanceled)
            map.put( MENU_RESULT_CANCELED_KEY, paymentMenuResultCanceled );
        if (null != paymentMenuResultPending)
            map.put( MENU_RESULT_PENDING_KEY, paymentMenuResultPending );
        if (null != paymentMenuResultSuccess)
            map.put( MENU_RESULT_ERROR_KEY, paymentMenuResultError );

        return map;
    }

    @Nullable
    public static PaymentContextDO fromMap(final Map<String, String> paymentContextMap)
            throws InvalidPaymentContextException {

        // check map valid
        if (!paymentContextMap.containsKey( AMOUNT_KEY ))
            throw new InvalidPaymentContextException( "Payment context's amount field is not present!" );

        if (!paymentContextMap.containsKey( CURRENCY_KEY ))
            throw new InvalidPaymentContextException( "Payment context's currency field is not present!" );

        if (!paymentContextMap.containsKey( VALIDATION_TIME_KEY ))
            throw new InvalidPaymentContextException( "Payment context's validation time field is not present!" );

        // convert
        PaymentContextDO paymentContextDO = new PaymentContextDO( Double.parseDouble( paymentContextMap.get( AMOUNT_KEY ) ),
                Currency.parse( paymentContextMap.get( CURRENCY_KEY ) ), paymentContextMap.get( DESCRIPTION_KEY ), paymentContextMap.get( ORDER_REFERENCE_KEY ),
                paymentContextMap.get( PROFILE_KEY ), Integer.parseInt( paymentContextMap.get( VALIDATION_TIME_KEY ) ),
                Boolean.parseBoolean( paymentContextMap.get( ADD_LINK_KEY ) ), Boolean.parseBoolean( paymentContextMap.get( RETURN_MENU_URL_KEY ) ),
                Boolean.parseBoolean( paymentContextMap.get( DEFERRED_PAY_KEY ) ), Boolean.parseBoolean( paymentContextMap.get( MANDATE_KEY ) ),
                paymentContextMap.get( MANDATE_DESCRIPTION_KEY ), paymentContextMap.get( MANDATE_REFERENCE_KEY ) );

        paymentContextDO.setPaymentMenuResultSuccess( paymentContextMap.get( MENU_RESULT_SUCCESS_KEY ) );
        paymentContextDO.setPaymentMenuResultCanceled( paymentContextMap.get( MENU_RESULT_CANCELED_KEY ) );
        paymentContextDO.setPaymentMenuResultPending( paymentContextMap.get( MENU_RESULT_PENDING_KEY ) );
        paymentContextDO.setPaymentMenuResultError( paymentContextMap.get( MENU_RESULT_ERROR_KEY ) );

        return paymentContextDO;
    }

    @Override
    public String toString() {

        return String.format( "Amount: %f, Currency: %s, Description: \"%s\", OrderReference: \"%s\", Profile: \"%s\", validationTime: %d, "
                              + "addPaymentMethodLink: %s, returnPaymentMenuURL: %s allowDeferredPay: %s, "
                              + "mandate: %s, mandateDescription: %s, mandateReference: %s "
                              + "menuResultSuccess: %s, menuResultCanceled: %s, menuResultPending: %s, menuResultError: %s", amount, currency, description,
                orderReference, paymentProfile, paymentValidationTime, showAddPaymentMethodLink, returnPaymentMenuURL, allowDeferredPay, mandate,
                mandateDescription, mandateReference, paymentMenuResultSuccess, paymentMenuResultCanceled, paymentMenuResultPending, paymentMenuResultError );
    }

    // Accessors

    public double getAmount() {

        return amount;
    }

    public Currency getCurrency() {

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

    public boolean isShowAddPaymentMethodLink() {

        return showAddPaymentMethodLink;
    }

    public boolean isReturnPaymentMenuURL() {

        return returnPaymentMenuURL;
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
}
