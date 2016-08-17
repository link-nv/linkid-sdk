/*
 * safe-online - linkid-sdk-api
 *
 * Copyright 2006-2016 linkID Inc. All rights reserved.
 * linkID Inc. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.attribute;

import com.google.common.base.MoreObjects;
import java.io.Serializable;
import net.link.safeonline.sdk.api.attribute.profile.LinkIDCountry;


/**
 * Created by wvdhaute
 * Date: 12/08/16
 * Time: 15:29
 */
public class LinkIDAddress implements Serializable {

    private String        street;
    private String        streetNumber;
    private String        streetBus;
    private String        postalCode;
    private String        city;
    private LinkIDCountry country;

    public LinkIDAddress() {

    }

    public LinkIDAddress(final String street, final String streetNumber, final String streetBus, final String postalCode, final String city,
                         final LinkIDCountry country) {

        this.street = street;
        this.streetNumber = streetNumber;
        this.streetBus = streetBus;
        this.postalCode = postalCode;
        this.city = city;
        this.country = country;
    }

    // Helper methods

    public String getStreetNumberAndBus() {

        if (null != streetBus) {
            return String.format( "%s %s", streetNumber, streetBus );
        }

        return streetNumber;
    }

    @Override
    public String toString() {

        return MoreObjects.toStringHelper( this )
                          .add( "street", street )
                          .add( "streetNumber", streetNumber )
                          .add( "streetBus", streetBus )
                          .add( "postalCode", postalCode )
                          .add( "city", city )
                          .add( "country", country )
                          .toString();
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

    public LinkIDCountry getCountry() {

        return country;
    }

    public void setStreet(final String street) {

        this.street = street;
    }

    public void setStreetNumber(final String streetNumber) {

        this.streetNumber = streetNumber;
    }

    public void setStreetBus(final String streetBus) {

        this.streetBus = streetBus;
    }

    public void setPostalCode(final String postalCode) {

        this.postalCode = postalCode;
    }

    public void setCity(final String city) {

        this.city = city;
    }

    public void setCountryCode(final LinkIDCountry country) {

        this.country = country;
    }
}
