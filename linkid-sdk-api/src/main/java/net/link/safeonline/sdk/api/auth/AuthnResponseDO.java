package net.link.safeonline.sdk.api.auth;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import net.link.safeonline.sdk.api.attribute.AttributeSDK;
import net.link.safeonline.sdk.api.externalcode.ExternalCodeResponseDO;
import net.link.safeonline.sdk.api.payment.PaymentResponseDO;


/**
 * Created by wvdhaute
 * Date: 02/05/14
 * Time: 10:58
 */
public class AuthnResponseDO implements Serializable {

    private final String                                        userId;
    private final Map<String, List<AttributeSDK<Serializable>>> attributes;
    private final PaymentResponseDO                             paymentResponse;
    private final ExternalCodeResponseDO                        externalCodeResponse;

    public AuthnResponseDO(final String userId, final Map<String, List<AttributeSDK<Serializable>>> attributes, final PaymentResponseDO paymentResponse,
                           final ExternalCodeResponseDO externalCodeResponse) {

        this.userId = userId;
        this.attributes = attributes;
        this.paymentResponse = paymentResponse;
        this.externalCodeResponse = externalCodeResponse;
    }

    @Override
    public String toString() {

        return String.format( "linkID.authnResponse: userId=\"%s\"", userId );
    }

    // Accessors

    public String getUserId() {

        return userId;
    }

    public Map<String, List<AttributeSDK<Serializable>>> getAttributes() {

        return attributes;
    }

    public PaymentResponseDO getPaymentResponse() {

        return paymentResponse;
    }

    public ExternalCodeResponseDO getExternalCodeResponse() {

        return externalCodeResponse;
    }
}
