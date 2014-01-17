package net.link.safeonline.sdk.api.ltqr;

public enum LTQRPaymentState {

    STARTED,                // payment is being processed
    PAYED,                  // completed
    FAILED;                 // payment has failed

    public static LTQRPaymentState parse(final String stateString) {

        for (LTQRPaymentState paymentState : LTQRPaymentState.values()) {

            if (paymentState.name().equals( stateString.toUpperCase() )) {
                return paymentState;
            }
        }

        throw new RuntimeException( String.format( "Unsupported payment state %s!", stateString ) );
    }

}
