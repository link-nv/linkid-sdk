/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.ws.xkms2;

import java.util.HashMap;
import java.util.Map;


/**
 * XKMS 2 ResultMinor URI's
 *
 * @author wvdhaute
 */
public enum LinkIDResultMinorCode {

    NO_MATCH( "http://www.w3.org/2002/03/xkms#NoMatch" ),
    TOO_MANY_RESPONSES( "http://www.w3.org/2002/03/xkms#TooManyResponses" ),
    INCOMPLETE( "http://www.w3.org/2002/03/xkms#Incomplete" ),
    FAILURE( "http://www.w3.org/2002/03/xkms#Failure" ),
    REFUSED( "http://www.w3.org/2002/03/xkms#Refused" ),
    NO_AUTHENTICATION( "http://www.w3.org/2002/03/xkms#NoAuthentication" ),
    MESSAGE_NOT_SUPPORTED( "http://www.w3.org/2002/03/xkms#MessageNotSupported" ),
    UNKNOWN_RESPONSE_ID( "http://www.w3.org/2002/03/xkms#UnknownResponseId" ),
    REPRESENT_REQUIRED( "http://www.w3.org/2002/03/xkms#RepresentRequired" ),
    NOT_SYNCHRONOUS( "http://www.w3.org/2002/03/xkms#NotSynchronous" ),
    OPTIONAL_ELEMENT_NOT_SUPPORTED( "http://www.w3.org/2002/03/xkms#OptionalElementNotSupported" ),
    PROOF_OF_POSSESSION_REQUIRED( "http://www.w3.org/2002/03/xkms#ProofOfPossessionRequired" ),
    TIME_INSTANT_NOT_SUPPORTED( "http://www.w3.org/2002/03/xkms#TimeInstantNotSupported" ),
    TIME_INSTANT_OUT_OF_RANGE( "http://www.w3.org/2002/03/xkms#TimeInstantOutOfRange" );

    private final String errorCode;

    private static final Map<String, LinkIDResultMinorCode> errorCodeMap = new HashMap<String, LinkIDResultMinorCode>();

    static {
        LinkIDResultMinorCode[] errorCodes = LinkIDResultMinorCode.values();
        for (LinkIDResultMinorCode errorCode : errorCodes) {
            errorCodeMap.put( errorCode.getErrorCode(), errorCode );
        }
    }

    LinkIDResultMinorCode(String errorCode) {

        this.errorCode = errorCode;
    }

    public String getErrorCode() {

        return errorCode;
    }

    public static LinkIDResultMinorCode getResultMinorCode(String errorCode) {

        LinkIDResultMinorCode linkIDResultMinorCode = errorCodeMap.get( errorCode );
        if (null == linkIDResultMinorCode)
            throw new IllegalArgumentException( "unknown ResultMinor error code: " + errorCode );
        return linkIDResultMinorCode;
    }

}
