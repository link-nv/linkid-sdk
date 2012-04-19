package net.link.safeonline.attribute.provider.profile;

public interface ProfileConstants {

    // attribute names
    String GIVEN_NAME    = "profile.givenName";
    String FAMILY_NAME   = "profile.familyName";
    String DATE_OF_BIRTH = "profile.dob";
    String GENDER        = "profile.gender";
    String LANGUAGE      = "profile.language";
    String MOBILE        = "profile.mobile";
    String PHONE         = "profile.phone";
    String NRN           = "profile.nrn";               // national registry number

    // address
    String ADDRESS               = "profile.address";
    String ADDRESS_STREET        = "profile.address.street";
    String ADDRESS_STREET_NUMBER = "profile.address.streetNumber";
    String ADDRESS_STREET_BUS    = "profile.address.streetBus";
    String ADDRESS_POSTAL_CODE   = "profile.address.postalCode";
    String ADDRESS_CITY          = "profile.address.city";
    String ADDRESS_COUNTRY       = "profile.address.country";

    // email
    String EMAIL           = "profile.email";
    String EMAIL_ADDRESS   = "profile.email.address";
    String EMAIL_CONFIRMED = "profile.email.confirmed";
}
