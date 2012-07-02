package net.link.safeonline.sdk.auth.protocol.oauth2.lib.exceptions;

import net.link.safeonline.sdk.auth.protocol.oauth2.lib.OAuth2Message;


/**
 * TODO description
 * <p/>
 * Date: 15/03/12
 * Time: 16:24
 *
 * @author: sgdesmet
 */
public class OAuthInvalidMessageException extends OAuthException {

    public OAuthInvalidMessageException() {

        super( OAuth2Message.ErrorType.INVALID_REQUEST );
    }

    public OAuthInvalidMessageException(final String s) {

        super( OAuth2Message.ErrorType.INVALID_REQUEST, s );
    }

    public OAuthInvalidMessageException(final String s, final Throwable throwable) {

        super( OAuth2Message.ErrorType.INVALID_REQUEST, s, throwable );
    }

    public OAuthInvalidMessageException(final Throwable throwable) {

        super( OAuth2Message.ErrorType.INVALID_REQUEST, throwable );
    }
}
