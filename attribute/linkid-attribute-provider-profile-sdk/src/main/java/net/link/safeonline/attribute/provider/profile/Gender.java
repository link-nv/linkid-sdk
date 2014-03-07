/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.attribute.provider.profile;

public enum Gender {

    MALE( "M" ),
    FEMALE( "F" );

    private final String stringValue;

    private Gender(final String stringValue) {

        this.stringValue = stringValue;
    }

    public String getStringValue() {

        return stringValue;
    }

    public static Gender toGender(final String stringValue) {

        if (null == stringValue)
            return null;

        for (Gender gender : Gender.values()) {
            if (stringValue.toLowerCase().startsWith( gender.getStringValue().toLowerCase() ))
                return gender;
        }

        throw new RuntimeException( String.format( "Invalid gender \"%s\"!", stringValue ) );
    }
}
