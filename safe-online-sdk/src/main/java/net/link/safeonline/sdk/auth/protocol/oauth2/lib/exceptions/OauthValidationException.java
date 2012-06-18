package net.link.safeonline.sdk.auth.protocol.oauth2.lib.exceptions;

import net.link.safeonline.sdk.auth.protocol.oauth2.lib.OAuth2Message;


/**
 * TODO description
 * <p/>
 * Date: 19/03/12
 * Time: 14:57
 *
 * @author: sgdesmet
 */
public class OauthValidationException extends OAuthException {

    public OauthValidationException(final OAuth2Message.ErrorType errorType) {

        super( errorType );
    }

    public OauthValidationException(final OAuth2Message.ErrorType errorType, final String s) {

        super( errorType, s );
    }

    public OauthValidationException(final OAuth2Message.ErrorType errorType, final String s, final Throwable throwable) {

        super( errorType, s, throwable );
    }

    public OauthValidationException(final OAuth2Message.ErrorType errorType, final Throwable throwable) {

        super( errorType, throwable );
    }
}
