package net.link.safeonline.attribute.provider.profile.attributes;

import com.lyndir.lhunath.opal.system.logging.exception.InternalInconsistencyException;


public enum Country {

    BELGIUM( "be", "Belgium" ),
    NETHERLANDS( "nl", "Netherlands" ),
    OTHER( "xx", "Other" );

    private final String key;

    private final String value;

    Country(final String key, final String value) {

        this.key = key;
        this.value = value;
    }

    public String getKey() {

        return key;
    }

    public String getValue() {

        return value;
    }

    public static Country toCountry(String countryString) {

        for (Country country : Country.values()) {
            if (country.getValue().equals( countryString )) {
                return country;
            }
        }

        throw new InternalInconsistencyException( String.format( "Invalid country: %s", countryString ) );
    }
}
