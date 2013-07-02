package net.link.safeonline.sdk.api.payment;

public enum Currency {

    EUR;

    public static Currency parse(final String currencyString) {

        for (Currency currency : Currency.values()) {

            if (currency.name().equals( currencyString.toUpperCase() )) {
                return currency;
            }
        }

        throw new RuntimeException( String.format( "Unsupported currency %s!", currencyString ) );
    }
}
