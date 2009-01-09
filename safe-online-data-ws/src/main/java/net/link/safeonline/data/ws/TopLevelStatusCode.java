/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.data.ws;

import java.util.HashMap;
import java.util.Map;

public enum TopLevelStatusCode {
    OK("OK"), FAILED("Failed");

    private String                                 code;

    private static Map<String, TopLevelStatusCode> statusCodes = new HashMap<String, TopLevelStatusCode>();

    static {
        for (TopLevelStatusCode topLevelStatusCode : TopLevelStatusCode
                .values()) {
            TopLevelStatusCode.statusCodes.put(topLevelStatusCode.getCode(),
                    topLevelStatusCode);
        }
    }


    private TopLevelStatusCode(String code) {

        this.code = code;
    }


    public String getCode() {

        return code;
    }


    public static TopLevelStatusCode fromCode(String code) {

        TopLevelStatusCode topLevelStatusCode = TopLevelStatusCode.statusCodes
                .get(code);
        if (null == topLevelStatusCode)
            throw new IllegalArgumentException("unknown code: " + code);
        return topLevelStatusCode;
    }
}
