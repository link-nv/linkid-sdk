package net.link.safeonline.sdk.auth.protocol.oauth2.library.exceptions;

import net.link.safeonline.sdk.auth.protocol.oauth2.library.OAuth2Message;


/**
 * TODO description
 * <p/>
 * Date: 08/05/12
 * Time: 14:35
 *
 * @author sgdesmet
 */
public class OAuthAuthorizationException extends OAuthException {

    public OAuthAuthorizationException() {

        super( OAuth2Message.ErrorType.INVALID_CLIENT );
    }

    public OAuthAuthorizationException(final String s) {

        super( OAuth2Message.ErrorType.INVALID_CLIENT, s );
    }

    public OAuthAuthorizationException(final String s, final Throwable throwable) {

        super( OAuth2Message.ErrorType.INVALID_CLIENT, s, throwable );
    }

    public OAuthAuthorizationException(final Throwable throwable) {

        super( OAuth2Message.ErrorType.INVALID_CLIENT, throwable );
    }
}
