/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.ws.wallet;

import java.util.List;


public class WalletEnrollException extends Exception {

    private final WalletEnrollErrorCode errorCode;
    private final List<String>          unknownUsers;

    public WalletEnrollException(final WalletEnrollErrorCode errorCode, final List<String> unknownUsers) {

        this.errorCode = errorCode;
        this.unknownUsers = unknownUsers;
    }

    public WalletEnrollErrorCode getErrorCode() {

        return errorCode;
    }

    public List<String> getUnknownUsers() {

        return unknownUsers;
    }
}
