/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.ws.configuration;

public class ThemesException extends Exception {

    private final ThemesErrorCode errorCode;

    public ThemesException(final ThemesErrorCode errorCode) {

        this.errorCode = errorCode;
    }

    public ThemesErrorCode getErrorCode() {

        return errorCode;
    }
}
