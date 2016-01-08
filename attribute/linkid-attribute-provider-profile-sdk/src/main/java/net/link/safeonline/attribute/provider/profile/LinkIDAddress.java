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
import net.link.safeonline.sdk.api.attribute.LinkIDAttribute;
import net.link.safeonline.sdk.api.attribute.LinkIDCompound;


@SuppressWarnings("UnusedDeclaration")
public class LinkIDAddress implements Serializable {

    private final String        street;
    private final String        streetNumber;
    private final String        streetBus;
    private final String        postalCode;
    private final String        city;
    private final LinkIDCountry linkIDCountry;

    public LinkIDAddress(final String street, final String streetNumber, final String streetBus, final String postalCode, final String city,
                         final LinkIDCountry linkIDCountry) {

        this.street = street;
        this.streetNumber = streetNumber;
        this.streetBus = streetBus;
        this.postalCode = postalCode;
        this.city = city;
        this.linkIDCountry = linkIDCountry;
    }

    // Helper methods

    public static List<LinkIDAddress> getAddress(final List<? extends LinkIDAttribute<Serializable>> addressAttributes) {

        if (null == addressAttributes) {
            return new LinkedList<LinkIDAddress>();
        }

        List<LinkIDAddress> linkIDAddresses = new LinkedList<LinkIDAddress>();
        for (LinkIDAttribute<Serializable> addressAttribute : addressAttributes) {

            LinkIDCompound addressLinkIDCompound = (LinkIDCompound) addressAttribute.getValue();

            // optional bus
            LinkIDAttribute<Serializable> busAttribute = addressLinkIDCompound.findMember( LinkIDProfileConstants.ADDRESS_STREET_BUS );
            String bus = null != busAttribute? (String) busAttribute.getValue(): null;

            LinkIDAddress linkIDAddress = new LinkIDAddress( (String) addressLinkIDCompound.getMember( LinkIDProfileConstants.ADDRESS_STREET ).getValue(),
                    (String) addressLinkIDCompound.getMember( LinkIDProfileConstants.ADDRESS_STREET_NUMBER ).getValue(), bus,
                    (String) addressLinkIDCompound.getMember( LinkIDProfileConstants.ADDRESS_POSTAL_CODE ).getValue(),
                    (String) addressLinkIDCompound.getMember( LinkIDProfileConstants.ADDRESS_CITY ).getValue(),
                    LinkIDCountry.toCountryAlpha2( (String) addressLinkIDCompound.getMember( LinkIDProfileConstants.ADDRESS_COUNTRY ).getValue() ) );
            linkIDAddresses.add( linkIDAddress );
        }

        return linkIDAddresses;
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

    public LinkIDCountry getLinkIDCountry() {

        return linkIDCountry;
    }
}
