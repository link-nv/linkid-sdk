/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.ws.wallet;

public class LinkIDWalletRemoveCreditException extends Exception {

    private final LinkIDWalletRemoveCreditErrorCode errorCode;

    public LinkIDWalletRemoveCreditException(final LinkIDWalletRemoveCreditErrorCode errorCode) {

        this.errorCode = errorCode;
    }

    public LinkIDWalletRemoveCreditErrorCode getErrorCode() {

        return errorCode;
    }
}
