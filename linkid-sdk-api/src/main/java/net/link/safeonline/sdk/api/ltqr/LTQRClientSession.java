/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.ltqr;

import java.io.Serializable;
import java.util.Date;
import org.jetbrains.annotations.Nullable;


/**
 * Created by wvdhaute
 * Date: 17/01/14
 * Time: 11:14
 */
@SuppressWarnings("UnusedDeclaration")
public class LTQRClientSession implements Serializable {

    private final byte[] qrCodeImage;
    private final String qrCodeURL;
    private final String ltqrReference;
    private final String clientSessionId;
    private final String userId;
    private final Date   created;

    @Nullable
    private final String           paymentOrderReference;
    private final LTQRPaymentState paymentState;

    public LTQRClientSession(final byte[] qrCodeImage, final String qrCodeURL, final String ltqrReference, final String clientSessionId, final String userId,
                             final Date created, final LTQRPaymentState paymentState, @Nullable final String paymentOrderReference) {

        this.qrCodeImage = qrCodeImage;
        this.qrCodeURL = qrCodeURL;
        this.ltqrReference = ltqrReference;
        this.clientSessionId = clientSessionId;
        this.userId = userId;
        this.created = created;
        this.paymentState = paymentState;
        this.paymentOrderReference = paymentOrderReference;
    }

    @Override
    public String toString() {

        return String.format( "QR code: %s, LTQR Ref: %s, ClientSessionID: %s, UserID: %s, Created: %s, PaymentState: %s, Payment order ref: %s", qrCodeURL,
                ltqrReference, clientSessionId, userId, created, paymentState, paymentOrderReference );
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

    public String getClientSessionId() {

        return clientSessionId;
    }

    public String getUserId() {

        return userId;
    }

    public Date getCreated() {

        return created;
    }

    public LTQRPaymentState getPaymentState() {

        return paymentState;
    }

    @Nullable
    public String getPaymentOrderReference() {

        return paymentOrderReference;
    }
}
