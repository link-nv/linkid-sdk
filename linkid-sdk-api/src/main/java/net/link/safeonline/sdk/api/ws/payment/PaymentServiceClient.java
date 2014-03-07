/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.ws.payment;

import net.link.safeonline.sdk.api.exception.WSClientTransportException;
import net.link.safeonline.sdk.api.payment.PaymentState;


/**
 * linkID Payment WS Client.
 * <p/>
 * Via this interface, applications can fetch payment status reports.
 */
public interface PaymentServiceClient {

    PaymentState getStatus(String orderReference)
            throws WSClientTransportException;
}
