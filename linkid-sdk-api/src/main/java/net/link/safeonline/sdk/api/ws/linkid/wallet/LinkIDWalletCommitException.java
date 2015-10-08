/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.ws.linkid.wallet;

public class LinkIDWalletCommitException extends Exception {

    private final LinkIDWalletCommitErrorCode errorCode;

    public LinkIDWalletCommitException(final LinkIDWalletCommitErrorCode errorCode) {

        this.errorCode = errorCode;
    }

    public LinkIDWalletCommitErrorCode getErrorCode() {

        return errorCode;
    }
}
