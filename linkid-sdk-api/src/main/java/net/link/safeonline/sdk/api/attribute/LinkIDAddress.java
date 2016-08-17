/*
 * safe-online - linkid-sdk-api
 *
 * Copyright 2006-2016 linkID Inc. All rights reserved.
 * linkID Inc. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.attribute;

import com.google.common.base.MoreObjects;
import java.io.Serializable;


/**
 * Created by wvdhaute
 * Date: 12/08/16
 * Time: 15:29
 */
public class LinkIDAddress implements Serializable {

    private String street;
    private String streetNumber;
    private String streetBus;
    private String postalCode;
    private String city;
    private String countryCode;

    public LinkIDAddress() {

    }

    public LinkIDAddress(final String street, final String streetNumber, final String streetBus, final String postalCode, final String city,
                         final String countryCode) {

        this.street = street;
        this.streetNumber = streetNumber;
        this.streetBus = streetBus;
        this.postalCode = postalCode;
        this.city = city;
        this.countryCode = countryCode;
    }

    // Helper methods

    @Override
    public String toString() {

        return MoreObjects.toStringHelper( this )
                          .add( "street", street )
                          .add( "streetNumber", streetNumber )
                          .add( "streetBus", streetBus )
                          .add( "postalCode", postalCode )
                          .add( "city", city )
                          .add( "countryCode", countryCode )
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

    public String getCountryCode() {

        return countryCode;
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

    public void setCountryCode(final String countryCode) {

        this.countryCode = countryCode;
    }
}
