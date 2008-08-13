/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.auth.pcsc;

public class SexConvertor implements Convertor<Sex> {

    public Sex convert(byte[] value) throws ConvertorException {

        String strValue = new String(value);
        if ("M".equals(strValue))
            return Sex.MALE;
        else if ("F".equals(strValue))
            return Sex.FEMALE;
        else if ("V".equals(strValue))
            return Sex.FEMALE;
        else if ("W".equals(strValue))
            return Sex.FEMALE;
        throw new ConvertorException("invalid sex: " + strValue);
    }
}
