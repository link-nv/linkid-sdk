/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.ws.wallet;

public class WalletAddCreditException extends Exception {

    private final WalletAddCreditErrorCode errorCode;

    public WalletAddCreditException(final WalletAddCreditErrorCode errorCode) {

        this.errorCode = errorCode;
    }

    public WalletAddCreditErrorCode getErrorCode() {

        return errorCode;
    }
}
