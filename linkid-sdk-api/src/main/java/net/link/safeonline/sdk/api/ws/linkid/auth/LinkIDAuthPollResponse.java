package net.link.safeonline.sdk.api.ws.linkid.auth;

import java.io.Serializable;
import net.link.safeonline.sdk.api.payment.LinkIDPaymentState;


/**
 * Created by wvdhaute
 * Date: 30/04/14
 * Time: 13:37
 */
public class LinkIDAuthPollResponse<Response> implements Serializable {

    private final LinkIDAuthenticationState linkIDAuthenticationState;
    private final LinkIDPaymentState        paymentState;
    private final String                    paymentMenuURL;
    private final Response                  response;

    public LinkIDAuthPollResponse(final LinkIDAuthenticationState linkIDAuthenticationState, final LinkIDPaymentState paymentState, final String paymentMenuURL,
                                  final Response response) {

        this.linkIDAuthenticationState = linkIDAuthenticationState;
        this.paymentState = paymentState;
        this.paymentMenuURL = paymentMenuURL;
        this.response = response;
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

    public Response getResponse() {

        return response;
    }
}
