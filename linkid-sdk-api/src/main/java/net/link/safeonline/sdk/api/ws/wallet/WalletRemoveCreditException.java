/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.ws.wallet;

public class WalletRemoveCreditException extends Exception {

    private final WalletRemoveCreditErrorCode errorCode;

    public WalletRemoveCreditException(final WalletRemoveCreditErrorCode errorCode) {

        this.errorCode = errorCode;
    }

    public WalletRemoveCreditErrorCode getErrorCode() {

        return errorCode;
    }
}
