/*
 * safe-online - linkid-sdk-api
 *
 * Copyright 2006-2016 linkID Inc. All rights reserved.
 * linkID Inc. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.ws.linkid.paymentconfiguration;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class LinkIDPaymentConfigurationAddException extends Exception {

    private final LinkIDPaymentConfigurationAddErrorCode errorCode;

    public LinkIDPaymentConfigurationAddException(final String message, @NotNull final LinkIDPaymentConfigurationAddErrorCode errorCode) {

        super( message );
        this.errorCode = errorCode;
    }

    @Nullable
    public LinkIDPaymentConfigurationAddErrorCode getErrorCode() {

        return errorCode;
    }

}
