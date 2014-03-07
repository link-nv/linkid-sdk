/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.auth.protocol.oauth2.library;

import java.io.Serializable;
import java.util.EnumSet;
import org.jetbrains.annotations.Nullable;


/**
 * <p/>
 * Date: 15/03/12
 * Time: 16:10
 *
 * @author sgdesmet
 */
public interface OAuth2Message extends Serializable {

    String RESPONSE_TYPE = "response_type";
    String CLIENT_ID     = "client_id";
    String CLIENT_SECRET = "client_secret";
    String REDIRECT_URI  = "redirect_uri";
    String SCOPE         = "scope";
    String STATE         = "state";

    String GRANT_TYPE = "grant_type";

    String USERNAME = "username";
    String PASSWORD = "password";

    String CODE = "code";

    String ERROR             = "error";
    String ERROR_DESCRIPTION = "error_description";
    String ERROR_URI         = "error_uri";

    String REFRESH_TOKEN = "refresh_token";

    String ACCESS_TOKEN = "access_token";
    String TOKEN_TYPE   = "token_type";
    String EXPIRES_IN   = "expires_in";

    String AUDIENCE = "audience"; //not official oauth, used for token validation, used same name as Google API (https://developers.google.com/accounts/docs/OAuth2Login)
    String USER_ID  = "user_id";


    enum ResponseType {

        CODE( "code" ),
        TOKEN( "token" );

        private final String code;

        ResponseType(String code) {

            this.code = code;
        }

        @Override
        public String toString() {

            return code;
        }

        @Nullable
        public static ResponseType fromString(String text) {

            for (ResponseType type : EnumSet.allOf( ResponseType.class )) {
                if (type.toString().equals( text ))
                    return type;
            }
            return null;
        }
    }


    enum ErrorType {

        INVALID_REQUEST( "invalid_request" ),
        INVALID_CLIENT( "invalid_client" ),
        UNAUTHORIZED_CLIENT( "unauthorized_client" ),
        ACCESS_DENIED( "access_denied" ),
        UNSUPPORTED_RESPONSE_TYPE( "unsupported_response_type" ),
        INVALID_SCOPE( "invalid_scope" ),
        SERVER_ERROR( "server_error" ),
        TEMPORARILY_UNAVAILABLE( "temporarily_unavailable" ),
        INVALID_GRANT( "invalid_grant" ),
        UNSUPPORTED_GRANT_TYPE( "unsupported_grant_type" );

        private final String type;

        ErrorType(String type) {

            this.type = type;
        }

        @Override
        public String toString() {

            return type;
        }

        public static ErrorType fromString(String text) {

            for (ErrorType type : EnumSet.allOf( ErrorType.class )) {
                if (type.toString().equals( text ))
                    return type;
            }
            return null;
        }

    }


    enum GrantType {
        AUTHORIZATION_CODE( "authorization_code" ),
        PASSWORD( "password" ),
        CLIENT_CREDENTIALS( "client_credentials" ),
        REFRESH_TOKEN( "refresh_token" );

        private String type;

        GrantType(String type) {

            this.type = type;
        }

        @Override
        public String toString() {

            return type;
        }

        @Nullable
        public static GrantType fromString(String text) {

            for (GrantType type : EnumSet.allOf( GrantType.class )) {
                if (type.toString().equals( text ))
                    return type;
            }
            return null;
        }
    }
}
