/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.ws.linkid.ltqr;

import org.jetbrains.annotations.Nullable;


/**
 * Created by wvdhaute
 * Date: 14/01/14
 * Time: 11:03
 */
public class LinkIDLTQRPushException extends Exception {

    private final LinkIDLTQRPushErrorCode errorCode;

    public LinkIDLTQRPushException(final LinkIDLTQRPushErrorCode errorCode, @Nullable final String errorMessage) {

        super( errorMessage );
        this.errorCode = errorCode;
    }

    public LinkIDLTQRPushErrorCode getErrorCode() {

        return errorCode;
    }
}
