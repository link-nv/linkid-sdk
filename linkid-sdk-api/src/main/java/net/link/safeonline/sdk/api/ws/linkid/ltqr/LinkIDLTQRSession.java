/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.ws.linkid.ltqr;

import java.io.Serializable;
import org.jetbrains.annotations.Nullable;


/**
 * Created by wvdhaute
 * Date: 14/01/14
 * Time: 11:01
 */
public class LinkIDLTQRSession implements Serializable {

    private final byte[] qrCodeImage;
    private final String qrCodeURL;
    private final String ltqrReference;

    @Nullable
    private final String paymentOrderReference;    // optional payment order reference, if applicable

    public LinkIDLTQRSession(final byte[] qrCodeImage, final String qrCodeURL, final String ltqrReference, @Nullable final String paymentOrderReference) {

        this.qrCodeImage = qrCodeImage;
        this.qrCodeURL = qrCodeURL;
        this.ltqrReference = ltqrReference;
        this.paymentOrderReference = paymentOrderReference;
    }

    public byte[] getQrCodeImage() {

        return qrCodeImage;
    }

    public String getQrCodeURL() {

        return qrCodeURL;
    }

    public String getLtqrReference() {

        return ltqrReference;
    }

    @Nullable
    public String getPaymentOrderReference() {

        return paymentOrderReference;
    }
}
