/*
 * safe-online - linkid-sdk-api
 *
 * Copyright 2006-2016 linkID Inc. All rights reserved.
 * linkID Inc. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.ws.linkid.paymentconfiguration;

import org.jetbrains.annotations.NotNull;


public class LinkIDPaymentConfigurationRemoveException extends Exception {

    private final LinkIDPaymentConfigurationRemoveErrorCode errorCode;

    public LinkIDPaymentConfigurationRemoveException(final String message, @NotNull final LinkIDPaymentConfigurationRemoveErrorCode errorCode) {

        super( message );
        this.errorCode = errorCode;
    }

    public LinkIDPaymentConfigurationRemoveErrorCode getErrorCode() {

        return errorCode;
    }

}
