/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.ws.linkid.ltqr;

import java.io.Serializable;
import java.util.Date;
import net.link.safeonline.sdk.api.payment.LinkIDPaymentState;
import net.link.safeonline.sdk.api.qr.LinkIDQRInfo;
import org.jetbrains.annotations.Nullable;


/**
 * Created by wvdhaute
 * Date: 17/01/14
 * Time: 11:14
 */
@SuppressWarnings("UnusedDeclaration")
public class LinkIDLTQRClientSession implements Serializable {

    private final String       ltqrReference;
    private final LinkIDQRInfo qrCodeInfo;
    private final String       clientSessionId;
    private final String       userId;
    private final Date         created;

    @Nullable
    private final String             paymentOrderReference;
    private final LinkIDPaymentState paymentState;

    public LinkIDLTQRClientSession(final String ltqrReference, final LinkIDQRInfo qrCodeInfo, final String clientSessionId, final String userId,
                                   final Date created, final LinkIDPaymentState paymentState, @Nullable final String paymentOrderReference) {

        this.ltqrReference = ltqrReference;
        this.qrCodeInfo = qrCodeInfo;
        this.clientSessionId = clientSessionId;
        this.userId = userId;
        this.created = created;
        this.paymentState = paymentState;
        this.paymentOrderReference = paymentOrderReference;
    }

    // Helper methods

    @Override
    public String toString() {

        return "LinkIDLTQRClientSession{" +
               "ltqrReference='" + ltqrReference + '\'' +
               ", qrCodeInfo=" + qrCodeInfo +
               ", clientSessionId='" + clientSessionId + '\'' +
               ", userId='" + userId + '\'' +
               ", created=" + created +
               ", paymentOrderReference='" + paymentOrderReference + '\'' +
               ", paymentState=" + paymentState +
               '}';
    }

    // Accessors

    public String getLtqrReference() {

        return ltqrReference;
    }

    public LinkIDQRInfo getQrCodeInfo() {

        return qrCodeInfo;
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

    public LinkIDPaymentState getPaymentState() {

        return paymentState;
    }

    @Nullable
    public String getPaymentOrderReference() {

        return paymentOrderReference;
    }
}
