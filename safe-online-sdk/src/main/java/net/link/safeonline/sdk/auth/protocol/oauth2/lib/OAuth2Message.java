package net.link.safeonline.sdk.auth.protocol.oauth2.lib;

import java.io.Serializable;
import java.util.EnumSet;


/**
 * TODO description
 * <p/>
 * Date: 15/03/12
 * Time: 16:10
 *
 * @author: sgdesmet
 */
public interface OAuth2Message extends Serializable {
    public static final String RESPONSE_TYPE = "response_type";
    public static final String CLIENT_ID = "client_id";
    public static final String CLIENT_SECRET = "client_secret";
    public static final String REDIRECT_URI = "redirect_uri";
    public static final String SCOPE = "scope";
    public static final String STATE = "state";

    public static final String GRANT_TYPE = "grant_type";

    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    
    public static final String CODE = "code";
    
    public static final String ERROR = "error";
    public static final String ERROR_DESCRIPTION = "error_description";
    public static final String ERROR_URI = "error_uri";

    public static final String REFRESH_TOKEN = "refresh_token";

    public static final String ACCESS_TOKEN = "access_token";
    public static final String TOKEN_TYPE = "token_type";
    public static final String EXPIRES_IN = "expires_in";

    public static final String AUDIENCE = "audience"; //not official oauth, used for token validation, used same name as Google API (https://developers.google.com/accounts/docs/OAuth2Login)
    public static final String USER_ID = "user_id";

    public enum ResponseType {

        CODE("code"),
        TOKEN("token");

        private String code;

        ResponseType(String code) {
            this.code = code;
        }

        @Override
        public String toString() {
            return code;
        }

        public static ResponseType fromString(String text){
            for (ResponseType type : EnumSet.allOf( ResponseType.class )){
                if (type.toString().equals( text ))
                    return type;
            }
            return null;
        }
    }

    public enum ErrorType{

        INVALID_REQUEST("invalid_request"),
        INVALID_CLIENT("invalid_client"),
        UNAUTHORIZED_CLIENT("unauthorized_client"),
        ACCESS_DENIED("access_denied"),
        UNSUPPORTED_RESPONSE_TYPE ("unsupported_response_type"),
        INVALID_SCOPE ("invalid_scope"),
        SERVER_ERROR("server_error"),
        TEMPORARILY_UNAVAILABLE("temporarily_unavailable"),
        INVALID_GRANT("invalid_grant"),
        UNSUPPORTED_GRANT_TYPE("unsupported_grant_type"),
        INVALID_TOKEN("invalid_token");

        private String type;

        ErrorType(String type) {
            this.type = type;
        }

        @Override
        public String toString() {
            return type;
        }

        public static ErrorType fromString(String text){
            for (ErrorType type : EnumSet.allOf( ErrorType.class )){
                if (type.toString().equals( text ))
                    return type;
            }
            return null;
        }

    }

    public enum GrantType{
        AUTHORIZATION_CODE ("authorization_code"),
        PASSWORD ("password"),
        CLIENT_CREDENTIALS ("client_credentials"),
        REFRESH_TOKEN ("refresh_token");

        private String type;

        GrantType(String type) {
            this.type = type;
        }

        @Override
        public String toString() {
            return type;
        }

        public static GrantType fromString(String text){
            for (GrantType type : EnumSet.allOf( GrantType.class )){
                if (type.toString().equals( text ))
                    return type;
            }
            return null;
        }
    }
}
