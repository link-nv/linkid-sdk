/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.ws.linkid.ltqr;

import java.io.Serializable;
import net.link.safeonline.sdk.api.qr.LinkIDQRInfo;
import org.jetbrains.annotations.Nullable;


/**
 * Created by wvdhaute
 * Date: 14/01/14
 * Time: 11:01
 */
public class LinkIDLTQRSession implements Serializable {

    private final String       ltqrReference;
    private final LinkIDQRInfo qrCodeInfo;
    //
    @Nullable
    private final String       paymentOrderReference;    // optional payment order reference, if applicable

    public LinkIDLTQRSession(final String ltqrReference, final LinkIDQRInfo qrCodeInfo, @Nullable final String paymentOrderReference) {

        this.ltqrReference = ltqrReference;
        this.qrCodeInfo = qrCodeInfo;
        this.paymentOrderReference = paymentOrderReference;
    }

    // Helper methods

    @Override
    public String toString() {

        return "LinkIDLTQRSession{" +
               "ltqrReference='" + ltqrReference + '\'' +
               ", qrCodeInfo=" + qrCodeInfo +
               ", paymentOrderReference='" + paymentOrderReference + '\'' +
               '}';
    }

    // Accessors

    public String getLtqrReference() {

        return ltqrReference;
    }

    public LinkIDQRInfo getQrCodeInfo() {

        return qrCodeInfo;
    }

    @Nullable
    public String getPaymentOrderReference() {

        return paymentOrderReference;
    }
}
