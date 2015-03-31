/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.attribute.provider.profile;

public enum LinkIDGender {

    MALE( "M" ),
    FEMALE( "F" );

    private final String stringValue;

    private LinkIDGender(final String stringValue) {

        this.stringValue = stringValue;
    }

    public String getStringValue() {

        return stringValue;
    }

    public static LinkIDGender toGender(final String stringValue) {

        if (null == stringValue)
            return null;

        for (LinkIDGender linkIDGender : LinkIDGender.values()) {
            if (stringValue.toLowerCase().startsWith( linkIDGender.getStringValue().toLowerCase() ))
                return linkIDGender;
        }

        throw new RuntimeException( String.format( "Invalid gender \"%s\"!", stringValue ) );
    }
}
