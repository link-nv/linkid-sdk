package net.link.safeonline.sdk.api.ltqr;

import java.io.Serializable;
import java.util.Date;


/**
 * Created by wvdhaute
 * Date: 17/01/14
 * Time: 11:14
 */
public class LTQRClientSession implements Serializable {

    private final String           sessionId;
    private final String           clientSessionId;
    private final String           userId;
    private final Date             created;
    private final LTQRPaymentState paymentState;

    public LTQRClientSession(final String sessionId, final String clientSessionId, final String userId, final Date created,
                             final LTQRPaymentState paymentState) {

        this.sessionId = sessionId;
        this.clientSessionId = clientSessionId;
        this.userId = userId;
        this.created = created;
        this.paymentState = paymentState;
    }

    @Override
    public String toString() {

        return String.format( "SessionID: %s, ClientSessionID: %s, UserID: %s, Created: %s, PaymentState: %s", sessionId, clientSessionId, userId, created,
                paymentState );
    }

    public String getSessionId() {

        return sessionId;
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
