package net.link.safeonline.sdk.auth.protocol.oauth2.lib.exceptions;

import net.link.safeonline.sdk.auth.protocol.oauth2.lib.OAuth2Message;


/**
 * TODO description
 * <p/>
 * Date: 20/03/12
 * Time: 11:40
 *
 * @author sgdesmet
 */
public class ClientNotFoundException extends OAuthException {

    public ClientNotFoundException() {

        super( OAuth2Message.ErrorType.INVALID_CLIENT );
    }

    public ClientNotFoundException(final String s) {

        super( OAuth2Message.ErrorType.INVALID_CLIENT, s );
    }

    public ClientNotFoundException(final String s, final Throwable throwable) {

        super( OAuth2Message.ErrorType.INVALID_CLIENT, s, throwable );
    }

    public ClientNotFoundException(final Throwable throwable) {

        super( OAuth2Message.ErrorType.INVALID_CLIENT, throwable );
    }
}
