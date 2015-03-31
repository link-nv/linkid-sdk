/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.ws.data;

import java.util.HashMap;
import java.util.Map;


/**
 * Enumerate of ID-WSF DST v2.1 Second Level Status Codes.
 *
 * <p> Specification: ID-WSF DST v2.1 - 3.2.2. Second Level Status Codes </p>
 *
 * @author fcorneli
 */
public enum LinkIDSecondLevelStatusCode {

    NOT_AUTHORIZED( "ActionNotAuthorized" ),
    UNSUPPORTED_OBJECT_TYPE( "UnsupportedObjectType" ),
    DOES_NOT_EXIST( "DoesNotExist" ),
    PAGINATION_NOT_SUPPORTED( "PaginationNotSupported" ),
    MISSING_OBJECT_TYPE( "MissingObjectType" ),
    INVALID_DATA( "InvalidData" ),
    EMPTY_REQUEST( "EmptyRequest" ),
    MISSING_SELECT( "MissingSelect" ),
    MISSING_CREDENTIALS( "MissingCredentials" ),
    MISSING_NEW_DATA_ELEMENT( "MissingNewDataElement" );

    private String code;

    private static Map<String, LinkIDSecondLevelStatusCode> statusCodes = new HashMap<String, LinkIDSecondLevelStatusCode>();

    static {
        for (LinkIDSecondLevelStatusCode linkIDSecondLevelStatusCode : LinkIDSecondLevelStatusCode.values()) {
            LinkIDSecondLevelStatusCode.statusCodes.put( linkIDSecondLevelStatusCode.getCode(), linkIDSecondLevelStatusCode );
        }
    }

    private LinkIDSecondLevelStatusCode(String code) {

        this.code = code;
    }

    public String getCode() {

        return code;
    }

    public static LinkIDSecondLevelStatusCode fromCode(String code) {

        LinkIDSecondLevelStatusCode linkIDSecondLevelStatusCode = LinkIDSecondLevelStatusCode.statusCodes.get( code );
        if (null == linkIDSecondLevelStatusCode)
            throw new IllegalArgumentException( "unknown code: " + code );
        return linkIDSecondLevelStatusCode;
    }
}
