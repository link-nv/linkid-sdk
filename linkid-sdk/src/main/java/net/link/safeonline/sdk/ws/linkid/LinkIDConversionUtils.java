package net.link.safeonline.sdk.ws.linkid;

import net.link.safeonline.sdk.api.payment.LinkIDPaymentState;
import net.link.safeonline.sdk.api.ws.linkid.auth.LinkIDAuthenticationState;
import net.link.util.InternalInconsistencyException;


/**
 * Created by wvdhaute
 * Date: 30/04/14
 * Time: 16:59
 */
public abstract class LinkIDConversionUtils {

    public static net.lin_k.safe_online.auth.AuthenticationState convert(final LinkIDAuthenticationState linkIDAuthenticationState) {

        switch (linkIDAuthenticationState) {

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

        throw new InternalInconsistencyException( String.format( "Invalid authentication state: %s", linkIDAuthenticationState ) );
    }

    public static net.lin_k.safe_online.auth._2.AuthenticationState convert20(final LinkIDAuthenticationState linkIDAuthenticationState) {

        switch (linkIDAuthenticationState) {

            case STARTED:
                return net.lin_k.safe_online.auth._2.AuthenticationState.LINKID_STATE_STARTED;
            case RETRIEVED:
                return net.lin_k.safe_online.auth._2.AuthenticationState.LINKID_STATE_RETRIEVED;
            case AUTHENTICATED:
                return net.lin_k.safe_online.auth._2.AuthenticationState.LINKID_STATE_AUTHENTICATED;
            case EXPIRED:
                return net.lin_k.safe_online.auth._2.AuthenticationState.LINKID_STATE_EXPIRED;
            case FAILED:
                return net.lin_k.safe_online.auth._2.AuthenticationState.LINKID_STATE_FAILED;
            case PAYMENT_ADD:
                return net.lin_k.safe_online.auth._2.AuthenticationState.LINKID_STATE_PAYMENT_ADD;
        }

        throw new InternalInconsistencyException( String.format( "Invalid authentication state: %s", linkIDAuthenticationState ) );
    }

    public static net.lin_k.safe_online.auth._3.AuthenticationState convert30(final LinkIDAuthenticationState linkIDAuthenticationState) {

        switch (linkIDAuthenticationState) {

            case STARTED:
                return net.lin_k.safe_online.auth._3.AuthenticationState.LINKID_STATE_STARTED;
            case RETRIEVED:
                return net.lin_k.safe_online.auth._3.AuthenticationState.LINKID_STATE_RETRIEVED;
            case AUTHENTICATED:
                return net.lin_k.safe_online.auth._3.AuthenticationState.LINKID_STATE_AUTHENTICATED;
            case EXPIRED:
                return net.lin_k.safe_online.auth._3.AuthenticationState.LINKID_STATE_EXPIRED;
            case FAILED:
                return net.lin_k.safe_online.auth._3.AuthenticationState.LINKID_STATE_FAILED;
            case PAYMENT_ADD:
                return net.lin_k.safe_online.auth._3.AuthenticationState.LINKID_STATE_PAYMENT_ADD;
        }

        throw new InternalInconsistencyException( String.format( "Invalid authentication state: %s", linkIDAuthenticationState ) );
    }

    public static net.lin_k.linkid._3.AuthAuthenticationState convert40(final LinkIDAuthenticationState linkIDAuthenticationState) {

        switch (linkIDAuthenticationState) {

            case STARTED:
                return net.lin_k.linkid._3.AuthAuthenticationState.LINKID_STATE_STARTED;
            case RETRIEVED:
                return net.lin_k.linkid._3.AuthAuthenticationState.LINKID_STATE_RETRIEVED;
            case AUTHENTICATED:
                return net.lin_k.linkid._3.AuthAuthenticationState.LINKID_STATE_AUTHENTICATED;
            case EXPIRED:
                return net.lin_k.linkid._3.AuthAuthenticationState.LINKID_STATE_EXPIRED;
            case FAILED:
                return net.lin_k.linkid._3.AuthAuthenticationState.LINKID_STATE_FAILED;
            case PAYMENT_ADD:
                return net.lin_k.linkid._3.AuthAuthenticationState.LINKID_STATE_PAYMENT_ADD;
        }

        throw new InternalInconsistencyException( String.format( "Invalid authentication state: %s", linkIDAuthenticationState ) );
    }

    public static LinkIDAuthenticationState convert(final net.lin_k.linkid._3.AuthAuthenticationState authenticationState) {

        switch (authenticationState) {

            case LINKID_STATE_STARTED:
                return LinkIDAuthenticationState.STARTED;
            case LINKID_STATE_RETRIEVED:
                return LinkIDAuthenticationState.RETRIEVED;
            case LINKID_STATE_AUTHENTICATED:
                return LinkIDAuthenticationState.AUTHENTICATED;
            case LINKID_STATE_EXPIRED:
                return LinkIDAuthenticationState.EXPIRED;
            case LINKID_STATE_FAILED:
                return LinkIDAuthenticationState.FAILED;
            case LINKID_STATE_PAYMENT_ADD:
                return LinkIDAuthenticationState.PAYMENT_ADD;
        }

        throw new InternalInconsistencyException( String.format( "Invalid authentication state %s!", authenticationState ) );
    }

    public static net.lin_k.safe_online.auth.PaymentState convert(final LinkIDPaymentState paymentState) {

        switch (paymentState) {

            case STARTED:
                return net.lin_k.safe_online.auth.PaymentState.LINKID_PAYMENT_STATE_STARTED;
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

    public static net.lin_k.safe_online.auth._2.PaymentState convert20(final LinkIDPaymentState paymentState) {

        switch (paymentState) {

            case STARTED:
                return net.lin_k.safe_online.auth._2.PaymentState.LINKID_PAYMENT_STATE_STARTED;
            case WAITING_FOR_UPDATE:
                return net.lin_k.safe_online.auth._2.PaymentState.LINKID_PAYMENT_STATE_WAITING;
            case FAILED:
                return net.lin_k.safe_online.auth._2.PaymentState.LINKID_PAYMENT_STATE_FAILED;
            case REFUNDED:
                return net.lin_k.safe_online.auth._2.PaymentState.LINKID_PAYMENT_STATE_REFUNDED;
            case REFUND_STARTED:
                return net.lin_k.safe_online.auth._2.PaymentState.LINKID_PAYMENT_STATE_REFUND_STARTED;
            case PAYED:
                return net.lin_k.safe_online.auth._2.PaymentState.LINKID_PAYMENT_STATE_PAYED;
        }

        throw new InternalInconsistencyException( String.format( "Invalid payment state: %s", paymentState ) );
    }

    public static net.lin_k.safe_online.auth._3.PaymentState convert30(final LinkIDPaymentState paymentState) {

        switch (paymentState) {

            case STARTED:
                return net.lin_k.safe_online.auth._3.PaymentState.LINKID_PAYMENT_STATE_STARTED;
            case WAITING_FOR_UPDATE:
                return net.lin_k.safe_online.auth._3.PaymentState.LINKID_PAYMENT_STATE_WAITING;
            case FAILED:
                return net.lin_k.safe_online.auth._3.PaymentState.LINKID_PAYMENT_STATE_FAILED;
            case REFUNDED:
                return net.lin_k.safe_online.auth._3.PaymentState.LINKID_PAYMENT_STATE_REFUNDED;
            case REFUND_STARTED:
                return net.lin_k.safe_online.auth._3.PaymentState.LINKID_PAYMENT_STATE_REFUND_STARTED;
            case PAYED:
                return net.lin_k.safe_online.auth._3.PaymentState.LINKID_PAYMENT_STATE_PAYED;
        }

        throw new InternalInconsistencyException( String.format( "Invalid payment state: %s", paymentState ) );
    }

    public static net.lin_k.linkid._3.AuthPaymentState convert40(final LinkIDPaymentState paymentState) {

        switch (paymentState) {

            case STARTED:
                return net.lin_k.linkid._3.AuthPaymentState.LINKID_PAYMENT_STATE_STARTED;
            case WAITING_FOR_UPDATE:
                return net.lin_k.linkid._3.AuthPaymentState.LINKID_PAYMENT_STATE_WAITING;
            case FAILED:
                return net.lin_k.linkid._3.AuthPaymentState.LINKID_PAYMENT_STATE_FAILED;
            case REFUNDED:
                return net.lin_k.linkid._3.AuthPaymentState.LINKID_PAYMENT_STATE_REFUNDED;
            case REFUND_STARTED:
                return net.lin_k.linkid._3.AuthPaymentState.LINKID_PAYMENT_STATE_REFUND_STARTED;
            case PAYED:
                return net.lin_k.linkid._3.AuthPaymentState.LINKID_PAYMENT_STATE_PAYED;
        }

        throw new InternalInconsistencyException( String.format( "Invalid payment state: %s", paymentState ) );
    }

    public static LinkIDPaymentState convert(final net.lin_k.safe_online.auth._2.PaymentState paymentState) {

        switch (paymentState) {

            case LINKID_PAYMENT_STATE_STARTED:
                return LinkIDPaymentState.STARTED;
            case LINKID_PAYMENT_STATE_WAITING:
                return LinkIDPaymentState.WAITING_FOR_UPDATE;
            case LINKID_PAYMENT_STATE_FAILED:
                return LinkIDPaymentState.FAILED;
            case LINKID_PAYMENT_STATE_REFUNDED:
                return LinkIDPaymentState.REFUNDED;
            case LINKID_PAYMENT_STATE_REFUND_STARTED:
                return LinkIDPaymentState.REFUND_STARTED;
            case LINKID_PAYMENT_STATE_PAYED:
                return LinkIDPaymentState.PAYED;
        }

        throw new InternalInconsistencyException( String.format( "Invalid payment state: %s", paymentState ) );
    }

    public static LinkIDPaymentState convert(final net.lin_k.safe_online.auth._3.PaymentState paymentState) {

        switch (paymentState) {

            case LINKID_PAYMENT_STATE_STARTED:
                return LinkIDPaymentState.STARTED;
            case LINKID_PAYMENT_STATE_WAITING:
                return LinkIDPaymentState.WAITING_FOR_UPDATE;
            case LINKID_PAYMENT_STATE_FAILED:
                return LinkIDPaymentState.FAILED;
            case LINKID_PAYMENT_STATE_REFUNDED:
                return LinkIDPaymentState.REFUNDED;
            case LINKID_PAYMENT_STATE_REFUND_STARTED:
                return LinkIDPaymentState.REFUND_STARTED;
            case LINKID_PAYMENT_STATE_PAYED:
                return LinkIDPaymentState.PAYED;
        }

        throw new InternalInconsistencyException( String.format( "Invalid payment state: %s", paymentState ) );
    }

    public static LinkIDPaymentState convert(final net.lin_k.linkid._3.AuthPaymentState paymentState) {

        switch (paymentState) {

            case LINKID_PAYMENT_STATE_STARTED:
                return LinkIDPaymentState.STARTED;
            case LINKID_PAYMENT_STATE_WAITING:
                return LinkIDPaymentState.WAITING_FOR_UPDATE;
            case LINKID_PAYMENT_STATE_FAILED:
                return LinkIDPaymentState.FAILED;
            case LINKID_PAYMENT_STATE_REFUNDED:
                return LinkIDPaymentState.REFUNDED;
            case LINKID_PAYMENT_STATE_REFUND_STARTED:
                return LinkIDPaymentState.REFUND_STARTED;
            case LINKID_PAYMENT_STATE_PAYED:
                return LinkIDPaymentState.PAYED;
        }

        throw new InternalInconsistencyException( String.format( "Invalid payment state: %s", paymentState ) );
    }
}
