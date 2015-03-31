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
public enum LinkIDResultMajorCode {

    SUCCESS( "http://www.w3.org/2002/03/xkms#Success" ),
    VERSION_MISMATCH( "http://www.w3.org/2002/03/xkms#VersionMismatch" ),
    SENDER( "http://www.w3.org/2002/03/xkms#Sender" ),
    RECEIVER( "http://www.w3.org/2002/03/xkms#Receiver" ),
    REPRESENT( "http://www.w3.org/2002/03/xkms#Represent" ),
    PENDING( "http://www.w3.org/2002/03/xkms#Pending" );

    private final String errorCode;

    private static final Map<String, LinkIDResultMajorCode> errorCodeMap = new HashMap<String, LinkIDResultMajorCode>();

    static {
        LinkIDResultMajorCode[] errorCodes = LinkIDResultMajorCode.values();
        for (LinkIDResultMajorCode errorCode : errorCodes) {
            errorCodeMap.put( errorCode.getErrorCode(), errorCode );
        }
    }

    LinkIDResultMajorCode(String errorCode) {

        this.errorCode = errorCode;
    }

    public String getErrorCode() {

        return errorCode;
    }

    public static LinkIDResultMajorCode getResultMajorCode(String errorCode) {

        LinkIDResultMajorCode linkIDResultMajorCode = errorCodeMap.get( errorCode );
        if (null == linkIDResultMajorCode)
            throw new IllegalArgumentException( "unknown ResultMajor error code: " + errorCode );
        return linkIDResultMajorCode;
    }
}
