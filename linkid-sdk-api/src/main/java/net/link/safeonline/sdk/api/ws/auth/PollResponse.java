package net.link.safeonline.sdk.api.ws.auth;

import java.io.Serializable;
import net.link.safeonline.sdk.api.payment.LinkIDPaymentState;


/**
 * Created by wvdhaute
 * Date: 30/04/14
 * Time: 13:37
 */
public class PollResponse<Response> implements Serializable {

    private final AuthenticationState authenticationState;
    private final LinkIDPaymentState  paymentState;
    private final String              paymentMenuURL;
    private final Response            response;

    public PollResponse(final AuthenticationState authenticationState, final LinkIDPaymentState paymentState, final String paymentMenuURL,
                        final Response response) {

        this.authenticationState = authenticationState;
        this.paymentState = paymentState;
        this.paymentMenuURL = paymentMenuURL;
        this.response = response;
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

    public Response getResponse() {

        return response;
    }
}
