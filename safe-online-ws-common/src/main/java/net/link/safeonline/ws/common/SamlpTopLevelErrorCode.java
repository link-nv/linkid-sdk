/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.ws.common;

import java.util.HashMap;
import java.util.Map;


/**
 * SAMLp version 2.0 Top Level Error Code.
 * 
 * @author fcorneli
 * 
 */
public enum SamlpTopLevelErrorCode {
    SUCCESS("urn:oasis:names:tc:SAML:2.0:status:Success"),
    REQUESTER("urn:oasis:names:tc:SAML:2.0:status:Requester"),
    RESPONDER("urn:oasis:names:tc:SAML:2.0:status:Responder"),
    VERSION_MISMATCH("urn:oasis:names:tc:SAML:2.0:status:VersionMismatch");

    private final String                                     errorCode;

    private final static Map<String, SamlpTopLevelErrorCode> errorCodeMap = new HashMap<String, SamlpTopLevelErrorCode>();

    static {
        SamlpTopLevelErrorCode[] errorCodes = SamlpTopLevelErrorCode.values();
        for (SamlpTopLevelErrorCode errorCode : errorCodes) {
            errorCodeMap.put(errorCode.getErrorCode(), errorCode);
        }
    }


    private SamlpTopLevelErrorCode(String errorCode) {

        this.errorCode = errorCode;
    }

    public String getErrorCode() {

        return errorCode;
    }

    @Override
    public String toString() {

        return errorCode;
    }

    public static SamlpTopLevelErrorCode getSamlpTopLevelErrorCode(String errorCode) {

        SamlpTopLevelErrorCode samlpTopLevelErrorCode = errorCodeMap.get(errorCode);
        if (null == samlpTopLevelErrorCode)
            throw new IllegalArgumentException("unknown SAMLp top-level error code: " + errorCode);
        return samlpTopLevelErrorCode;
    }
}
