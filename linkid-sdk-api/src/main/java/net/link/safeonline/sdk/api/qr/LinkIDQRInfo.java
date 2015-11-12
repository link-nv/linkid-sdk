package net.link.safeonline.sdk.api.qr;

import java.io.Serializable;
import java.util.Arrays;


/**
 * Created by wvdhaute
 * Date: 09/10/15
 * Time: 14:33
 */
@SuppressWarnings("unused")
public class LinkIDQRInfo implements Serializable {

    private final byte[]  qrImage;          // PNG of the QR code
    private final String  qrEncoded;        // base64 encoded PNG
    private final String  qrCodeURL;        // URL of the QR code to be shown in mobile clients ( if user agent was specified ), else defaults to default protocol
    private final String  qrContent;        // QR code content ( everything but the protocol )
    private final boolean mobile;           // If user agent was specified, will return whether the request was started from a mobile client or not. Else is false

    public LinkIDQRInfo(final byte[] qrImage, final String qrEncoded, final String qrCodeURL, final String qrContent, final boolean mobile) {

        this.qrImage = qrImage;
        this.qrEncoded = qrEncoded;
        this.qrCodeURL = qrCodeURL;
        this.qrContent = qrContent;
        this.mobile = mobile;
    }

    // Helper methods

    @Override
    public String toString() {

        return "LinkIDQRInfo{" +
               "qrImage=" + Arrays.toString( qrImage ) +
               ", qrEncoded='" + qrEncoded + '\'' +
               ", qrCodeURL='" + qrCodeURL + '\'' +
               ", qrContent='" + qrContent + '\'' +
               ", mobile=" + mobile +
               '}';
    }

    // Accessors

    public byte[] getQrImage() {

        return qrImage;
    }

    public String getQrEncoded() {

        return qrEncoded;
    }

    public String getQrCodeURL() {

        return qrCodeURL;
    }

    public String getQrContent() {

        return qrContent;
    }

    public boolean isMobile() {

        return mobile;
    }

}
