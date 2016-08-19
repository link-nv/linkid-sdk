/*
 * safe-online - linkid-sdk-api
 *
 * Copyright 2006-2016 linkID Inc. All rights reserved.
 * linkID Inc. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.attribute.profile;

import java.io.Serializable;
import java.util.Collection;
import net.link.safeonline.sdk.api.attribute.LinkIDAttribute;
import org.jetbrains.annotations.Nullable;


/**
 * Created by wvdhaute
 * Date: 18/08/16
 * Time: 10:45
 */
public class LinkIDAddress implements Serializable {

    private String        street;
    private String        streetNumber;
    private String        streetBus;
    private String        postalCode;
    private String        city;
    private LinkIDCountry linkIDCountry;

    // Helper methods

    @Nullable
    public static LinkIDAddress findAddress(final Collection<? extends LinkIDAttribute<Serializable>> attributes) {

        if (null == attributes) {
            return null;
        }

        LinkIDAddress linkIDAddress = null;
        for (LinkIDAttribute<Serializable> attribute : attributes) {

            if (attribute.getName().equals( LinkIDProfileConstants.ADDRESS_STREET )) {
                if (null == linkIDAddress) {
                    linkIDAddress = new LinkIDAddress();
                }
                linkIDAddress.setStreet( (String) attribute.getValue() );
            }
            if (attribute.getName().equals( LinkIDProfileConstants.ADDRESS_STREET_NUMBER )) {
                if (null == linkIDAddress) {
                    linkIDAddress = new LinkIDAddress();
                }
                linkIDAddress.setStreetNumber( (String) attribute.getValue() );
            }
            if (attribute.getName().equals( LinkIDProfileConstants.ADDRESS_STREET_BUS )) {
                if (null == linkIDAddress) {
                    linkIDAddress = new LinkIDAddress();
                }
                linkIDAddress.setStreetBus( (String) attribute.getValue() );
            }
            if (attribute.getName().equals( LinkIDProfileConstants.ADDRESS_POSTAL_CODE )) {
                if (null == linkIDAddress) {
                    linkIDAddress = new LinkIDAddress();
                }
                linkIDAddress.setPostalCode( (String) attribute.getValue() );
            }
            if (attribute.getName().equals( LinkIDProfileConstants.ADDRESS_CITY )) {
                if (null == linkIDAddress) {
                    linkIDAddress = new LinkIDAddress();
                }
                linkIDAddress.setCity( (String) attribute.getValue() );
            }

            if (attribute.getName().equals( LinkIDProfileConstants.ADDRESS_COUNTRY )) {
                if (null == linkIDAddress) {
                    linkIDAddress = new LinkIDAddress();
                }
                LinkIDCountry country = LinkIDCountry.toCountryAlpha2( (String) attribute.getValue() );
                linkIDAddress.setLinkIDCountry( country );
            }
        }

        return linkIDAddress;
    }

    public String getStreetNumberAndBus() {

        if (null != streetBus) {
            return String.format( "%s %s", streetNumber, streetBus );
        }

        return streetNumber;
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

    public LinkIDCountry getLinkIDCountry() {

        return linkIDCountry;
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

    public void setLinkIDCountry(final LinkIDCountry linkIDCountry) {

        this.linkIDCountry = linkIDCountry;
    }
}
