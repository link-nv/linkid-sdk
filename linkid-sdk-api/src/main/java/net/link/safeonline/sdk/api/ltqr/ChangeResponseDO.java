package net.link.safeonline.sdk.api.ltqr;

import java.io.Serializable;
import org.jetbrains.annotations.Nullable;


/**
 * Created by wvdhaute
 * Date: 26/09/14
 * Time: 08:15
 */
public class ChangeResponseDO implements Serializable {

    @Nullable
    private final String paymentOrderReference;

    public ChangeResponseDO(@Nullable final String paymentOrderReference) {

        this.paymentOrderReference = paymentOrderReference;
    }

    @Nullable
    public String getPaymentOrderReference() {

        return paymentOrderReference;
    }
}
