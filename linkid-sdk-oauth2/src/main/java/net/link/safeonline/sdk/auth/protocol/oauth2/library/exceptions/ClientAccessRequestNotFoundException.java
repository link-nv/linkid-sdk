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
 * Date: 02/05/12
 * Time: 13:43
 *
 * @author sgdesmet
 */
public class ClientAccessRequestNotFoundException extends OAuthException {

    public ClientAccessRequestNotFoundException() {

        super( OAuth2Message.ErrorType.SERVER_ERROR );
    }

    public ClientAccessRequestNotFoundException(final String s) {

        super( OAuth2Message.ErrorType.SERVER_ERROR, s );
    }

    public ClientAccessRequestNotFoundException(final String s, final Throwable throwable) {

        super( OAuth2Message.ErrorType.SERVER_ERROR, s, throwable );
    }

    public ClientAccessRequestNotFoundException(final Throwable throwable) {

        super( OAuth2Message.ErrorType.SERVER_ERROR, throwable );
    }
}
