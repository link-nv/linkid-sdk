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
    private final LinkIDAuthenticationState linkIDAuthenticationState;
    private final LinkIDPaymentState        paymentState;
    private final String                    paymentMenuURL;
    //
    // the linkID authentication response if finished
    @Nullable
    private final LinkIDAuthnResponse       linkIDAuthnResponse;

    public LinkIDAuthPollResponse(final LinkIDAuthenticationState linkIDAuthenticationState, final LinkIDPaymentState paymentState, final String paymentMenuURL,
                                  @Nullable final LinkIDAuthnResponse linkIDAuthnResponse) {

        this.linkIDAuthenticationState = linkIDAuthenticationState;
        this.paymentState = paymentState;
        this.paymentMenuURL = paymentMenuURL;

        this.linkIDAuthnResponse = linkIDAuthnResponse;
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

    @Nullable
    public LinkIDAuthnResponse getLinkIDAuthnResponse() {

        return linkIDAuthnResponse;
    }

}
