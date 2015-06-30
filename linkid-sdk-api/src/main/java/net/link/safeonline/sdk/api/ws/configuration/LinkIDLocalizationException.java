/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.ws.configuration;

public class LinkIDLocalizationException extends Exception {

    private final LinkIDLocalizationErrorCode errorCode;

    public LinkIDLocalizationException(final LinkIDLocalizationErrorCode errorCode) {

        this.errorCode = errorCode;
    }

    public LinkIDLocalizationErrorCode getErrorCode() {

        return errorCode;
    }
}
