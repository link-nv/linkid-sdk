/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.attribute.provider;

/**
 * <h2>{@link DataType}</h2>
 *
 * <p> linkID Attribute data type. </p>
 *
 * <p> <i>Jan 14, 2008</i> </p>
 *
 * @author wvdhaute
 */
public enum DataType {

    STRING( "string" ),
    BOOLEAN( "boolean" ),
    INTEGER( "integer" ),
    DOUBLE( "double" ),
    DATE( "date" ),
    COMPOUNDED( "compound" );

    private final String friendlyName;

    DataType(String value) {

        friendlyName = value;
    }

    public String getValue() {

        return friendlyName;
    }

    @Override
    public String toString() {

        return friendlyName;
    }

    public static DataType getDataType(String dataTypeValue) {

        for (DataType dataType : DataType.values())
            if (dataType.friendlyName.equals( dataTypeValue ))
                return dataType;

        throw new IllegalArgumentException( "unknown dataType: " + dataTypeValue );
    }
}
