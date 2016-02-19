package net.link.safeonline.sdk.api.ws.linkid.auth;

import java.io.Serializable;
import net.link.safeonline.sdk.api.auth.LinkIDAuthnResponse;
import net.link.safeonline.sdk.api.payment.LinkIDPaymentState;
import org.jetbrains.annotations.Nullable;


/**
 * Created by wvdhaute
 * Date: 30/04/14
 * Time: 13:37
 */
public class LinkIDAuthPollResponse implements Serializable {

    // polling state
    private final LinkIDAuthenticationState authenticationState;
    private final LinkIDPaymentState        paymentState;
    private final String                    paymentMenuURL;
    //
    // the linkID authentication response if finished
    @Nullable
    private final LinkIDAuthnResponse       authnResponse;

    public LinkIDAuthPollResponse(final LinkIDAuthenticationState authenticationState, final LinkIDPaymentState paymentState, final String paymentMenuURL,
                                  @Nullable final LinkIDAuthnResponse authnResponse) {

        this.authenticationState = authenticationState;
        this.paymentState = paymentState;
        this.paymentMenuURL = paymentMenuURL;

        this.authnResponse = authnResponse;
    }

    // Accessors

    public LinkIDAuthenticationState getAuthenticationState() {

        return authenticationState;
    }

    public LinkIDPaymentState getPaymentState() {

        return paymentState;
    }

    public String getPaymentMenuURL() {

        return paymentMenuURL;
    }

    @Nullable
    public LinkIDAuthnResponse getAuthnResponse() {

        return authnResponse;
    }

}
