/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.ws.linkid.capture;

public class LinkIDCaptureException extends Exception {

    private final LinkIDCaptureErrorCode linkIDCaptureErrorCode;

    public LinkIDCaptureException(final LinkIDCaptureErrorCode linkIDCaptureErrorCode) {

        this.linkIDCaptureErrorCode = linkIDCaptureErrorCode;
    }

    public LinkIDCaptureErrorCode getErrorCode() {

        return linkIDCaptureErrorCode;
    }
}
