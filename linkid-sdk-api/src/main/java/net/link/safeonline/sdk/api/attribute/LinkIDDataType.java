/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.attribute;

/**
 * <h2>{@link LinkIDDataType}</h2>
 * <p/>
 * <p> linkID Attribute data type. </p>
 * <p/>
 * <p> <i>Jan 14, 2008</i> </p>
 *
 * @author wvdhaute
 */
public enum LinkIDDataType {

    // NOTE: COMPOUNDED is still in here for backwards compatibility

    STRING( "string" ), BOOLEAN( "boolean" ), INTEGER( "integer" ), DOUBLE( "double" ), DATE( "date" );

    private final String friendlyName;

    LinkIDDataType(String value) {

        friendlyName = value;
    }

    public String getValue() {

        return friendlyName;
    }

    @Override
    public String toString() {

        return friendlyName;
    }

}
