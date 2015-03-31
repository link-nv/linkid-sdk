package net.link.safeonline.sdk.api.parking;

import java.io.Serializable;
import java.util.Date;
import net.link.safeonline.sdk.api.payment.LinkIDPaymentState;


/**
 * Created by wvdhaute
 * Date: 29/08/14
 * Time: 14:24
 */
@SuppressWarnings("UnusedDeclaration")
public class LinkIDParkingSession implements Serializable {

    private final Date               date;
    private final String             barCode;
    private final String             parking;
    private final String             userId;
    private final double             turnover;
    private final boolean            validated;
    private final String             paymentOrderReference;
    private final LinkIDPaymentState paymentState;

    public LinkIDParkingSession(final Date date, final String barCode, final String parking, final String userId, final double turnover,
                                final boolean validated, final String paymentOrderReference, final LinkIDPaymentState paymentState) {

        this.date = date;
        this.barCode = barCode;
        this.parking = parking;
        this.userId = userId;
        this.turnover = turnover;
        this.validated = validated;
        this.paymentOrderReference = paymentOrderReference;
        this.paymentState = paymentState;
    }

    // Accessors

    public Date getDate() {

        return date;
    }

    public String getBarCode() {

        return barCode;
    }

    public String getParking() {

        return parking;
    }

    public String getUserId() {

        return userId;
    }

    public double getTurnover() {

        return turnover;
    }

    public boolean isValidated() {

        return validated;
    }

    public String getPaymentOrderReference() {

        return paymentOrderReference;
    }

    public LinkIDPaymentState getPaymentState() {

        return paymentState;
    }
}
