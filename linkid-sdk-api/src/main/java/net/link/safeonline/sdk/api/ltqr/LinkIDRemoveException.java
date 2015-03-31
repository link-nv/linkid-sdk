/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.ltqr;

/**
 * Created by wvdhaute
 * Date: 14/01/14
 * Time: 11:03
 */
public class LinkIDRemoveException extends Exception {

    private final LinkIDErrorCode linkIDErrorCode;

    public LinkIDRemoveException(final LinkIDErrorCode linkIDErrorCode) {

        this.linkIDErrorCode = linkIDErrorCode;
    }

    public LinkIDErrorCode getLinkIDErrorCode() {

        return linkIDErrorCode;
    }
}
