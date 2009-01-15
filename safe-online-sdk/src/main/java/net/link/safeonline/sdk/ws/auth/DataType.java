/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.sdk.ws.auth;

import java.util.HashMap;
import java.util.Map;

/**
 * <h2>{@link DataType}<br>
 * <sub>[in short] (TODO).</sub></h2>
 * 
 * <p>
 * OLAS Attribute data type.
 * </p>
 * 
 * <p>
 * <i>Jan 14, 2008</i>
 * </p>
 * 
 * @author wvdhaute
 */
public enum DataType {

    STRING("string"), BOOLEAN("boolean"), INTEGER("integer"), DOUBLE("double"), DATE(
            "date"), COMPOUNDED("compound");

    private final String                       value;

    private final static Map<String, DataType> dataTypeMap = new HashMap<String, DataType>();

    static {
        DataType[] dataTypes = DataType.values();
        for (DataType dataType : dataTypes) {
            dataTypeMap.put(dataType.getValue(), dataType);
        }
    }


    private DataType(String value) {

        this.value = value;
    }


    public String getValue() {

        return this.value;
    }


    @Override
    public String toString() {

        return this.value;
    }


    public static DataType getDataType(String dataTypeValue) {

        DataType dataType = dataTypeMap.get(dataTypeValue);
        if (null == dataType)
            throw new IllegalArgumentException("unknown dataType: "
                    + dataTypeValue);
        return dataType;
    }

}
