/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.ws.data;

import java.util.HashMap;
import java.util.Map;


public enum LinkIDTopLevelStatusCode {

    OK( "OK" ),
    FAILED( "Failed" );

    private String code;

    private static Map<String, LinkIDTopLevelStatusCode> statusCodes = new HashMap<String, LinkIDTopLevelStatusCode>();

    static {
        for (LinkIDTopLevelStatusCode linkIDTopLevelStatusCode : LinkIDTopLevelStatusCode.values())
            LinkIDTopLevelStatusCode.statusCodes.put( linkIDTopLevelStatusCode.getCode(), linkIDTopLevelStatusCode );
    }

    private LinkIDTopLevelStatusCode(String code) {

        this.code = code;
    }

    public String getCode() {

        return code;
    }

    public static LinkIDTopLevelStatusCode fromCode(String code) {

        LinkIDTopLevelStatusCode linkIDTopLevelStatusCode = LinkIDTopLevelStatusCode.statusCodes.get( code );
        if (null == linkIDTopLevelStatusCode)
            throw new IllegalArgumentException( "unknown code: " + code );
        return linkIDTopLevelStatusCode;
    }
}
