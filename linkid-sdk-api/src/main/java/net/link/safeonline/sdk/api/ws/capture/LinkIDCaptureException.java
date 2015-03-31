/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.ws.capture;

public class LinkIDCaptureException extends Exception {

    private final LinkIDErrorCode linkIDErrorCode;

    public LinkIDCaptureException(final LinkIDErrorCode linkIDErrorCode) {

        this.linkIDErrorCode = linkIDErrorCode;
    }

    public LinkIDErrorCode getLinkIDErrorCode() {

        return linkIDErrorCode;
    }
}
