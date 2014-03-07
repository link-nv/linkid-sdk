/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.ltqr;

import java.io.Serializable;
import java.util.Date;


/**
 * Created by wvdhaute
 * Date: 17/01/14
 * Time: 11:14
 */
public class LTQRClientSession implements Serializable {

    private final String           orderReference;
    private final String           clientSessionId;
    private final String           userId;
    private final Date             created;
    private final LTQRPaymentState paymentState;

    public LTQRClientSession(final String orderReference, final String clientSessionId, final String userId, final Date created,
                             final LTQRPaymentState paymentState) {

        this.orderReference = orderReference;
        this.clientSessionId = clientSessionId;
        this.userId = userId;
        this.created = created;
        this.paymentState = paymentState;
    }

    @Override
    public String toString() {

        return String.format( "SessionID: %s, ClientSessionID: %s, UserID: %s, Created: %s, PaymentState: %s", orderReference, clientSessionId, userId, created,
                paymentState );
    }

    public String getOrderReference() {

        return orderReference;
    }

    public String getClientSessionId() {

        return clientSessionId;
    }

    public String getUserId() {

        return userId;
    }

    public Date getCreated() {

        return created;
    }

    public LTQRPaymentState getPaymentState() {

        return paymentState;
    }
}
