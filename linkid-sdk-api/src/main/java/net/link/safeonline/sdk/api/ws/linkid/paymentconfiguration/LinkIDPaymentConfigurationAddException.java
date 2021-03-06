/*
 * safe-online - linkid-sdk-api
 *
 * Copyright 2006-2016 linkID Inc. All rights reserved.
 * linkID Inc. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.ws.linkid.paymentconfiguration;

public class LinkIDPaymentConfigurationAddException extends Exception {

    private final LinkIDPaymentConfigurationAddErrorCode errorCode;

    public LinkIDPaymentConfigurationAddException(final String message, final LinkIDPaymentConfigurationAddErrorCode errorCode) {

        super( String.format( "Error code: \"%s\", message=\"%s\"", errorCode, message ) );
        this.errorCode = errorCode;
    }

    public LinkIDPaymentConfigurationAddErrorCode getErrorCode() {

        return errorCode;
    }

}
