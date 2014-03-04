/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.ws;

import java.util.HashMap;
import java.util.Map;


/**
 * Error codes used by the session tracking web service.
 *
 * @author wvdhaute
 */
public enum SessionTrackingErrorCode {

    SUBJECT_NOT_FOUND( "urn:net:lin-k:safe-online:ws:session:status:SubjectNotFound" ),
    TRUSTED_DEVICE_NOT_FOUND( "urn:net:lin-k:safe-online:ws:session:status:TrustedDeviceNotFound" ),
    SUCCESS( "urn:net:lin-k:safe-online:ws:session:status:success" );

    private final String errorCode;

    private final static Map<String, SessionTrackingErrorCode> errorCodeMap = new HashMap<String, SessionTrackingErrorCode>();

    static {
        SessionTrackingErrorCode[] errorCodes = SessionTrackingErrorCode.values();
        for (SessionTrackingErrorCode errorCode : errorCodes)
            errorCodeMap.put( errorCode.getErrorCode(), errorCode );
    }

    private SessionTrackingErrorCode(String errorCode) {

        this.errorCode = errorCode;
    }

    public String getErrorCode() {

        return errorCode;
    }

    @Override
    public String toString() {

        return errorCode;
    }

    public static SessionTrackingErrorCode getSessionTrackingErrorCode(String errorCode) {

        SessionTrackingErrorCode sessionTrackingErrorCode = errorCodeMap.get( errorCode );
        if (null == sessionTrackingErrorCode)
            throw new IllegalArgumentException( "unknown session tracking error code: " + errorCode );
        return sessionTrackingErrorCode;
    }
}
