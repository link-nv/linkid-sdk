package net.link.safeonline.sdk.api.ws.auth;

import java.io.Serializable;
import net.link.safeonline.sdk.api.payment.LinkIDPaymentState;


/**
 * Created by wvdhaute
 * Date: 22/07/14
 * Time: 15:17
 */
public class LinkIDAuthnSessionState implements Serializable {

    private final LinkIDAuthenticationState linkIDAuthenticationState;
    private final LinkIDPaymentState        paymentState;
    private final String                    paymentMenuURL;

    public LinkIDAuthnSessionState(final LinkIDAuthenticationState linkIDAuthenticationState, final LinkIDPaymentState paymentState,
                                   final String paymentMenuURL) {

        this.linkIDAuthenticationState = linkIDAuthenticationState;
        this.paymentState = paymentState;
        this.paymentMenuURL = paymentMenuURL;
    }

    // Accessors

    public LinkIDAuthenticationState getLinkIDAuthenticationState() {

        return linkIDAuthenticationState;
    }

    public LinkIDPaymentState getPaymentState() {

        return paymentState;
    }

    public String getPaymentMenuURL() {

        return paymentMenuURL;
    }
}