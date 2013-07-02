package net.link.safeonline.sdk.api.payment;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import net.link.safeonline.sdk.api.exception.InvalidPaymentContextException;
import org.jetbrains.annotations.Nullable;


public class PaymentContextDO implements Serializable {

    public static final String AMOUNT_KEY          = "PaymentContext.amount";
    public static final String CURRENCY_KEY        = "PaymentContext.currency";
    public static final String DESCRIPTION_KEY     = "PaymentContext.description";
    public static final String PROFILE_KEY         = "PaymentContext.profile";
    public static final String VALIDATION_TIME_KEY = "PaymentContext.validationTime";

    private final double   amount;
    private final Currency currency;
    private final String   description;

    // optional payment profile
    private final String paymentProfile;

    // maximum time to wait for payment validation, if not specified defaults to 5s
    private final int paymentValidationTime;

    /**
     * @param amount         amount in cents
     * @param currency       currency
     * @param description    optional description
     * @param paymentProfile optional payment profile
     */
    public PaymentContextDO(final double amount, final Currency currency, @Nullable final String description, @Nullable final String paymentProfile,
                            final int paymentValidationTime) {

        this.amount = amount;
        this.currency = currency;
        this.description = description;
        this.paymentProfile = paymentProfile;
        this.paymentValidationTime = paymentValidationTime;
    }

    /**
     * @param amount         amount in cents
     * @param currency       currency
     * @param description    optional description
     * @param paymentProfile optional payment profile
     */
    public PaymentContextDO(final double amount, final Currency currency, @Nullable final String description, @Nullable final String paymentProfile) {

        this( amount, currency, description, paymentProfile, 5 );
    }

    /**
     * @param amount   amount in cents
     * @param currency currency
     */
    public PaymentContextDO(final double amount, final Currency currency) {

        this( amount, currency, null, null, 5 );
    }

    // Helper methods

    public Map<String, String> toMap() {

        Map<String, String> map = new HashMap<String, String>();

        map.put( AMOUNT_KEY, Double.toString( amount ) );
        map.put( CURRENCY_KEY, currency.name() );
        if (null != description)
            map.put( DESCRIPTION_KEY, description );
        if (null != paymentProfile)
            map.put( PROFILE_KEY, paymentProfile );
        map.put( VALIDATION_TIME_KEY, Integer.toString( paymentValidationTime ) );

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
        return new PaymentContextDO( Double.parseDouble( paymentContextMap.get( AMOUNT_KEY ) ), Currency.parse( paymentContextMap.get( CURRENCY_KEY ) ),
                paymentContextMap.get( DESCRIPTION_KEY ), paymentContextMap.get( PROFILE_KEY ),
                Integer.parseInt( paymentContextMap.get( VALIDATION_TIME_KEY ) ) );
    }

    // Accessors

    public double getAmount() {

        return amount;
    }

    public Currency getCurrency() {

        return currency;
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
}
