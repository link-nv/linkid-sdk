package net.link.safeonline.sdk.api.auth;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import net.link.safeonline.sdk.api.attribute.LinkIDAttribute;
import net.link.safeonline.sdk.api.externalcode.LinkIDExternalCodeResponse;
import net.link.safeonline.sdk.api.payment.LinkIDPaymentResponse;


/**
 * Created by wvdhaute
 * Date: 02/05/14
 * Time: 10:58
 */
public class LinkIDAuthnResponse implements Serializable {

    private final String                                           userId;
    private final Map<String, List<LinkIDAttribute<Serializable>>> attributes;
    private final LinkIDPaymentResponse                            paymentResponse;
    private final LinkIDExternalCodeResponse                       externalCodeResponse;

    public LinkIDAuthnResponse(final String userId, final Map<String, List<LinkIDAttribute<Serializable>>> attributes,
                               final LinkIDPaymentResponse paymentResponse, final LinkIDExternalCodeResponse externalCodeResponse) {

        this.userId = userId;
        this.attributes = attributes;
        this.paymentResponse = paymentResponse;
        this.externalCodeResponse = externalCodeResponse;
    }

    @Override
    public String toString() {

        return "LinkIDAuthnResponse{" +
               "userId='" + userId + '\'' +
               ", attributes=" + attributes +
               ", paymentResponse=" + paymentResponse +
               ", externalCodeResponse=" + externalCodeResponse +
               '}';
    }

    // Accessors

    public String getUserId() {

        return userId;
    }

    public Map<String, List<LinkIDAttribute<Serializable>>> getAttributes() {

        return attributes;
    }

    public LinkIDPaymentResponse getPaymentResponse() {

        return paymentResponse;
    }

    public LinkIDExternalCodeResponse getExternalCodeResponse() {

        return externalCodeResponse;
    }
}
