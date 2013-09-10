package net.link.safeonline.sdk.api.ws.payment;

import net.link.safeonline.sdk.api.payment.PaymentState;


/**
 * linkID Payment WS Client.
 * <p/>
 * Via this interface, applications can fetch payment status reports.
 */
public interface PaymentServiceClient {

    PaymentState getStatus(String transactionId);
}
