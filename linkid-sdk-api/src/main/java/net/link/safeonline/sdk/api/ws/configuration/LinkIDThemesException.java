/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.ws.configuration;

public class LinkIDThemesException extends Exception {

    private final LinkIDThemesErrorCode errorCode;

    public LinkIDThemesException(final LinkIDThemesErrorCode errorCode) {

        this.errorCode = errorCode;
    }

    public LinkIDThemesErrorCode getErrorCode() {

        return errorCode;
    }
}
