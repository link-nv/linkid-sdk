/*
 * safe-online - linkid-sdk-api
 *
 * Copyright 2006-2016 linkID Inc. All rights reserved.
 * linkID Inc. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.attribute.profile;

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
