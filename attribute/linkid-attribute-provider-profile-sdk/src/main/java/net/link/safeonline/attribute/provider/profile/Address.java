/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.attribute.provider.profile;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import net.link.safeonline.sdk.api.attribute.AttributeSDK;
import net.link.safeonline.sdk.api.attribute.Compound;


@SuppressWarnings("UnusedDeclaration")
public class Address implements Serializable {

    private final String  street;
    private final String  streetNumber;
    private final String  streetBus;
    private final String  postalCode;
    private final String  city;
    private final Country country;

    public Address(final String street, final String streetNumber, final String streetBus, final String postalCode, final String city, final Country country) {

        this.street = street;
        this.streetNumber = streetNumber;
        this.streetBus = streetBus;
        this.postalCode = postalCode;
        this.city = city;
        this.country = country;
    }

    // Helper methods

    public static List<Address> getAddress(final List<AttributeSDK<Serializable>> addressAttributes) {

        if (null == addressAttributes) {
            return new LinkedList<Address>();
        }

        List<Address> addresses = new LinkedList<Address>();
        for (AttributeSDK<Serializable> addressAttribute : addressAttributes) {

            Compound addressCompound = (Compound) addressAttribute.getValue();

            // optional bus
            AttributeSDK<Serializable> busAttribute = addressCompound.findMember( ProfileConstants.ADDRESS_STREET_BUS );
            String bus = null != busAttribute? (String) busAttribute.getValue(): null;

            Address address = new Address( (String) addressCompound.getMember( ProfileConstants.ADDRESS_STREET ).getValue(),
                    (String) addressCompound.getMember( ProfileConstants.ADDRESS_STREET_NUMBER ).getValue(), bus,
                    (String) addressCompound.getMember( ProfileConstants.ADDRESS_POSTAL_CODE ).getValue(),
                    (String) addressCompound.getMember( ProfileConstants.ADDRESS_CITY ).getValue(),
                    Country.toCountryAlpha2( (String) addressCompound.getMember( ProfileConstants.ADDRESS_COUNTRY ).getValue() ) );
            addresses.add( address );
        }

        return addresses;
    }

    /**
     * @return street, streetnumber, (streetbus)
     */
    public String getAddress() {

        if (null == streetBus) {
            return String.format( "%s, %s", street, streetNumber );
        } else {
            return String.format( "%s, %s %s", street, streetNumber, streetBus );
        }
    }

    // Accessors

    public String getStreet() {

        return street;
    }

    public String getStreetNumber() {

        return streetNumber;
    }

    public String getStreetBus() {

        return streetBus;
    }

    public String getPostalCode() {

        return postalCode;
    }

    public String getCity() {

        return city;
    }

    public Country getCountry() {

        return country;
    }
}
