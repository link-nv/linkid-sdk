/*
 * safe-online - linkid-sdk-api
 *
 * Copyright 2006-2016 linkID Inc. All rights reserved.
 * linkID Inc. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.ws.linkid.configuration;

public class LinkIDConfigApplicationsException extends Exception {

    private final LinkIDConfigApplicationsErrorCode errorCode;

    public LinkIDConfigApplicationsException(final String message, final LinkIDConfigApplicationsErrorCode errorCode) {

        super( message );
        this.errorCode = errorCode;
    }

    public LinkIDConfigApplicationsErrorCode getErrorCode() {

        return errorCode;
    }

}
