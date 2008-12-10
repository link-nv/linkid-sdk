/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.custom.converter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * <h2>{@link PhoneNumberConverter}<br>
 * <sub>[in short] (TODO).</sub></h2>
 * 
 * <p>
 * [description / usage].
 * </p>
 * 
 * <p>
 * <i>Dec 2, 2008</i>
 * </p>
 * 
 * @author wvdhaute
 */
public class PhoneNumberConverter {

    /**
     * Converts the phone number by removing spaces, ".", "/", "-"
     * 
     * Validates the phoneNumber for being numeric or starting with a "+" if area code is specified.
     */
    public static String convertNumber(String phoneNumber) {

        if (null == phoneNumber)
            return null;

        String convertedNumber;

        // strip illegal characters: space . / -
        convertedNumber = phoneNumber.replace(" ", "");
        convertedNumber = convertedNumber.replace(".", "");
        convertedNumber = convertedNumber.replace("/", "");
        convertedNumber = convertedNumber.replace("-", "");

        Pattern pattern = Pattern.compile("[+]?[0-9]*");
        Matcher m = pattern.matcher(convertedNumber);
        if (m.matches())
            return convertedNumber;

        return null;
    }

}
