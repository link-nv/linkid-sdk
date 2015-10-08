/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.ws.linkid.wallet;

public class LinkIDWalletGetInfoException extends Exception {

    private final LinkIDWalletGetInfoErrorCode errorCode;

    public LinkIDWalletGetInfoException(final LinkIDWalletGetInfoErrorCode errorCode) {

        this.errorCode = errorCode;
    }

    public LinkIDWalletGetInfoErrorCode getErrorCode() {

        return errorCode;
    }
}
