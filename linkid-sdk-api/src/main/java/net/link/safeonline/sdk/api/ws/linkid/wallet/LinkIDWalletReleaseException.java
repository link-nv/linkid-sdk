/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.ws.linkid.wallet;

public class LinkIDWalletReleaseException extends Exception {

    private final LinkIDWalletReleaseErrorCode errorCode;

    public LinkIDWalletReleaseException(final LinkIDWalletReleaseErrorCode errorCode) {

        super( String.format( "Error code: \"%s\"", errorCode ) );
        this.errorCode = errorCode;
    }

    public LinkIDWalletReleaseErrorCode getErrorCode() {

        return errorCode;
    }
}
