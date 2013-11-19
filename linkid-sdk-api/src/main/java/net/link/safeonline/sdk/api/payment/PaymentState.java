package net.link.safeonline.sdk.api.payment;

public enum PaymentState {

    STARTED,                // payment is being processed
    DEFERRED,               // deferred payment
    WAITING_FOR_UPDATE,     // linkID stopped waiting for status update, SP will be informed on payment status change
    FAILED,                 // payment has failed
    REFUNDED,               // payment has been refunded
    PAYED;                  // completed

    public static PaymentState parse(final String stateString) {

        for (PaymentState paymentState : PaymentState.values()) {

            if (paymentState.name().equals( stateString.toUpperCase() )) {
                return paymentState;
            }
        }

        throw new RuntimeException( String.format( "Unsupported payment state %s!", stateString ) );
    }

}
