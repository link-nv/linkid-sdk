/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.ws.mandate;

import java.util.Locale;
import net.link.safeonline.sdk.api.payment.LinkIDPaymentContext;


/**
 * linkID Mandate WS Client.
 * <p/>
 * Via this interface, applications can make mandate payments
 */
public interface LinkIDMandateServiceClient {

    /**
     * Make a payment for specified mandate
     *
     * @return the order reference for this payment
     *
     * @throws LinkIDPayException something went wrong, check the error code in the exception
     */
    String pay(String mandateReference, LinkIDPaymentContext paymentContext, Locale locale)
            throws LinkIDPayException;
}
