package net.link.safeonline.sdk.auth.protocol.oauth2.lib.exceptions;

import net.link.safeonline.sdk.auth.protocol.oauth2.lib.OAuth2Message;


/**
 * TODO description
 * <p/>
 * Date: 08/05/12
 * Time: 14:35
 *
 * @author: sgdesmet
 */
public class AuthorizationException extends OauthValidationException {

    public AuthorizationException() {

        super( OAuth2Message.ErrorType.INVALID_CLIENT );
    }

    public AuthorizationException(final String s) {

        super( OAuth2Message.ErrorType.INVALID_CLIENT, s );
    }

    public AuthorizationException(final String s, final Throwable throwable) {

        super( OAuth2Message.ErrorType.INVALID_CLIENT, s, throwable );
    }

    public AuthorizationException(final Throwable throwable) {

        super( OAuth2Message.ErrorType.INVALID_CLIENT, throwable );
    }
}
