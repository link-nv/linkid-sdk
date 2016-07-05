/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.ws.linkid.wallet;

public class LinkIDWalletRemoveException extends Exception {

    private final LinkIDWalletRemoveErrorCode errorCode;

    public LinkIDWalletRemoveException(final LinkIDWalletRemoveErrorCode errorCode) {

        super( String.format( "Error code: \"%s\"", errorCode ) );
        this.errorCode = errorCode;
    }

    public LinkIDWalletRemoveErrorCode getErrorCode() {

        return errorCode;
    }
}
