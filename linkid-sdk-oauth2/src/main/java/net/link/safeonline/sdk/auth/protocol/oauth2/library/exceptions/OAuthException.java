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
 * Date: 22/03/12
 * Time: 14:36
 *
 * @author sgdesmet
 */
public class OAuthException extends Exception {

    protected final OAuth2Message.ErrorType errorType;

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
