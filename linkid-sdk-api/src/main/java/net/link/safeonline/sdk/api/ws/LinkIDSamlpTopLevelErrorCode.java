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
 * SAMLp version 2.0 Top Level Error Code.
 *
 * @author fcorneli
 */
public enum LinkIDSamlpTopLevelErrorCode {

    SUCCESS( "urn:oasis:names:tc:SAML:2.0:status:Success" ),
    REQUESTER( "urn:oasis:names:tc:SAML:2.0:status:Requester" ),
    RESPONDER( "urn:oasis:names:tc:SAML:2.0:status:Responder" ),
    VERSION_MISMATCH( "urn:oasis:names:tc:SAML:2.0:status:VersionMismatch" );

    private final String errorCode;

    private final static Map<String, LinkIDSamlpTopLevelErrorCode> errorCodeMap = new HashMap<String, LinkIDSamlpTopLevelErrorCode>();

    static {
        LinkIDSamlpTopLevelErrorCode[] errorCodes = LinkIDSamlpTopLevelErrorCode.values();
        for (LinkIDSamlpTopLevelErrorCode errorCode : errorCodes)
            errorCodeMap.put( errorCode.getErrorCode(), errorCode );
    }

    private LinkIDSamlpTopLevelErrorCode(String errorCode) {

        this.errorCode = errorCode;
    }

    public String getErrorCode() {

        return errorCode;
    }

    @Override
    public String toString() {

        return errorCode;
    }

    public static LinkIDSamlpTopLevelErrorCode getSamlpTopLevelErrorCode(String errorCode) {

        LinkIDSamlpTopLevelErrorCode linkIDSamlpTopLevelErrorCode = errorCodeMap.get( errorCode );
        if (null == linkIDSamlpTopLevelErrorCode)
            throw new IllegalArgumentException( "unknown SAMLp top-level error code: " + errorCode );
        return linkIDSamlpTopLevelErrorCode;
    }
}
