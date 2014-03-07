/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.auth.protocol.oauth2.library.exceptions;

import net.link.safeonline.sdk.auth.protocol.oauth2.library.OAuth2Message;


/**
 * <p/>
 * Date: 15/03/12
 * Time: 16:24
 *
 * @author sgdesmet
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
