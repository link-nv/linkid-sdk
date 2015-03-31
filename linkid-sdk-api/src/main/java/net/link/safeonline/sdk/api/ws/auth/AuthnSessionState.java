package net.link.safeonline.sdk.api.ws.auth;

import java.io.Serializable;
import net.link.safeonline.sdk.api.payment.LinkIDPaymentState;


/**
 * Created by wvdhaute
 * Date: 22/07/14
 * Time: 15:17
 */
public class AuthnSessionState implements Serializable {

    private final AuthenticationState authenticationState;
    private final LinkIDPaymentState  paymentState;
    private final String              paymentMenuURL;

    public AuthnSessionState(final AuthenticationState authenticationState, final LinkIDPaymentState paymentState, final String paymentMenuURL) {

        this.authenticationState = authenticationState;
        this.paymentState = paymentState;
        this.paymentMenuURL = paymentMenuURL;
    }

    // Accessors

    public AuthenticationState getAuthenticationState() {

        return authenticationState;
    }

    public LinkIDPaymentState getPaymentState() {

        return paymentState;
    }

    public String getPaymentMenuURL() {

        return paymentMenuURL;
    }
}