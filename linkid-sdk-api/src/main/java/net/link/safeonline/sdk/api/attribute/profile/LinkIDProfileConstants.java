/*
 * safe-online - linkid-sdk-api
 *
 * Copyright 2006-2016 linkID Inc. All rights reserved.
 * linkID Inc. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.attribute.profile;

public class LinkIDProfileConstants {

    private LinkIDProfileConstants() {

    }

    // attribute names
    public static final String GIVEN_NAME    = "profile.givenName";
    public static final String FAMILY_NAME   = "profile.familyName";
    public static final String DATE_OF_BIRTH = "profile.dob";
    public static final String GENDER        = "profile.gender";
    public static final String LANGUAGE      = "profile.language";
    public static final String MOBILE        = "profile.mobile";
    public static final String PHONE         = "profile.phone";

    // attribute group
    public static final String GROUP = "profile";

    // address
    public static final String ADDRESS               = "profile.address";
    public static final String ADDRESS_STREET        = "profile.address.street";
    public static final String ADDRESS_STREET_NUMBER = "profile.address.streetNumber";
    public static final String ADDRESS_STREET_BUS    = "profile.address.streetBus";
    public static final String ADDRESS_POSTAL_CODE   = "profile.address.postalCode";
    public static final String ADDRESS_CITY          = "profile.address.city";
    public static final String ADDRESS_COUNTRY       = "profile.address.country";

    // email
    public static final String EMAIL           = "profile.email";
    public static final String EMAIL_ADDRESS   = "profile.email.address";
    public static final String EMAIL_CONFIRMED = "profile.email.confirmed";
}
