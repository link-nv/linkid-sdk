package net.link.safeonline.sdk.ws.auth;

import net.link.safeonline.sdk.api.payment.PaymentState;
import net.link.safeonline.sdk.api.ws.auth.AuthenticationState;
import net.link.util.InternalInconsistencyException;


/**
 * Created by wvdhaute
 * Date: 30/04/14
 * Time: 16:59
 */
public abstract class ConversionUtils {

    public static net.lin_k.safe_online.auth.AuthenticationState convert(final AuthenticationState authenticationState) {

        switch (authenticationState) {

            case STARTED:
                return net.lin_k.safe_online.auth.AuthenticationState.LINKID_STATE_STARTED;
            case RETRIEVED:
                return net.lin_k.safe_online.auth.AuthenticationState.LINKID_STATE_RETRIEVED;
            case AUTHENTICATED:
                return net.lin_k.safe_online.auth.AuthenticationState.LINKID_STATE_AUTHENTICATED;
            case EXPIRED:
                return net.lin_k.safe_online.auth.AuthenticationState.LINKID_STATE_EXPIRED;
            case FAILED:
                return net.lin_k.safe_online.auth.AuthenticationState.LINKID_STATE_FAILED;
            case PAYMENT_ADD:
                return net.lin_k.safe_online.auth.AuthenticationState.LINKID_STATE_PAYMENT_ADD;
        }

        throw new InternalInconsistencyException( String.format( "Invalid authentication state: %s", authenticationState ) );
    }

    public static AuthenticationState convert(final net.lin_k.safe_online.auth.AuthenticationState authenticationState) {

        switch (authenticationState) {

            case LINKID_STATE_STARTED:
                return AuthenticationState.STARTED;
            case LINKID_STATE_RETRIEVED:
                return AuthenticationState.RETRIEVED;
            case LINKID_STATE_AUTHENTICATED:
                return AuthenticationState.AUTHENTICATED;
            case LINKID_STATE_EXPIRED:
                return AuthenticationState.EXPIRED;
            case LINKID_STATE_FAILED:
                return AuthenticationState.FAILED;
            case LINKID_STATE_PAYMENT_ADD:
                return AuthenticationState.PAYMENT_ADD;
        }

        throw new InternalInconsistencyException( String.format( "Invalid authentication state %s!", authenticationState ) );
    }

    public static net.lin_k.safe_online.auth.PaymentState convert(final PaymentState paymentState) {

        switch (paymentState) {

            case STARTED:
                return net.lin_k.safe_online.auth.PaymentState.LINKID_PAYMENT_STATE_STARTED;
            case DEFERRED:
                return net.lin_k.safe_online.auth.PaymentState.LINKID_PAYMENT_STATE_DEFERRED;
            case WAITING_FOR_UPDATE:
                return net.lin_k.safe_online.auth.PaymentState.LINKID_PAYMENT_STATE_WAITING;
            case FAILED:
                return net.lin_k.safe_online.auth.PaymentState.LINKID_PAYMENT_STATE_FAILED;
            case REFUNDED:
                return net.lin_k.safe_online.auth.PaymentState.LINKID_PAYMENT_STATE_REFUNDED;
            case REFUND_STARTED:
                return net.lin_k.safe_online.auth.PaymentState.LINKID_PAYMENT_STATE_REFUND_STARTED;
            case PAYED:
                return net.lin_k.safe_online.auth.PaymentState.LINKID_PAYMENT_STATE_PAYED;
        }

        throw new InternalInconsistencyException( String.format( "Invalid payment state: %s", paymentState ) );
    }

    public static PaymentState convert(final net.lin_k.safe_online.auth.PaymentState paymentState) {

        switch (paymentState) {

            case LINKID_PAYMENT_STATE_STARTED:
                return PaymentState.STARTED;
            case LINKID_PAYMENT_STATE_DEFERRED:
                return PaymentState.DEFERRED;
            case LINKID_PAYMENT_STATE_WAITING:
                return PaymentState.WAITING_FOR_UPDATE;
            case LINKID_PAYMENT_STATE_FAILED:
                return PaymentState.FAILED;
            case LINKID_PAYMENT_STATE_REFUNDED:
                return PaymentState.REFUNDED;
            case LINKID_PAYMENT_STATE_REFUND_STARTED:
                return PaymentState.REFUND_STARTED;
            case LINKID_PAYMENT_STATE_PAYED:
                return PaymentState.PAYED;
        }

        throw new InternalInconsistencyException( String.format( "Invalid payment state: %s", paymentState ) );
    }
}
