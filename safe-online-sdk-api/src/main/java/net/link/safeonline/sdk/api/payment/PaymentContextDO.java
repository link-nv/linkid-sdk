package net.link.safeonline.sdk.api.payment;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import net.link.safeonline.sdk.api.exception.InvalidPaymentContextException;
import org.jetbrains.annotations.Nullable;


public class PaymentContextDO implements Serializable {

    public static final String AMOUNT_KEY   = "PaymentContext.amount";
    public static final String CURRENCY_KEY = "PaymentContext.currency";

    private final double   amount;
    private final Currency currency;

    /**
     * @param amount   amount in cents
     * @param currency currency
     */
    public PaymentContextDO(final double amount, final Currency currency) {

        this.amount = amount;
        this.currency = currency;
    }

    // Helper methods

    public Map<String, String> toMap() {

        Map<String, String> map = new HashMap<String, String>();

        map.put( AMOUNT_KEY, Double.toString( amount ) );
        map.put( CURRENCY_KEY, currency.name() );

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

        // convert
        return new PaymentContextDO( Double.parseDouble( paymentContextMap.get( AMOUNT_KEY ) ), Currency.parse( paymentContextMap.get( CURRENCY_KEY ) ) );
    }

    // Accessors

    public double getAmount() {

        return amount;
    }

    public Currency getCurrency() {

        return currency;
    }
}
