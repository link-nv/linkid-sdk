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
 * Error codes used by the web service authentication WS's.
 * 
 * @author wvdhaute
 * 
 */
public enum WSAuthenticationErrorCode {
    ALREADY_AUTHENTICATED("urn:net:lin-k:safe-online:ws:auth:status:AlreadyAuthenticated"),
    ALREADY_SUBSCRIBED("urn:net:lin-k:safe-online:ws:auth:status:AlreadySubscribed"),
    APPLICATION_NOT_FOUND("urn:net:lin-k:safe-online:ws:auth:status:ApplicationNotFound"),
    APPLICATION_IDENTITY_NOT_FOUND("urn:net:lin-k:safe-online:ws:auth:status:ApplicationIdentityNotFound"),
    ATTRIBUTE_NOT_FOUND("urn:net:lin-k:safe-online:ws:auth:status:AttributeNotFound"),
    ATTRIBUTE_TYPE_NOT_FOUND("urn:net:lin-k:safe-online:ws:auth:status:AttributeTypeNotFound"),
    ATTRIBUTE_UNAVAILABLE("urn:net:lin-k:safe-online:ws:auth:status:AttributeUnavailable"),
    AUTHENTICATION_FAILED("urn:net:lin-k:safe-online:ws:auth:status:AuthenticationFailed"),
    DEVICE_DISABLED("urn:net:lin-k:safe-online:ws:auth:status:DeviceDisabled"),
    DEVICE_NOT_FOUND("urn:net:lin-k:safe-online:ws:auth:status:DeviceNotFound"),
    EMPTY_DEVICE_POLICY("urn:net:lin-k:safe-online:ws:auth:status:EmptyDevicePolicy"),
    INSUFFICIENT_CREDENTIALS("urn:net:lin-k:safe-online:ws:auth:status:InsufficientCredentials"),
    INSUFFICIENT_DEVICE("urn:net:lin-k:safe-online:ws:auth:status:InsufficientDevice"),
    MISSING_ATTRIBUTE_VALUE_IS_NULL("urn:net:lin-k:safe-online:ws:auth:status:MissingAttributeValueIsNull"),
    NODE_MAPPING_NOT_FOUND("urn:net:lin-k:safe-online:ws:auth:status:NodeMappingNotFound"),
    NODE_NOT_FOUND("urn:net:lin-k:safe-online:ws:auth:status:NodeNotFound"),
    NOT_AUTHENTICATED("urn:net:lin-k:safe-online:ws:auth:status:NotAuthenticated"),
    PERMISSION_DENIED("urn:net:lin-k:safe-online:ws:auth:status:PermissionDenied"),
    REQUEST_DENIED("urn:net:lin-k:safe-online:ws:auth:status:RequestDenied"),
    REQUEST_FAILED("urn:net:lin-k:safe-online:ws:auth:status:RequestFailed"),
    SUBJECT_NOT_FOUND("urn:net:lin-k:safe-online:ws:auth:status:SubjectNotFound"),
    SUBSCRIPTION_NOT_FOUND("urn:net:lin-k:safe-online:ws:auth:status:SubscriptionNotFound"),
    SUCCESS("urn:oasis:names:tc:SAML:2.0:status:Success");

    private final String                                        errorCode;

    private final static Map<String, WSAuthenticationErrorCode> errorCodeMap = new HashMap<String, WSAuthenticationErrorCode>();

    static {
        WSAuthenticationErrorCode[] errorCodes = WSAuthenticationErrorCode.values();
        for (WSAuthenticationErrorCode errorCode : errorCodes) {
            errorCodeMap.put(errorCode.getErrorCode(), errorCode);
        }
    }


    private WSAuthenticationErrorCode(String errorCode) {

        this.errorCode = errorCode;
    }

    public String getErrorCode() {

        return this.errorCode;
    }

    @Override
    public String toString() {

        return this.errorCode;
    }

    public static WSAuthenticationErrorCode getWSAuthenticationErrorCode(String errorCode) {

        WSAuthenticationErrorCode wsAuthenticationErrorCode = errorCodeMap.get(errorCode);
        if (null == wsAuthenticationErrorCode)
            throw new IllegalArgumentException("unknown ws authentication error code: " + errorCode);
        return wsAuthenticationErrorCode;
    }

}
