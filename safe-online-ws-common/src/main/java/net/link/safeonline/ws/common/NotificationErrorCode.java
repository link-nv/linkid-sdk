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
 * Error codes used by the WS-Notification WS's.
 * 
 * @author wvdhaute
 * 
 */
public enum NotificationErrorCode {
    SUCCESS("urn:net:lin-k:safe-online:notification:status:Success"),
    SUBSCRIPTION_NOT_FOUND("urn:net:lin-k:safe-online:notification:status:SubscriptionNotFound"),
    PERMISSION_DENIED("urn:net:lin-k:safe-online:notification:status:PermissionDenied"),
    SUBSCRIPTION_FAILED("urn:net:lin-k:safe-online:notification:status:SubscriptionFailed");

    private final String                                    errorCode;

    private final static Map<String, NotificationErrorCode> errorCodeMap = new HashMap<String, NotificationErrorCode>();

    static {
        NotificationErrorCode[] errorCodes = NotificationErrorCode.values();
        for (NotificationErrorCode errorCode : errorCodes) {
            errorCodeMap.put(errorCode.getErrorCode(), errorCode);
        }
    }


    private NotificationErrorCode(String errorCode) {

        this.errorCode = errorCode;
    }

    public String getErrorCode() {

        return this.errorCode;
    }

    @Override
    public String toString() {

        return this.errorCode;
    }

    public static NotificationErrorCode getNotificationErrorCode(String errorCode) {

        NotificationErrorCode notificationErrorCode = errorCodeMap.get(errorCode);
        if (null == notificationErrorCode)
            throw new IllegalArgumentException("unknown Notification error code: " + errorCode);
        return notificationErrorCode;
    }

}
