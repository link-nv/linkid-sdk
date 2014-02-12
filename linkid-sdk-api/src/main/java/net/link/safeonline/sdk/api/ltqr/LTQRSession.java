package net.link.safeonline.sdk.api.ltqr;

import java.io.Serializable;


/**
 * Created by wvdhaute
 * Date: 14/01/14
 * Time: 11:01
 */
public class LTQRSession implements Serializable {

    private final byte[] qrCodeImage;
    private final String qrCodeURL;
    private final String orderReference;

    public LTQRSession(final byte[] qrCodeImage, final String qrCodeURL, final String orderReference) {

        this.qrCodeImage = qrCodeImage;
        this.qrCodeURL = qrCodeURL;
        this.orderReference = orderReference;
    }

    public byte[] getQrCodeImage() {

        return qrCodeImage;
    }

    public String getQrCodeURL() {

        return qrCodeURL;
    }

    public String getOrderReference() {

        return orderReference;
    }
}
