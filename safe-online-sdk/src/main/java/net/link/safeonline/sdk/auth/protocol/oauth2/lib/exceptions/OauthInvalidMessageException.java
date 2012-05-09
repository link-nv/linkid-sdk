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
public class OauthInvalidMessageException extends OauthValidationException {

    public OauthInvalidMessageException() {

        super( OAuth2Message.ErrorType.INVALID_REQUEST );
    }

    public OauthInvalidMessageException(final String s) {

        super( OAuth2Message.ErrorType.INVALID_REQUEST, s );
    }

    public OauthInvalidMessageException(final String s, final Throwable throwable) {

        super( OAuth2Message.ErrorType.INVALID_REQUEST, s, throwable );
    }

    public OauthInvalidMessageException(final Throwable throwable) {

        super( OAuth2Message.ErrorType.INVALID_REQUEST, throwable );
    }
}
