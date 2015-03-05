/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.ws.wallet;

public class WalletRemoveException extends Exception {

    private final WalletRemoveErrorCode errorCode;

    public WalletRemoveException(final WalletRemoveErrorCode errorCode) {

        this.errorCode = errorCode;
    }

    public WalletRemoveErrorCode getErrorCode() {

        return errorCode;
    }
}
