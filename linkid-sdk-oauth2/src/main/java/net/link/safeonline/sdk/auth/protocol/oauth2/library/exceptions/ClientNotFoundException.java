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
