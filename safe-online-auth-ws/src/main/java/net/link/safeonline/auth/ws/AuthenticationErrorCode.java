/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.auth.ws;

import java.util.HashMap;
import java.util.Map;


/**
 * Error codes used by the authentication protocols.
 *
 * @author wvdhaute
 */
public enum AuthenticationErrorCode {

    ALREADY_AUTHENTICATED( "urn:net:lin-k:safe-online:ws:auth:status:AlreadyAuthenticated" ),
    ALREADY_SUBSCRIBED( "urn:net:lin-k:safe-online:ws:auth:status:AlreadySubscribed" ),
    APPLICATION_NOT_FOUND( "urn:net:lin-k:safe-online:ws:auth:status:ApplicationNotFound" ),
    ATTRIBUTE_TYPE_NOT_FOUND( "urn:net:lin-k:safe-online:ws:auth:status:AttributeTypeNotFound" ),
    AUTHENTICATION_FAILED( "urn:net:lin-k:safe-online:ws:auth:status:AuthenticationFailed" ),
    DEVICE_DISABLED( "urn:net:lin-k:safe-online:ws:auth:status:DeviceDisabled" ),
    DEVICE_NOT_FOUND( "urn:net:lin-k:safe-online:ws:auth:status:DeviceNotFound" ),
    EMPTY_DEVICE_POLICY( "urn:net:lin-k:safe-online:ws:auth:status:EmptyDevicePolicy" ),
    IDENTITY_UNAVAILABLE( "urn:net:lin-k:safe-online:ws:auth:status:IdentityUnavilable" ),
    INSUFFICIENT_IDENTITY( "urn:net:lin-k:safe-online:ws:auth:status:InsufficientIdentity" ),
    INSUFFICIENT_CREDENTIALS( "urn:net:lin-k:safe-online:ws:auth:status:InsufficientCredentials" ),
    INSUFFICIENT_DEVICE( "urn:net:lin-k:safe-online:ws:auth:status:InsufficientDevice" ),
    INVALID_CREDENTIALS( "urn:net:lin-k:safe-online:ws:auth:status:InvalidCredentials" ),
    INTERNAL_ERROR( "urn:net:lin-k:safe-online:ws:auth:status:InternalError" ),
    NOT_AUTHENTICATED( "urn:net:lin-k:safe-online:ws:auth:status:NotAuthenticated" ),
    PERMISSION_DENIED( "urn:net:lin-k:safe-online:ws:auth:status:PermissionDenied" ),
    PKI_REVOKED( "urn:net:lin-k:safe-online:ws:auth:status:PkiRevoked" ),
    PKI_SUSPENDED( "urn:net:lin-k:safe-online:ws:auth:status:PkiSuspended" ),
    PKI_EXPIRED( "urn:net:lin-k:safe-online:ws:auth:status:PkiExpired" ),
    PKI_NOT_YET_VALID( "urn:net:lin-k:safe-online:ws:auth:status:PkiNotYetValid" ),
    PKI_INVALID( "urn:net:lin-k:safe-online:ws:auth:status:PkiInvalid" ),
    REQUEST_DENIED( "urn:net:lin-k:safe-online:ws:auth:status:RequestDenied" ),
    REQUEST_FAILED( "urn:net:lin-k:safe-online:ws:auth:status:RequestFailed" ),
    SESSION_EXPIRED( "urn:net:lin-k:safe-online:ws:auth:status:SessionExpired" ),
    SUBJECT_NOT_FOUND( "urn:net:lin-k:safe-online:ws:auth:status:SubjectNotFound" ),
    SUBSCRIPTION_NOT_FOUND( "urn:net:lin-k:safe-online:ws:auth:status:SubscriptionNotFound" ),
    LANGUAGE_NOT_FOUND( "urn:net:lin-k:safe-online:ws:auth:status:LanguageNotFound" ),
    SUCCESS( "urn:oasis:names:tc:SAML:2.0:status:Success" );

    private final String errorCode;

    private static final Map<String, AuthenticationErrorCode> errorCodeMap = new HashMap<String, AuthenticationErrorCode>();

    static {
        AuthenticationErrorCode[] errorCodes = AuthenticationErrorCode.values();
        for (AuthenticationErrorCode errorCode : errorCodes)
            errorCodeMap.put( errorCode.getErrorCode(), errorCode );
    }

    AuthenticationErrorCode(String errorCode) {

        this.errorCode = errorCode;
    }

    public String getErrorCode() {

        return errorCode;
    }

    @Override
    public String toString() {

        return errorCode;
    }

    public static AuthenticationErrorCode getWSAuthenticationErrorCode(String errorCode) {

        AuthenticationErrorCode authenticationErrorCode = errorCodeMap.get( errorCode );
        if (null == authenticationErrorCode)
            throw new IllegalArgumentException( "unknown ws authentication error code: " + errorCode );
        return authenticationErrorCode;
    }
}
