/*
 * safe-online - linkid-sdk-api
 *
 * Copyright 2006-2016 linkID Inc. All rights reserved.
 * linkID Inc. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.ws.linkid.paymentconfiguration;

import org.jetbrains.annotations.NotNull;


public class LinkIDPaymentConfigurationUpdateException extends Exception {

    private final LinkIDPaymentConfigurationUpdateErrorCode errorCode;

    public LinkIDPaymentConfigurationUpdateException(final String message, @NotNull final LinkIDPaymentConfigurationUpdateErrorCode errorCode) {

        super( message );
        this.errorCode = errorCode;
    }

    public LinkIDPaymentConfigurationUpdateErrorCode getErrorCode() {

        return errorCode;
    }

}
