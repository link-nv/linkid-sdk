/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.ws.linkid.ltqr;

/**
 * Created by wvdhaute
 * Date: 14/01/14
 * Time: 11:03
 */
public class LinkIDLTQRInfoException extends Exception {

    private final LinkIDLTQRErrorCode linkIDErrorCode;

    public LinkIDLTQRInfoException(final LinkIDLTQRErrorCode linkIDErrorCode) {

        this.linkIDErrorCode = linkIDErrorCode;
    }

    public LinkIDLTQRErrorCode getErrorCode() {

        return linkIDErrorCode;
    }
}
