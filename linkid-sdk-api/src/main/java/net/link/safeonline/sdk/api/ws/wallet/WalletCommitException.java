/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.ws.wallet;

public class WalletCommitException extends Exception {

    private final WalletCommitErrorCode errorCode;

    public WalletCommitException(final WalletCommitErrorCode errorCode) {

        this.errorCode = errorCode;
    }

    public WalletCommitErrorCode getErrorCode() {

        return errorCode;
    }
}
