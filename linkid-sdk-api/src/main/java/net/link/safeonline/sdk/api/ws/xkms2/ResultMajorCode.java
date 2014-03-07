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
 * XKMS 2 ResultMajor URI's
 *
 * @author wvdhaute
 */
public enum ResultMajorCode {

    SUCCESS( "http://www.w3.org/2002/03/xkms#Success" ),
    VERSION_MISMATCH( "http://www.w3.org/2002/03/xkms#VersionMismatch" ),
    SENDER( "http://www.w3.org/2002/03/xkms#Sender" ),
    RECEIVER( "http://www.w3.org/2002/03/xkms#Receiver" ),
    REPRESENT( "http://www.w3.org/2002/03/xkms#Represent" ),
    PENDING( "http://www.w3.org/2002/03/xkms#Pending" );

    private final String errorCode;

    private static final Map<String, ResultMajorCode> errorCodeMap = new HashMap<String, ResultMajorCode>();

    static {
        ResultMajorCode[] errorCodes = ResultMajorCode.values();
        for (ResultMajorCode errorCode : errorCodes) {
            errorCodeMap.put( errorCode.getErrorCode(), errorCode );
        }
    }

    ResultMajorCode(String errorCode) {

        this.errorCode = errorCode;
    }

    public String getErrorCode() {

        return errorCode;
    }

    public static ResultMajorCode getResultMajorCode(String errorCode) {

        ResultMajorCode resultMajorCode = errorCodeMap.get( errorCode );
        if (null == resultMajorCode)
            throw new IllegalArgumentException( "unknown ResultMajor error code: " + errorCode );
        return resultMajorCode;
    }
}
