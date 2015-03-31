/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.ws;

import java.util.HashMap;
import java.util.Map;


/**
 * SAMLp version 2.0 Second-Level Error Code.
 *
 * <p>
 * Specification: 3.2.2.2 Element StatusCode - Assertions and Protocols for the OASIS Security Assertion Markup Language (SAML) V2.0 - OASIS
 * Standard, 15 March 2005
 * </p>
 *
 * @author fcorneli
 */
public enum LinkIDSamlpSecondLevelErrorCode {

    INVALID_ATTRIBUTE_NAME_OR_VALUE( "urn:oasis:names:tc:SAML:2.0:status:InvalidAttrNameOrValue" ),
    ATTRIBUTE_UNAVAILABLE( "urn:net:lin-k:safe-online:SAML:2.0:status:AttributeUnavailable" ),
    UNKNOWN_PRINCIPAL( "urn:oasis:names:tc:SAML:2.0:status:UnknownPrincipal" ),
    REQUEST_DENIED( "urn:oasis:names:tc:SAML:2.0:status:RequestDenied" ),
    INVALID_NAMEID_POLICY( "urn:oasis:names:tc:SAML:2.0:status:InvalidNameIDPolicy" );

    private final String errorCode;

    private final static Map<String, LinkIDSamlpSecondLevelErrorCode> errorCodeMap = new HashMap<String, LinkIDSamlpSecondLevelErrorCode>();

    static {
        LinkIDSamlpSecondLevelErrorCode[] errorCodes = LinkIDSamlpSecondLevelErrorCode.values();
        for (LinkIDSamlpSecondLevelErrorCode errorCode : errorCodes)
            errorCodeMap.put( errorCode.getErrorCode(), errorCode );
    }

    private LinkIDSamlpSecondLevelErrorCode(String errorCode) {

        this.errorCode = errorCode;
    }

    public String getErrorCode() {

        return errorCode;
    }

    public static LinkIDSamlpSecondLevelErrorCode getSamlpTopLevelErrorCode(String errorCode) {

        LinkIDSamlpSecondLevelErrorCode linkIDSamlpSecondLevelErrorCode = errorCodeMap.get( errorCode );
        if (null == linkIDSamlpSecondLevelErrorCode)
            throw new IllegalArgumentException( "unknown SAMLp second-level error code: " + errorCode );
        return linkIDSamlpSecondLevelErrorCode;
    }
}
