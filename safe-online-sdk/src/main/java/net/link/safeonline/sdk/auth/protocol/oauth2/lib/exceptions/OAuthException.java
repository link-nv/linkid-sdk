package net.link.safeonline.sdk.auth.protocol.oauth2.lib.exceptions;

import net.link.safeonline.sdk.auth.protocol.oauth2.lib.OAuth2Message;


/**
 * TODO description
 * <p/>
 * Date: 22/03/12
 * Time: 14:36
 *
 * @author: sgdesmet
 */
public class OAuthException extends Exception {

    protected OAuth2Message.ErrorType errorType;

    public void setErrorType(final OAuth2Message.ErrorType errorType) {

        this.errorType = errorType;
    }

    public OAuth2Message.ErrorType getErrorType() {

        return errorType;
    }

    public OAuthException(final OAuth2Message.ErrorType errorType) {

        super( errorType.toString() );
        this.errorType = errorType;
    }

    public OAuthException(final OAuth2Message.ErrorType errorType, final String s) {

        super( s );
        this.errorType = errorType;
    }

    public OAuthException(final OAuth2Message.ErrorType errorType, final String s, final Throwable throwable) {

        super( s, throwable );
        this.errorType = errorType;
    }

    public OAuthException(final OAuth2Message.ErrorType errorType, final Throwable throwable) {

        super( throwable );
        this.errorType = errorType;
    }
}
